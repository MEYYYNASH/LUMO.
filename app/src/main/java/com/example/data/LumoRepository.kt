package com.example.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class LumoRepository(
    private val context: Context,
    private val dao: LumoDao,
    private val externalScope: CoroutineScope,
    private val catalogService: LumoOnlineCatalogService = LumoOnlineCatalogService()
) {
    val allMedia: Flow<List<MediaItemEntity>> = dao.observeAllMedia()
    val favoriteMedia: Flow<List<MediaItemEntity>> = dao.observeFavoriteMedia()
    val historyMedia: Flow<List<MediaItemEntity>> = dao.observeHistoryMedia()
    val playlists: Flow<List<PlaylistEntity>> = dao.observePlaylists()
    val allDownloads: Flow<List<DownloadTaskEntity>> = dao.observeAllDownloads()
    val settings: Flow<LumoSettingsEntity> = dao.observeSettings().map { it ?: LumoSettingsEntity() }

    private val activeDownloadJobs = ConcurrentHashMap<String, Job>()
    private val downloadHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    suspend fun initializeSettingsAndDiscoverLiveMusic(): Result<List<MediaItemEntity>> = withContext(Dispatchers.IO) {
        if (dao.getSettingsOnce() == null) {
            dao.upsertSettings(LumoSettingsEntity())
        }
        // Purge any legacy 30-second preview tracks, offline proxy URLs, or old unsaved fake trending items
        dao.removeShortPreviewItems()
        dao.removeMismatchedYouTubeItems()
        dao.removeLegacyCatalogItems()
        dao.purgeUnsavedCatalogItems()
        dao.removeBrokenProxyDownloads()

        val worldTrending = fetchRealWorldTrending(TrendingFeedFilter.ALL_TRENDING).getOrDefault(emptyList())
        if (dao.countMediaItems() == 0 && worldTrending.isNotEmpty()) {
            dao.insertAllMedia(worldTrending)
        }
        Result.success(worldTrending)
    }

    suspend fun fetchRealWorldTrending(
        filter: TrendingFeedFilter
    ): Result<List<MediaItemEntity>> = withContext(Dispatchers.IO) {
        val result = catalogService.fetchRealWorldTrendingCatalog(
            filter = filter,
            limit = 28
        )
        result.map { liveItems ->
            val existingById = dao.getAllMediaOnce().associateBy { it.id }
            val merged = liveItems.map { fetched ->
                val existing = existingById[fetched.id]
                if (existing != null) {
                    val validLocalPath = existing.localFilePath.takeIf {
                        it.isNotBlank() && File(it).exists() && File(it).length() > 1024L
                    }.orEmpty()
                    fetched.copy(
                        isFavorite = existing.isFavorite,
                        inLibrary = existing.inLibrary,
                        localFilePath = validLocalPath,
                        lastPlayedTimestamp = existing.lastPlayedTimestamp,
                        lastPositionSec = existing.lastPositionSec,
                        playCount = existing.playCount,
                        // Always use the real live worldwide popularityScore from global APIs, never inflated local cache
                        popularityScore = fetched.popularityScore
                    )
                } else {
                    fetched
                }
            }
            val sortedTrending = KhmerArtistDirectory.sortTrendingNewFirst(merged, filter)
            // Update metadata only for tracks the user already has in their library/history; do not mix unsaved user tracks into Trending
            sortedTrending.forEach { track ->
                if (existingById.containsKey(track.id)) {
                    runCatching { dao.upsertMedia(track) }
                }
            }
            sortedTrending
        }
    }

    suspend fun refreshPlayableStreamUrl(
        media: MediaItemEntity,
        forceRefresh: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        val localFile = if (media.localFilePath.isNotBlank()) File(media.localFilePath) else null
        if (!forceRefresh && localFile != null && localFile.exists() && localFile.length() > 1024L) {
            return@withContext localFile.absolutePath
        }
        // Keep media.streamUrl intact so SoundCloud /media/ transcoding pointers can still be resolved
        val resolved = catalogService.resolvePlayableStreamUrl(media, forceRefresh = forceRefresh)
        if (resolved.isNotBlank() && resolved != media.streamUrl && !media.streamUrl.contains("/media/")) {
            runCatching { dao.updateStreamUrl(media.id, resolved) }
        }
        resolved.ifBlank { media.effectivePlayableUrlOrPath }
    }

    suspend fun searchLiveOnlineCatalog(
        query: String,
        includeSongs: Boolean = true,
        includeVideos: Boolean = true,
        limit: Int = 80,
        offset: Int = 0
    ): Result<List<MediaItemEntity>> = withContext(Dispatchers.IO) {
        val result = catalogService.searchCatalog(
            query = query,
            includeSongs = includeSongs,
            includeVideos = includeVideos,
            limit = limit,
            offset = offset
        )
        result.map { liveItems ->
            val existingById = dao.getAllMediaOnce().associateBy { it.id }
            val merged = liveItems.map { fetched ->
                val existing = existingById[fetched.id]
                if (existing != null) {
                    val validLocalPath = existing.localFilePath.takeIf {
                        it.isNotBlank() && File(it).exists() && File(it).length() > 1024L
                    }.orEmpty()
                    fetched.copy(
                        isFavorite = existing.isFavorite,
                        inLibrary = existing.inLibrary,
                        localFilePath = validLocalPath,
                        lastPlayedTimestamp = existing.lastPlayedTimestamp,
                        lastPositionSec = existing.lastPositionSec,
                        playCount = existing.playCount,
                        popularityScore = maxOf(fetched.popularityScore, existing.popularityScore)
                    )
                } else {
                    fetched
                }
            }
            val sortedMerged = KhmerArtistDirectory.sortPopularSongsFirst(
                items = merged,
                queryHint = query,
                prioritizeSongsBeforeVideos = !(includeVideos && !includeSongs)
            )
            // Do not auto-insert raw online search results into Room so search songs never clutter Home or Library
            sortedMerged
        }
    }

    suspend fun resolveYouTubeOrDirectLink(
        rawInput: String,
        preferVideoStream: Boolean = false
    ): Result<MediaItemEntity> = withContext(Dispatchers.IO) {
        val result = catalogService.resolveYouTubeOrDirectLink(rawInput, preferVideoStream)
        result.onSuccess { resolved ->
            val existing = dao.getMediaById(resolved.id)
            val merged = if (existing != null) {
                val validLocalPath = existing.localFilePath.takeIf {
                    it.isNotBlank() && File(it).exists() && File(it).length() > 1024L
                }.orEmpty()
                resolved.copy(
                    isFavorite = existing.isFavorite,
                    localFilePath = validLocalPath,
                    lastPlayedTimestamp = existing.lastPlayedTimestamp,
                    lastPositionSec = existing.lastPositionSec,
                    playCount = existing.playCount,
                    popularityScore = maxOf(resolved.popularityScore, existing.popularityScore)
                )
            } else {
                resolved
            }
            dao.upsertMedia(merged)
        }
        result
    }

    suspend fun importMediaItem(item: MediaItemEntity) = withContext(Dispatchers.IO) {
        dao.upsertMedia(item.copy(inLibrary = true))
    }

    suspend fun removeMediaItem(mediaId: String) = withContext(Dispatchers.IO) {
        dao.deleteMediaById(mediaId)
    }

    suspend fun toggleFavorite(media: MediaItemEntity) = withContext(Dispatchers.IO) {
        val existing = dao.getMediaById(media.id)
        if (existing == null) {
            dao.upsertMedia(media.copy(isFavorite = true, inLibrary = true))
        } else {
            dao.setFavorite(media.id, !existing.isFavorite)
        }
    }

    suspend fun recordPlayback(mediaId: String, positionSec: Int) {
        dao.recordPlayback(mediaId, System.currentTimeMillis(), positionSec)
    }

    suspend fun updatePlaybackPosition(mediaId: String, positionSec: Int) {
        dao.updatePlaybackPosition(mediaId, positionSec)
    }

    suspend fun updateDuration(mediaId: String, durationSec: Int) {
        if (durationSec > 0) {
            dao.updateDuration(mediaId, durationSec)
        }
    }

    suspend fun saveCustomPlaylistCoverFromUri(uri: android.net.Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val coversDir = File(context.filesDir, "playlist_covers").apply { mkdirs() }
            val targetFile = File(coversDir, "playlist_cover_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (targetFile.exists() && targetFile.length() > 128L) {
                targetFile.absolutePath
            } else null
        }.getOrNull()
    }

    suspend fun createPlaylist(
        name: String,
        description: String,
        coverResName: String,
        coverUrl: String = "",
        initialTrackIds: List<String> = emptyList()
    ): Long {
        val accents = listOf(0xFF3B82F6L, 0xFF8B5CF6L, 0xFF10B981L, 0xFFF97316L)
        val playlist = PlaylistEntity(
            name = name.trim().ifEmpty { "Untitled Playlist" },
            description = description.trim().ifEmpty { "Curated in LUMO" },
            coverUrl = coverUrl,
            coverResName = coverResName,
            accentHex = accents[name.length % accents.size],
            trackIdsCsv = initialTrackIds.joinToString(",")
        )
        return dao.insertPlaylist(playlist)
    }

    suspend fun renamePlaylist(playlistId: Long, newName: String, newDescription: String) {
        val current = dao.getPlaylistById(playlistId) ?: return
        dao.updatePlaylist(
            current.copy(
                name = newName.trim().ifEmpty { current.name },
                description = newDescription.trim().ifEmpty { current.description }
            )
        )
    }

    suspend fun updatePlaylistCover(
        playlistId: Long,
        coverResName: String,
        coverUrl: String? = null
    ) {
        val current = dao.getPlaylistById(playlistId) ?: return
        dao.updatePlaylist(
            current.copy(
                coverResName = coverResName.ifBlank { current.coverResName },
                coverUrl = coverUrl ?: current.coverUrl
            )
        )
    }

    suspend fun deletePlaylist(playlistId: Long) {
        dao.deletePlaylist(playlistId)
    }

    suspend fun addTrackToPlaylist(playlistId: Long, media: MediaItemEntity) = withContext(Dispatchers.IO) {
        if (dao.getMediaById(media.id) == null) {
            dao.upsertMedia(media.copy(inLibrary = true))
        }
        addTrackToPlaylist(playlistId, media.id)
    }

    suspend fun addTrackToPlaylist(playlistId: Long, mediaId: String) {
        val current = dao.getPlaylistById(playlistId) ?: return
        val media = dao.getMediaById(mediaId)
        val updated = (current.trackIds + mediaId).distinct()
        val updatedCoverUrl = if (current.coverUrl.isBlank() && media != null && media.artworkUrl.isNotBlank()) {
            media.artworkUrl
        } else current.coverUrl
        dao.updatePlaylist(
            current.copy(
                trackIdsCsv = updated.joinToString(","),
                coverUrl = updatedCoverUrl
            )
        )
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, mediaId: String) {
        val current = dao.getPlaylistById(playlistId) ?: return
        val updated = current.trackIds.filterNot { it == mediaId }
        dao.updatePlaylist(current.copy(trackIdsCsv = updated.joinToString(",")))
    }

    suspend fun moveTrackInPlaylist(playlistId: Long, fromIndex: Int, toIndex: Int) {
        val current = dao.getPlaylistById(playlistId) ?: return
        val mutable = current.trackIds.toMutableList()
        if (fromIndex !in mutable.indices || toIndex !in mutable.indices) return
        val item = mutable.removeAt(fromIndex)
        mutable.add(toIndex, item)
        dao.updatePlaylist(current.copy(trackIdsCsv = mutable.joinToString(",")))
    }

    /**
     * Performs a real HTTP byte stream download of the authorized media stream URL to local storage,
     * automatically resolving fresh signed CDN URLs if the cached URL has expired.
     */
    fun startOrResumeDownload(
        media: MediaItemEntity,
        formatOption: DownloadFormatOption,
        onFinished: ((Boolean, String) -> Unit)? = null
    ) {
        if (!media.isAuthorizedDownload) return
        val taskId = "${media.id}_${formatOption.id}"
        if (activeDownloadJobs.containsKey(taskId)) return

        val job = externalScope.launch(Dispatchers.IO) {
            // Ensure the track is stored in Room so localFilePath and Library sync work for online search / link results
            if (dao.getMediaById(media.id) == null) {
                runCatching { dao.upsertMedia(media) }
            }

            val downloadsDir = File(context.filesDir, "lumo_downloads").apply { mkdirs() }
            val safeName = media.title
                .replace(Regex("[\\\\/:*?\"<>|\\s]+"), "_")
                .trim('_')
                .take(36)
                .ifBlank { "lumo_track" }
            val safeId = media.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(32)
            val targetFile = File(downloadsDir, "${safeName}_${safeId}.${formatOption.fileExtension}")

            // If already downloaded locally on disk and non-empty, copy or mark completed immediately
            val existingLocal = media.localFilePath.takeIf { it.isNotBlank() }?.let { File(it) }
            if (existingLocal != null && existingLocal.exists() && existingLocal.length() > 1024L) {
                if (existingLocal.absolutePath != targetFile.absolutePath) {
                    runCatching { existingLocal.copyTo(targetFile, overwrite = true) }
                }
                val finalSize = targetFile.length().coerceAtLeast(existingLocal.length())
                dao.upsertDownload(
                    DownloadTaskEntity(
                        id = taskId,
                        mediaId = media.id,
                        title = media.title,
                        artist = media.artist,
                        artworkUrl = media.artworkUrl,
                        artworkResName = media.artworkResName,
                        format = formatOption.id,
                        isVideo = formatOption.isVideo || media.isVideo,
                        totalBytes = finalSize,
                        downloadedBytes = finalSize,
                        progressPercent = 100,
                        status = "COMPLETED",
                        remoteUrl = media.streamUrl,
                        localFilePath = targetFile.absolutePath,
                        createdAt = System.currentTimeMillis()
                    )
                )
                dao.setLocalFilePath(media.id, targetFile.absolutePath)
                activeDownloadJobs.remove(taskId)
                onFinished?.invoke(true, "Saved \"${media.title}\" (${formatOption.label})")
                return@launch
            }

            val initialTask = DownloadTaskEntity(
                id = taskId,
                mediaId = media.id,
                title = media.title,
                artist = media.artist,
                artworkUrl = media.artworkUrl,
                artworkResName = media.artworkResName,
                format = formatOption.id,
                isVideo = formatOption.isVideo || media.isVideo,
                totalBytes = 0L,
                downloadedBytes = 0L,
                progressPercent = 2,
                status = "DOWNLOADING",
                remoteUrl = media.streamUrl,
                localFilePath = targetFile.absolutePath,
                createdAt = System.currentTimeMillis()
            )
            dao.upsertDownload(initialTask)

            var resolvedStreamUrl = catalogService.resolveBestDownloadUrl(media, formatOption)
            if (resolvedStreamUrl.isBlank()) {
                resolvedStreamUrl = catalogService.resolvePlayableStreamUrl(media, forceRefresh = true)
            }
            if (resolvedStreamUrl.isBlank()) {
                resolvedStreamUrl = catalogService.resolveFallbackDownloadUrlForTrack(media)
            }

            suspend fun executeHlsPlaylistDownload(playlistUrl: String): Boolean {
                val segmentUrls = catalogService.fetchHlsSegmentUrls(playlistUrl)
                if (segmentUrls.isEmpty()) return false
                var downloadedBytes = 0L
                var lastReportedPct = 2
                var lastReportTimeMs = 0L

                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(32 * 1024)
                    for ((index, segUrl) in segmentUrls.withIndex()) {
                        if (!isActive) return false
                        val segReq = Request.Builder().url(segUrl).get()
                        LumoYouTubeStreamAuth.getHeadersForUrl().forEach { (k, v) ->
                            segReq.header(k, v)
                        }
                        val segSuccess = runCatching {
                            downloadHttpClient.newCall(segReq.build()).execute().use { resp ->
                                if (!resp.isSuccessful) return@use false
                                val body = resp.body ?: return@use false
                                body.byteStream().use { input ->
                                    var read = 0
                                    while (isActive && input.read(buffer).also { read = it } != -1) {
                                        output.write(buffer, 0, read)
                                        downloadedBytes += read
                                    }
                                }
                                true
                            }
                        }.getOrDefault(false)

                        if (!segSuccess && index == 0) return false

                        val pct = (((index + 1) * 100) / segmentUrls.size).coerceIn(3, 99)
                        val now = System.currentTimeMillis()
                        if (pct - lastReportedPct >= 5 || now - lastReportTimeMs >= 250L) {
                            lastReportedPct = pct
                            lastReportTimeMs = now
                            val estimatedTotal = if (pct > 0) (downloadedBytes * 100L) / pct else downloadedBytes
                            dao.upsertDownload(
                                initialTask.copy(
                                    remoteUrl = playlistUrl,
                                    totalBytes = estimatedTotal.coerceAtLeast(downloadedBytes),
                                    downloadedBytes = downloadedBytes,
                                    progressPercent = pct,
                                    status = "DOWNLOADING"
                                )
                            )
                        }
                    }
                    output.flush()
                }

                if (isActive && targetFile.exists() && targetFile.length() > 1024L) {
                    val finalSize = targetFile.length()
                    dao.upsertDownload(
                        initialTask.copy(
                            remoteUrl = playlistUrl,
                            totalBytes = finalSize,
                            downloadedBytes = finalSize,
                            progressPercent = 100,
                            status = "COMPLETED"
                        )
                    )
                    dao.setLocalFilePath(media.id, targetFile.absolutePath)
                    return true
                }
                return false
            }

            suspend fun executeStreamDownload(rawUrlToFetch: String): Boolean {
                if (rawUrlToFetch.isBlank()) return false
                val urlToFetch = catalogService.resolveSoundCloudPointerIfNeeded(rawUrlToFetch)
                if (urlToFetch.isBlank()) return false

                if (urlToFetch.contains(".m3u8", ignoreCase = true)) {
                    return executeHlsPlaylistDownload(urlToFetch)
                }

                val reqBuilder = Request.Builder()
                    .url(urlToFetch)
                    .get()
                LumoYouTubeStreamAuth.getHeadersForUrl().forEach { (k, v) ->
                    reqBuilder.header(k, v)
                }

                downloadHttpClient.newCall(reqBuilder.build()).execute().use { response ->
                    if (!response.isSuccessful) {
                        return false
                    }
                    val contentType = response.header("Content-Type").orEmpty().lowercase()
                    if (contentType.contains("mpegurl") || contentType.contains("m3u8")) {
                        return executeHlsPlaylistDownload(urlToFetch)
                    }
                    if (contentType.contains("application/json")) {
                        // In case a JSON pointer was returned, resolve it
                        val jsonText = response.body?.string().orEmpty()
                        val innerUrl = runCatching {
                            org.json.JSONObject(jsonText).optString("url").trim()
                        }.getOrDefault("")
                        if (innerUrl.startsWith("http") && innerUrl != urlToFetch) {
                            return executeStreamDownload(innerUrl)
                        }
                        return false
                    }

                    val body = response.body ?: return false
                    val totalBytes = body.contentLength().coerceAtLeast(1L)
                    var downloadedBytes = 0L
                    var lastReportedPct = 2
                    var lastReportTimeMs = 0L

                    body.byteStream().use { input ->
                        FileOutputStream(targetFile).use { output ->
                            val buffer = ByteArray(32 * 1024)
                            var read = 0
                            while (isActive && input.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                                downloadedBytes += read
                                val pct = if (totalBytes > 1024L) {
                                    ((downloadedBytes * 100L) / totalBytes).toInt().coerceIn(2, 99)
                                } else {
                                    (15 + (downloadedBytes / (64 * 1024)).toInt()).coerceIn(5, 95)
                                }
                                val now = System.currentTimeMillis()
                                if (pct - lastReportedPct >= 6 && now - lastReportTimeMs >= 220L) {
                                    lastReportedPct = pct
                                    lastReportTimeMs = now
                                    dao.upsertDownload(
                                        initialTask.copy(
                                            remoteUrl = urlToFetch,
                                            totalBytes = if (totalBytes > 1024L) totalBytes else downloadedBytes,
                                            downloadedBytes = downloadedBytes,
                                            progressPercent = pct,
                                            status = "DOWNLOADING"
                                        )
                                    )
                                }
                            }
                            output.flush()
                        }
                    }

                    if (isActive && targetFile.exists() && targetFile.length() > 1024L) {
                        val finalSize = targetFile.length()
                        dao.upsertDownload(
                            initialTask.copy(
                                remoteUrl = urlToFetch,
                                totalBytes = finalSize,
                                downloadedBytes = finalSize,
                                progressPercent = 100,
                                status = "COMPLETED"
                            )
                        )
                        dao.setLocalFilePath(media.id, targetFile.absolutePath)
                        return true
                    }
                }
                return false
            }

            runCatching {
                var succeeded = runCatching { executeStreamDownload(resolvedStreamUrl) }.getOrDefault(false)
                if (!succeeded && isActive) {
                    // Refresh signed CDN token (preserving original streamUrl pointer) and retry
                    val freshRetryUrl = catalogService.resolvePlayableStreamUrl(media, forceRefresh = true)
                    if (freshRetryUrl.isNotBlank() && freshRetryUrl != resolvedStreamUrl) {
                        succeeded = runCatching { executeStreamDownload(freshRetryUrl) }.getOrDefault(false)
                    }
                }
                if (!succeeded && isActive) {
                    // Multi-source fallback across JioSaavn, SoundCloud, Audius, and iTunes
                    val fallbackUrl = catalogService.resolveFallbackDownloadUrlForTrack(media)
                    if (fallbackUrl.isNotBlank()) {
                        succeeded = runCatching { executeStreamDownload(fallbackUrl) }.getOrDefault(false)
                    }
                }
                if (succeeded && isActive) {
                    onFinished?.invoke(true, "Downloaded \"${media.title}\" (${formatOption.label}) — Ready offline")
                } else if (!succeeded && isActive) {
                    dao.upsertDownload(initialTask.copy(status = "PAUSED"))
                    onFinished?.invoke(false, "Download paused for \"${media.title}\" — Tap Resume to retry")
                }
            }.onFailure {
                dao.upsertDownload(initialTask.copy(status = "PAUSED"))
                onFinished?.invoke(false, "Download paused for \"${media.title}\" — Tap Resume to retry")
            }
            activeDownloadJobs.remove(taskId)
        }
        activeDownloadJobs[taskId] = job
    }

    fun pauseOrResumeExistingDownload(task: DownloadTaskEntity, media: MediaItemEntity?) {
        val runningJob = activeDownloadJobs.remove(task.id)
        if (runningJob != null) {
            runningJob.cancel()
            externalScope.launch(Dispatchers.IO) {
                dao.upsertDownload(task.copy(status = "PAUSED"))
            }
        } else {
            val formatOpt = DownloadFormatOption.entries.find { it.id == task.format }
                ?: DownloadFormatOption.M4A
            val resolvedMedia = media ?: MediaItemEntity(
                id = task.mediaId,
                title = task.title,
                artist = task.artist,
                album = "Downloaded Media",
                durationSec = 180,
                isVideo = task.isVideo,
                artworkUrl = task.artworkUrl,
                artworkResName = task.artworkResName,
                streamUrl = task.remoteUrl,
                localFilePath = task.localFilePath
            )
            startOrResumeDownload(resolvedMedia, formatOpt)
        }
    }

    suspend fun renameDownload(taskId: String, newTitle: String) {
        val trimmed = newTitle.trim()
        if (trimmed.isNotEmpty()) {
            dao.renameDownload(taskId, trimmed)
        }
    }

    suspend fun deleteDownload(task: DownloadTaskEntity) = withContext(Dispatchers.IO) {
        activeDownloadJobs.remove(task.id)?.cancel()
        runCatching { File(task.localFilePath).delete() }
        dao.deleteDownload(task.id)
    }

    suspend fun clearAllCompletedDownloads() = withContext(Dispatchers.IO) {
        val downloadsDir = File(context.filesDir, "lumo_downloads")
        downloadsDir.listFiles()?.forEach { runCatching { it.delete() } }
        dao.clearCompletedDownloads()
    }

    suspend fun cleanAppClutter(
        clearUnsavedSearchCache: Boolean = true,
        clearHistory: Boolean = false,
        clearIncompleteDownloads: Boolean = true
    ): Int = withContext(Dispatchers.IO) {
        var cleanedCount = 0
        if (clearUnsavedSearchCache) {
            cleanedCount += dao.deleteUnsavedSearchAndCatalogCache()
        }
        if (clearHistory) {
            dao.clearListeningHistory()
            cleanedCount += 1
        }
        if (clearIncompleteDownloads) {
            dao.deleteIncompleteDownloads()
            cleanedCount += 1
        }
        cleanedCount
    }

    suspend fun updateMediaMetadata(updated: MediaItemEntity) = withContext(Dispatchers.IO) {
        dao.upsertMedia(updated.copy(inLibrary = true))
    }

    suspend fun updateSettings(transform: (LumoSettingsEntity) -> LumoSettingsEntity) {
        val current = dao.getSettingsOnce() ?: LumoSettingsEntity()
        dao.upsertSettings(transform(current))
    }
}
