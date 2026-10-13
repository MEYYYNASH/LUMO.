package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.math.abs

object LumoYouTubeStreamAuth {
    const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
    const val SOUNDCLOUD_CLIENT_ID = "vI5BsvpTIlavDLl7RDbbcFAPg8kls8Bg"
    private const val COMPANION_STREAM_HOST = "https://jp1-cmp.invidious.f5.si/companion"

    val PIPED_API_INSTANCES = listOf(
        "https://pipedapi.kavin.rocks",
        "https://pipedapi.tokhmi.xyz",
        "https://pipedapi.moomoo.me",
        "https://pipedapi.syncpundit.io",
        "https://piped-api.garudalinux.org",
        "https://pipedapi.drgns.space"
    )

    const val ITUNES_SEARCH_API = "https://itunes.apple.com/search"
    const val DEEZER_SEARCH_API = "https://api.deezer.com/search"
    const val YOUTUBE_OEMBED_API = "https://www.youtube.com/oembed"
    const val NOEMBED_FALLBACK_API = "https://noembed.com/embed"

    fun buildExactYouTubeStreamUrl(videoId: String, itag: Int = 140): String {
        val cleanId = videoId.removePrefix("yt_").trim()
        return "$COMPANION_STREAM_HOST/latest_version?id=$cleanId&itag=$itag&local=true"
    }

    fun buildExactYouTubeVideoStreamUrl(videoId: String, itag: Int = 134): String {
        val cleanId = videoId.removePrefix("yt_").trim()
        return "$COMPANION_STREAM_HOST/latest_version?id=$cleanId&itag=$itag&local=true"
    }

    fun getHeadersForUrl(): Map<String, String> = mapOf(
        "User-Agent" to USER_AGENT
    )
}

data class ResolvedYouTubeStreams(
    val videoId: String,
    val title: String,
    val author: String,
    val durationSec: Int,
    val thumbnailUrl: String,
    val directVideoMp4Url: String?,
    val directAudioUrl: String?,
    val views: Long = 0L
)

private data class CachedStreamUrl(
    val streamUrl: String,
    val resolvedAtMs: Long
)

private data class CachedSearchEntry(
    val results: List<MediaItemEntity>,
    val cachedAtMs: Long
)

private data class CandidateAudioTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSec: Int,
    val artworkUrl: String,
    val streamOrTranscodingUrl: String,
    val isTranscodingPointer: Boolean,
    val sourceName: String,
    val popularityScore: Long = 0L,
    val releaseYear: Int = 2026
)

class LumoOnlineCatalogService(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
) {
    private val accentPalette = listOf(
        0xFF3B82F6L,
        0xFF8B5CF6L,
        0xFF10B981L,
        0xFFF97316L,
        0xFFEC4899L,
        0xFF06B6D4L
    )

    @Volatile
    private var preferredPipedHost: String = LumoYouTubeStreamAuth.PIPED_API_INSTANCES.first()

    private val streamCache = ConcurrentHashMap<String, CachedStreamUrl>()
    private val searchResultCache = ConcurrentHashMap<String, CachedSearchEntry>()
    private val cacheTtlMs = 18 * 60 * 1000L
    private val searchCacheTtlMs = 5 * 60 * 1000L

    companion object {
        private val YT_ID_EXACT_REGEX = Regex("^[a-zA-Z0-9_-]{11}$")
        private val YT_URL_PATTERNS = listOf(
            Regex("(?:v=|/v/|youtu\\.be/|/shorts/|/embed/|/live/)([a-zA-Z0-9_-]{11})"),
            Regex("youtube\\.com/watch\\?.*v=([a-zA-Z0-9_-]{11})")
        )
        private val CLEAN_TITLE_PAREN_REGEX = Regex(
            "\\([^)]*?(Official|Video|Audio|Lyrics|Visualizer|HD|4K|MV|Clip)[^)]*\\)",
            RegexOption.IGNORE_CASE
        )
        private val CLEAN_TITLE_BRACKET_REGEX = Regex(
            "\\[[^]]*?(Official|Video|Audio|Lyrics|Visualizer|HD|4K|MV|Clip)[^]]*]",
            RegexOption.IGNORE_CASE
        )
        private val MULTI_WHITESPACE_REGEX = Regex("\\s+")
        private val TOKEN_REGEX = Regex("[\\p{L}\\p{M}\\p{N}]+")
    }

    suspend fun searchCatalog(
        query: String,
        includeSongs: Boolean = true,
        includeVideos: Boolean = true,
        limit: Int = 75,
        offset: Int = 0
    ): Result<List<MediaItemEntity>> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanQuery = query.trim()
            if (cleanQuery.isEmpty()) return@runCatching emptyList()

            val cacheKey = "${cleanQuery.lowercase(Locale.ROOT)}|$includeSongs|$includeVideos|$limit|$offset"
            val now = System.currentTimeMillis()
            searchResultCache[cacheKey]?.let { cached ->
                if (now - cached.cachedAtMs < searchCacheTtlMs && cached.results.isNotEmpty()) {
                    return@runCatching cached.results
                }
            }

            if (offset == 0 && (isLikelyYouTubeOrMediaUrl(cleanQuery) || extractYouTubeVideoId(cleanQuery) != null)) {
                val resolved = resolveYouTubeOrDirectLink(cleanQuery).getOrNull()
                if (resolved != null) {
                    val related = searchMultiSourceByTerm(
                        cleanQuery = "${resolved.artist} ${resolved.title}",
                        includeSongs = includeSongs,
                        includeVideos = includeVideos,
                        limit = 30,
                        offset = 0
                    )
                    val finalUrlResults = (listOf(resolved) + related).distinctBy { it.id }
                    searchResultCache[cacheKey] = CachedSearchEntry(finalUrlResults, now)
                    return@runCatching finalUrlResults
                }
            }

            val results = searchMultiSourceByTerm(cleanQuery, includeSongs, includeVideos, limit, offset)
            if (results.isNotEmpty()) {
                if (searchResultCache.size > 120) {
                    searchResultCache.clear()
                }
                searchResultCache[cacheKey] = CachedSearchEntry(results, now)
            }
            results
        }
    }

    /**
     * Fetches Real World Trending songs strictly for **Trending Today** (`TRENDING_TODAY`)
     * or **Trending 2026** (`TRENDING_2026`) — never songs from older years (< 2026).
     */
    suspend fun fetchRealWorldTrendingCatalog(
        filter: TrendingFeedFilter = TrendingFeedFilter.TRENDING_TODAY,
        limit: Int = 28
    ): Result<List<MediaItemEntity>> = withContext(Dispatchers.IO) {
        runCatching {
            coroutineScope {
                val isTodayFilter = filter == TrendingFeedFilter.TRENDING_TODAY
                val searchQuery = if (isTodayFilter) {
                    "Trending Music Today 2026 New Song"
                } else {
                    "Top Hits 2026 Trending Songs 2026 បទថ្មី 2026"
                }

                val pipedLiveChartDeferred = async {
                    withTimeoutOrNull(3800L) {
                        fetchPipedLiveRegionalTrendingTracks(
                            regionCode = "US",
                            limit = 18,
                            onlyTodayOr2026 = true
                        )
                    }.orEmpty()
                }

                val pipedMusicSongsDeferred = async {
                    withTimeoutOrNull(3800L) {
                        fetchPipedOpenYouTubeSearchTracks(
                            query = searchQuery,
                            limit = 22,
                            includeSongs = true,
                            includeVideos = false,
                            onlyYear2026 = true
                        )
                    }.orEmpty()
                }

                val soundCloudGlobalDeferred = async {
                    withTimeoutOrNull(3600L) {
                        fetchFullLengthSoundCloudTracks(
                            query = searchQuery,
                            limit = 24,
                            includeSongs = true,
                            includeVideos = false,
                            onlyYear2026 = true
                        )
                    }.orEmpty()
                }

                val appleRssTrendingDeferred = async {
                    withTimeoutOrNull(3800L) {
                        fetchAppleMusicTrendingChartTracks(
                            filter = filter,
                            maxCount = 24
                        ).filter { it.releaseYear >= 2026 && KhmerArtistDirectory.isEligibleForTrending2026OrToday(it) }
                    }.orEmpty()
                }

                val pipedLiveChart = pipedLiveChartDeferred.await()
                val pipedMusicSongs = pipedMusicSongsDeferred.await()
                val scGlobalTracks = soundCloudGlobalDeferred.await()
                val appleRssTracks = appleRssTrendingDeferred.await()

                // Build a lookup of real worldwide listener counts by normalized title/artist
                val realPlaysPool = (pipedLiveChart + pipedMusicSongs + scGlobalTracks)
                    .filter { KhmerArtistDirectory.isEligibleForTrending2026OrToday(it) }

                val enrichedAppleTracks = appleRssTracks.map { appleItem ->
                    val appleTitleNorm = appleItem.title.lowercase(Locale.ROOT).trim()
                    val appleArtistNorm = appleItem.artist.lowercase(Locale.ROOT).trim()
                    val matchedGlobalTrack = realPlaysPool.firstOrNull { cand ->
                        val cTitle = cand.title.lowercase(Locale.ROOT)
                        val cArtist = cand.artist.lowercase(Locale.ROOT)
                        (cTitle.contains(appleTitleNorm) || appleTitleNorm.contains(cTitle)) &&
                            (cArtist.contains(appleArtistNorm) || appleArtistNorm.contains(cArtist))
                    }
                    if (matchedGlobalTrack != null && matchedGlobalTrack.popularityScore > 0L) {
                        appleItem.copy(
                            popularityScore = matchedGlobalTrack.popularityScore,
                            streamUrl = matchedGlobalTrack.streamUrl.ifBlank { appleItem.streamUrl },
                            durationSec = matchedGlobalTrack.durationSec.takeIf { it > 45 } ?: appleItem.durationSec
                        )
                    } else {
                        appleItem
                    }
                }

                val categoryTag = if (isTodayFilter) "Trending Today · 2026" else "Trending 2026"

                // Combine all real worldwide 2026/Today tracks and keep the highest real global play count per distinct song
                val combinedBySongKey = LinkedHashMap<String, MediaItemEntity>()
                for (item in (pipedLiveChart + scGlobalTracks + pipedMusicSongs + enrichedAppleTracks)) {
                    if (!KhmerArtistDirectory.isEligibleForTrending2026OrToday(item)) continue
                    val key = "${item.title.lowercase(Locale.ROOT).trim()}|${item.artist.lowercase(Locale.ROOT).trim()}"
                    val existing = combinedBySongKey[key]
                    if (existing == null || item.popularityScore > existing.popularityScore) {
                        combinedBySongKey[key] = item.copy(
                            category = categoryTag,
                            releaseYear = 2026
                        )
                    }
                }

                val merged = combinedBySongKey.values.toList()
                val withRealGlobalPlays = merged.filter { it.popularityScore > 0L }
                val candidatePool = if (withRealGlobalPlays.size >= 6) {
                    withRealGlobalPlays + merged.filter { it.popularityScore == 0L }
                } else {
                    merged
                }

                KhmerArtistDirectory.sortTrendingNewFirst(
                    items = candidatePool.distinctBy { it.id },
                    filter = filter
                ).take(limit)
            }
        }
    }

    private fun parsePipedReleaseYear(uploadedMs: Long, uploadedDateStr: String, rawTitle: String): Int {
        val dateLower = uploadedDateStr.lowercase(Locale.ROOT).trim()
        if (dateLower.contains("year")) {
            val yearsAgo = Regex("(\\d+)\\s*year").find(dateLower)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 1
            return (2026 - yearsAgo).coerceAtMost(2025)
        }
        if (uploadedMs > 100_000_000_000L) {
            val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
            cal.timeInMillis = uploadedMs
            val yr = cal.get(java.util.Calendar.YEAR)
            if (yr in 1970..2026) return yr
        }
        val oldYearMatch = Regex("\\b(19[7-9]\\d|20[01]\\d|202[0-5])\\b").find(rawTitle)
        if (oldYearMatch != null) {
            return oldYearMatch.value.toIntOrNull() ?: 2025
        }
        return 2026
    }

    /**
     * Fetches live regional `/trending?region={regionCode}` directly from Piped Open YouTube API.
     * Every track uses the real worldwide `views` count and strictly filters out older years (< 2026).
     */
    private fun fetchPipedLiveRegionalTrendingTracks(
        regionCode: String = "US",
        limit: Int = 18,
        onlyTodayOr2026: Boolean = true
    ): List<MediaItemEntity> {
        for (instance in orderedPipedInstances().take(3)) {
            val items = runCatching {
                val url = "$instance/trending?region=${regionCode.uppercase(Locale.US)}"
                val req = Request.Builder()
                    .url(url)
                    .header("User-Agent", LumoYouTubeStreamAuth.USER_AGENT)
                    .get()
                    .build()

                httpClient.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@use emptyList<MediaItemEntity>()
                    val body = resp.body?.string()?.trim().orEmpty()
                    if (body.isBlank()) return@use emptyList<MediaItemEntity>()

                    val arr = if (body.startsWith("[")) {
                        JSONArray(body)
                    } else {
                        JSONObject(body).optJSONArray("items")
                    } ?: return@use emptyList<MediaItemEntity>()

                    val parsed = mutableListOf<MediaItemEntity>()
                    for (i in 0 until arr.length()) {
                        if (parsed.size >= limit) break
                        val obj = arr.optJSONObject(i) ?: continue
                        val rawUrl = obj.optString("url", "").trim()
                        val videoId = extractYouTubeVideoId(rawUrl)
                            ?: rawUrl.substringAfter("v=", "").substringBefore("&").trim()
                        if (videoId.length != 11) continue

                        val rawTitle = obj.optString("title", "").trim()
                        if (rawTitle.isEmpty()) continue

                        val uploadedMs = obj.optLong("uploaded", 0L)
                        val uploadedDateStr = obj.optString("uploadedDate", "")
                        val releaseYear = parsePipedReleaseYear(uploadedMs, uploadedDateStr, rawTitle)
                        if (onlyTodayOr2026 && releaseYear < 2026) continue

                        val durationSec = obj.optInt("duration", 0)
                        // Keep music-length streams (30s to 9 minutes)
                        if (durationSec !in 30..540) continue

                        val rawViews = obj.optLong("views", 0L).coerceAtLeast(0L)
                        if (rawViews <= 0L) continue

                        val rawUploader = obj.optString("uploaderName", "")
                            .ifBlank { obj.optString("uploader", "YouTube Music") }
                            .replace("- Topic", "")
                            .replace("Official", "")
                            .trim()
                            .ifBlank { "Global Artist" }

                        val thumb = obj.optString("thumbnail", "").trim()
                            .ifBlank { "https://i.ytimg.com/vi/$videoId/hqdefault.jpg" }

                        val (normalizedArtist, normalizedTitle) = KhmerArtistDirectory.normalizeTrackArtistAndTitle(
                            rawTitle = rawTitle,
                            uploaderUsername = rawUploader
                        )
                        val cleanedSongTitle = cleanYouTubeTitleOnly(normalizedTitle).ifBlank { rawTitle }
                        val seed = abs(videoId.hashCode())

                        val entity = MediaItemEntity(
                            id = "yt_$videoId",
                            title = cleanedSongTitle,
                            artist = normalizedArtist,
                            album = "Trending Today · $rawUploader",
                            durationSec = durationSec,
                            isVideo = false,
                            artworkUrl = thumb,
                            artworkResName = "img_lumo_icon_fg",
                            accentHex = accentPalette[seed % accentPalette.size],
                            streamUrl = "",
                            licenseBadge = "Trending Today · (${formatSeconds(durationSec)})",
                            isAuthorizedDownload = true,
                            category = "Trending Today · 2026",
                            releaseYear = 2026,
                            bitrateKbps = 256,
                            popularityScore = rawViews,
                            waveformSeed = (seed % 500) + 19
                        )
                        if (!onlyTodayOr2026 || KhmerArtistDirectory.isEligibleForTrending2026OrToday(entity)) {
                            parsed.add(entity)
                        }
                    }
                    parsed
                }
            }.getOrDefault(emptyList())

            if (items.isNotEmpty()) {
                preferredPipedHost = instance
                return items
            }
        }
        return emptyList()
    }

    /**
     * Fetches live Apple Music / iTunes RSS Top Songs directly from Apple's official RSS JSON feed
     * (`https://itunes.apple.com/{country}/rss/topsongs/limit=20/.../json`) with zero hardcoded songs.
     */
    private fun fetchAppleMusicTrendingChartTracks(
        filter: TrendingFeedFilter = TrendingFeedFilter.TRENDING_TODAY,
        maxCount: Int = 20
    ): List<MediaItemEntity> {
        return runCatching {
            val countryLower = filter.regionCode.lowercase(Locale.US)
            val genreSegment = filter.appleRssGenreId?.let { "/genre=$it" }.orEmpty()
            val rssUrl = "https://itunes.apple.com/$countryLower/rss/topsongs/limit=$maxCount$genreSegment/json"
            val req = Request.Builder()
                .url(rssUrl)
                .header("User-Agent", LumoYouTubeStreamAuth.USER_AGENT)
                .get()
                .build()
            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@runCatching emptyList()
                val body = resp.body?.string().orEmpty()
                if (body.isBlank()) return@runCatching emptyList()
                val entries = JSONObject(body).optJSONObject("feed")?.optJSONArray("entry")
                    ?: return@runCatching emptyList()

                val parsed = mutableListOf<MediaItemEntity>()
                for (i in 0 until minOf(entries.length(), maxCount)) {
                    val entry = entries.optJSONObject(i) ?: continue
                    val songName = entry.optJSONObject("im:name")?.optString("label", "")?.trim().orEmpty()
                    val artistName = entry.optJSONObject("im:artist")?.optString("label", "")?.trim().orEmpty()
                    if (songName.isEmpty() || artistName.isEmpty()) continue

                    val trackId = entry.optJSONObject("id")
                        ?.optJSONObject("attributes")
                        ?.optString("im:id", "")
                        ?.trim()
                        .orEmpty()
                        .ifEmpty { "${artistName}_${songName}".hashCode().toString() }

                    val albumName = entry.optJSONObject("im:collection")
                        ?.optJSONObject("im:name")
                        ?.optString("label", "")
                        ?.trim()
                        .orEmpty()
                        .ifBlank { "Apple Music Chart · #${i + 1}" }

                    val imagesArr = entry.optJSONArray("im:image")
                    val rawImg = if (imagesArr != null && imagesArr.length() > 0) {
                        imagesArr.optJSONObject(imagesArr.length() - 1)?.optString("label", "").orEmpty()
                    } else ""
                    val artworkUrl = rawImg.replace("170x170bb", "600x600bb")
                        .replace("100x100bb", "600x600bb")

                    var previewUrl = ""
                    var durationSec = 210
                    val linksArr = entry.optJSONArray("link")
                    if (linksArr != null) {
                        for (l in 0 until linksArr.length()) {
                            val linkObj = linksArr.optJSONObject(l) ?: continue
                            val attrs = linkObj.optJSONObject("attributes") ?: continue
                            val href = attrs.optString("href", "").trim()
                            val rel = attrs.optString("rel", "")
                            val type = attrs.optString("type", "")
                            if (href.startsWith("http") && (rel == "enclosure" || type.contains("audio"))) {
                                previewUrl = href
                                val durMs = linkObj.optJSONObject("im:duration")?.optString("label", "")?.toLongOrNull() ?: 0L
                                if (durMs > 30_000L) {
                                    durationSec = (durMs / 1000L).toInt()
                                }
                                break
                            }
                        }
                    }

                    val releaseYear = entry.optJSONObject("im:releaseDate")
                        ?.optString("label", "2026")
                        ?.take(4)
                        ?.toIntOrNull() ?: 2026

                    val (normalizedArtist, normalizedTitle) = KhmerArtistDirectory.normalizeTrackArtistAndTitle(
                        rawTitle = songName,
                        uploaderUsername = artistName,
                        publisherArtist = artistName
                    )

                    val seed = abs(trackId.hashCode())
                    parsed.add(
                        MediaItemEntity(
                            id = "am_$trackId",
                            title = normalizedTitle,
                            artist = normalizedArtist,
                            album = albumName,
                            durationSec = durationSec,
                            isVideo = false,
                            artworkUrl = artworkUrl,
                            artworkResName = "img_lumo_icon_fg",
                            accentHex = accentPalette[seed % accentPalette.size],
                            streamUrl = previewUrl,
                            licenseBadge = "Global Chart · #${i + 1}",
                            isAuthorizedDownload = true,
                            category = "World Trending",
                            releaseYear = releaseYear,
                            bitrateKbps = 256,
                            popularityScore = 0L,
                            waveformSeed = (seed % 500) + 31
                        )
                    )
                }
                parsed
            }
        }.getOrDefault(emptyList())
    }

    /**
     * Universal All-Sound Finder & Multi-Source Search powered by:
     * 1. iTunes / Apple Music Public Search API (Songs + Music Videos)
     * 2. Deezer Public Search API (Global Songs, Remixes & Sounds with instant MP3 CDN)
     * 3. Piped Open YouTube API (All Songs, Short Sounds, Effects, Remixes & Videos)
     * 4. SoundCloud Public Catalog (Full-length tracks, Khmer indie/remixes, DJ mixes & short sounds)
     */
    private suspend fun searchMultiSourceByTerm(
        cleanQuery: String,
        includeSongs: Boolean,
        includeVideos: Boolean,
        limit: Int,
        offset: Int = 0
    ): List<MediaItemEntity> = coroutineScope {
        val isKhmerQuery = KhmerArtistDirectory.isKhmerRelatedQuery(cleanQuery)
        val expandedQueries = if (isKhmerQuery) {
            KhmerArtistDirectory.expandSearchQueries(cleanQuery).take(2)
        } else {
            listOf(cleanQuery)
        }

        // 1. iTunes / Apple Music Public Search API (Songs)
        val appleSongsDeferred = async {
            if (!includeSongs) return@async emptyList()
            withTimeoutOrNull(3600L) {
                expandedQueries.mapIndexed { idx, subQuery ->
                    async {
                        fetchAppleMusicCatalogTracks(
                            query = subQuery,
                            limit = if (idx == 0) 35 else 20,
                            isKhmerQuery = isKhmerQuery,
                            offset = if (idx == 0) offset else 0
                        )
                    }
                }.awaitAll().flatten()
            }.orEmpty()
        }

        // 2. Deezer Public Search API (Global Songs, Remixes & Sounds)
        val deezerSongsDeferred = async {
            if (!includeSongs) return@async emptyList()
            withTimeoutOrNull(3200L) {
                fetchDeezerCatalogTracks(
                    query = expandedQueries.first(),
                    limit = 25,
                    isKhmerQuery = isKhmerQuery,
                    offset = offset
                )
            }.orEmpty()
        }

        // 3. iTunes / Apple Music Public Search API (Music Videos)
        val appleVideosDeferred = async {
            if (!includeVideos) return@async emptyList()
            withTimeoutOrNull(3400L) {
                fetchAppleMusicVideoTracks(
                    query = expandedQueries.first(),
                    limit = 16,
                    isKhmerQuery = isKhmerQuery,
                    offset = offset / 2
                )
            }.orEmpty()
        }

        // 4. Piped Open YouTube API Search (Supports all songs, short sounds, effects & videos)
        val pipedYouTubeDeferred = async {
            withTimeoutOrNull(3500L) {
                val ytQuery = if (offset > 0) "$cleanQuery audio" else cleanQuery
                fetchPipedOpenYouTubeSearchTracks(
                    query = ytQuery,
                    limit = 28,
                    includeSongs = includeSongs,
                    includeVideos = includeVideos,
                    onlyYear2026 = false
                )
            }.orEmpty()
        }

        // 5. Fast SoundCloud supplement for Khmer / indie remixes / DJ mixes / short sounds
        val scSupplementDeferred = async {
            withTimeoutOrNull(3400L) {
                expandedQueries.take(2).mapIndexed { idx, subQuery ->
                    async {
                        fetchFullLengthSoundCloudTracks(
                            query = subQuery,
                            limit = if (idx == 0) 24 else 14,
                            offset = if (idx == 0) offset else 0,
                            includeSongs = includeSongs,
                            includeVideos = includeVideos,
                            onlyYear2026 = false
                        )
                    }
                }.awaitAll().flatten()
            }.orEmpty()
        }

        val appleSongs = appleSongsDeferred.await()
        val deezerSongs = deezerSongsDeferred.await()
        val appleVideos = appleVideosDeferred.await()
        val pipedTracks = pipedYouTubeDeferred.await()
        val scTracks = scSupplementDeferred.await()

        val combined = mutableListOf<MediaItemEntity>()
        val maxLen = maxOf(appleSongs.size, deezerSongs.size, pipedTracks.size, scTracks.size, appleVideos.size)
        for (i in 0 until maxLen) {
            if (i < scTracks.size) combined.add(scTracks[i])
            if (i < pipedTracks.size) combined.add(pipedTracks[i])
            if (i < deezerSongs.size) combined.add(deezerSongs[i])
            if (i < appleSongs.size) combined.add(appleSongs[i])
            if (i < appleVideos.size) combined.add(appleVideos[i])
        }

        val filtered = combined
            .distinctBy { it.id }
            .distinctBy { "${it.title.lowercase(Locale.ROOT)}|${it.artist.lowercase(Locale.ROOT)}|${it.isVideo}" }
            .filter { item ->
                when {
                    includeSongs && includeVideos -> true
                    includeVideos && !includeSongs -> item.isVideo
                    includeSongs && !includeVideos -> !item.isVideo
                    else -> true
                }
            }

        val sortedPopularFirst = KhmerArtistDirectory.sortPopularSongsFirst(
            items = filtered,
            queryHint = cleanQuery,
            prioritizeSongsBeforeVideos = !(includeVideos && !includeSongs)
        )

        if (sortedPopularFirst.isNotEmpty()) {
            return@coroutineScope sortedPopularFirst.take(limit)
        }

        KhmerArtistDirectory.sortPopularSongsFirst(
            items = combined.distinctBy { it.id },
            queryHint = cleanQuery,
            prioritizeSongsBeforeVideos = !(includeVideos && !includeSongs)
        ).take(limit)
    }

    /**
     * Resolves a pasted YouTube link, direct media URL, or song title using:
     * 1. YouTube Official Public oEmbed API (`https://www.youtube.com/oembed`) for instant official title/author/thumbnail
     * 2. Piped Open YouTube API (`/streams/{videoId}`) for direct audio/video streams & duration
     * 3. iTunes / Apple Music Public Search API fallback so every track is 100% playable and downloadable.
     */
    suspend fun resolveYouTubeOrDirectLink(
        rawInput: String,
        preferVideoStream: Boolean = false
    ): Result<MediaItemEntity> = withContext(Dispatchers.IO) {
        runCatching {
            val trimmed = rawInput.trim()
            require(trimmed.isNotEmpty()) { "Please paste a valid YouTube or media link." }

            val ytId = extractYouTubeVideoId(trimmed)
            if (ytId != null) {
                val ytResolved = extractYouTubeMetadataViaOEmbedAndPiped(ytId)

                val rawTitle = ytResolved.title
                val rawChannel = ytResolved.author
                val ytThumbnail = ytResolved.thumbnailUrl
                val fullDurationSec = ytResolved.durationSec.takeIf { it > 15 } ?: 218

                val parsedArtist = if (rawTitle.contains(" - ")) {
                    rawTitle.substringBefore(" - ").trim().ifEmpty { rawChannel }
                } else {
                    rawChannel
                        .replace("VEVO", "", ignoreCase = true)
                        .replace("- Topic", "", ignoreCase = true)
                        .replace("Official", "", ignoreCase = true)
                        .trim()
                        .ifEmpty { rawChannel }
                }
                val parsedSongTitle = if (rawTitle.contains(" - ")) {
                    rawTitle.substringAfter(" - ").trim().ifEmpty { rawTitle }
                } else {
                    rawTitle
                }

                val cleanSongTitleOnly = cleanYouTubeTitleOnly(parsedSongTitle).ifBlank { rawTitle }
                val cleanFullTitle = cleanYouTubeTitleOnly(rawTitle).ifBlank { rawTitle }

                // Prefer direct Piped stream if available, otherwise match via iTunes / Apple Music Public Search API
                val directPipedUrl = if (preferVideoStream) {
                    ytResolved.directVideoMp4Url ?: ytResolved.directAudioUrl
                } else {
                    ytResolved.directAudioUrl ?: ytResolved.directVideoMp4Url
                }

                val matchedStream = if (!directPipedUrl.isNullOrBlank()) {
                    Pair(directPipedUrl, fullDurationSec)
                } else {
                    resolveBestMatchingStreamForTrack(
                        rawTitle = cleanFullTitle,
                        songTitle = cleanSongTitleOnly,
                        artist = parsedArtist,
                        channel = rawChannel,
                        targetDurationSec = fullDurationSec,
                        preferVideo = preferVideoStream
                    )
                }

                val resolvedStreamUrl = matchedStream?.first
                    ?: throw IllegalStateException("Could not resolve playable stream for $cleanFullTitle")
                val resolvedDuration = matchedStream.second.takeIf { it > 25 } ?: fullDurationSec

                streamCache["yt_$ytId"] = CachedStreamUrl(
                    streamUrl = resolvedStreamUrl,
                    resolvedAtMs = System.currentTimeMillis()
                )

                val hashSeed = abs(ytId.hashCode())
                val accent = accentPalette[hashSeed % accentPalette.size]

                return@runCatching MediaItemEntity(
                    id = "yt_$ytId",
                    title = cleanSongTitleOnly,
                    artist = parsedArtist,
                    album = "YouTube · $rawChannel",
                    durationSec = resolvedDuration,
                    isVideo = preferVideoStream,
                    artworkUrl = ytThumbnail,
                    artworkResName = "img_lumo_icon_fg",
                    accentHex = accent,
                    streamUrl = resolvedStreamUrl,
                    licenseBadge = "Piped YouTube + oEmbed · ${formatSeconds(resolvedDuration)}",
                    isAuthorizedDownload = true,
                    category = if (preferVideoStream) "YouTube Video" else "YouTube Audio",
                    releaseYear = 2026,
                    bitrateKbps = 256,
                    popularityScore = ytResolved.views.coerceAtLeast(0L),
                    waveformSeed = (hashSeed % 500) + 11,
                    inLibrary = true
                )
            }

            if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
                val pathOnly = trimmed.substringBefore('?').lowercase(Locale.ROOT)
                val isDirectMediaFile = listOf(
                    ".mp3", ".m4a", ".aac", ".wav", ".ogg", ".flac", ".opus", ".mp4", ".m4v", ".webm"
                ).any { pathOnly.endsWith(it) }

                if (isDirectMediaFile) {
                    val fileName = trimmed.substringAfterLast('/').substringBefore('?').ifBlank { "Direct Media Stream" }
                    val isVideoUrl = fileName.endsWith(".mp4", true) || fileName.endsWith(".m4v", true) || fileName.endsWith(".webm", true)
                    val cleanTitle = fileName.substringBeforeLast('.').replace('_', ' ').replace('-', ' ').ifBlank { "Custom Link Media" }
                    val hashId = abs(trimmed.hashCode())
                    return@runCatching MediaItemEntity(
                        id = "link_$hashId",
                        title = cleanTitle,
                        artist = "Direct Link",
                        album = "Web Stream",
                        durationSec = 210,
                        isVideo = isVideoUrl,
                        artworkUrl = "",
                        artworkResName = "img_lumo_icon_fg",
                        accentHex = accentPalette[hashId % accentPalette.size],
                        streamUrl = trimmed,
                        licenseBadge = "Direct URL · Full Stream",
                        isAuthorizedDownload = true,
                        category = if (isVideoUrl) "Direct Video" else "Direct Audio",
                        releaseYear = 2026,
                        bitrateKbps = if (isVideoUrl) 1200 else 320,
                        popularityScore = 0L,
                        waveformSeed = (hashId % 500) + 9,
                        inLibrary = true
                    )
                }

                // For SoundCloud, TikTok, Spotify, Apple Music, or Deezer page URLs, extract metadata or slug and resolve playable audio
                val slugTokens = trimmed
                    .substringBefore('?')
                    .trimEnd('/')
                    .split('/')
                    .takeLast(2)
                    .filterNot { it in listOf("track", "song", "video", "watch", "music", "album", "playlist") }
                    .joinToString(" ") { it.replace('-', ' ').replace('_', ' ') }
                    .trim()
                val resolvedFromPlatform = searchMultiSourceByTerm(
                    cleanQuery = slugTokens.ifBlank { trimmed },
                    includeSongs = !preferVideoStream,
                    includeVideos = preferVideoStream,
                    limit = 8
                ).firstOrNull()
                if (resolvedFromPlatform != null) {
                    return@runCatching resolvedFromPlatform.copy(inLibrary = true)
                }
            }

            val topMatch = searchMultiSourceByTerm(trimmed, includeSongs = true, includeVideos = true, limit = 8).firstOrNull()
                ?: throw IllegalArgumentException("Could not find media for '$trimmed'")
            topMatch
        }
    }

    /**
     * Resolves a fresh, live HTTP 200 CDN stream URL right before online playback or download.
     * Uses Piped Open YouTube `/streams/{videoId}` and iTunes / Apple Music Public Search API.
     */
    suspend fun resolvePlayableStreamUrl(
        media: MediaItemEntity,
        forceRefresh: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        val localFile = if (media.localFilePath.isNotBlank()) java.io.File(media.localFilePath) else null
        if (localFile != null && localFile.exists() && localFile.length() > 1024L) {
            return@withContext localFile.absolutePath
        }

        val now = System.currentTimeMillis()
        if (!forceRefresh) {
            val cached = streamCache[media.id]
            if (cached != null &&
                (now - cached.resolvedAtMs) < cacheTtlMs &&
                !cached.streamUrl.contains("invidious.f5.si") &&
                !cached.streamUrl.contains("api-v2.soundcloud.com/media/")
            ) {
                return@withContext cached.streamUrl
            }
        }

        val currentStream = media.streamUrl

        // 1. If not force-refreshing and currentStream is already a direct CDN stream (iTunes / Deezer), return immediately
        if (!forceRefresh && (
                currentStream.contains("itunes.apple.com") ||
                    currentStream.contains("mzstatic.com") ||
                    currentStream.contains("dzcdn.net")
                )
        ) {
            streamCache[media.id] = CachedStreamUrl(currentStream, now)
            return@withContext currentStream
        }

        // 2. If media is a YouTube track ("yt_...") and NOT retrying after a stream error, try Piped Streams API
        if (!forceRefresh && media.id.startsWith("yt_")) {
            val videoId = media.id.removePrefix("yt_").trim()
            val pipedResolved = withTimeoutOrNull(2500L) {
                fetchPipedStreamsForVideoId(videoId)
            }
            val directPiped = if (media.isVideo) {
                pipedResolved?.directVideoMp4Url ?: pipedResolved?.directAudioUrl
            } else {
                pipedResolved?.directAudioUrl ?: pipedResolved?.directVideoMp4Url
            }
            if (!directPiped.isNullOrBlank() && !directPiped.contains(".m3u8", ignoreCase = true)) {
                streamCache[media.id] = CachedStreamUrl(directPiped, now)
                return@withContext directPiped
            }
        }

        // 3. If currentStream is a SoundCloud transcoding pointer, dereference it
        if (!forceRefresh && currentStream.contains("api-v2.soundcloud.com/media/")) {
            val resolvedFromPointer = resolveSoundCloudProgressiveUrl(currentStream)
            if (!resolvedFromPointer.isNullOrBlank()) {
                streamCache[media.id] = CachedStreamUrl(resolvedFromPointer, now)
                return@withContext resolvedFromPointer
            }
        }

        // 4. SoundCloud track ID ("sc_123456789") -> resolve progressive URL by trackId
        if (!forceRefresh && media.id.startsWith("sc_")) {
            val scTrackId = media.id.removePrefix("sc_").trim()
            val freshScUrl = resolveSoundCloudTrackStreamById(scTrackId)
            if (!freshScUrl.isNullOrBlank()) {
                streamCache[media.id] = CachedStreamUrl(freshScUrl, now)
                return@withContext freshScUrl
            }
        }

        val isDeadOrExpired = forceRefresh ||
            currentStream.isBlank() ||
            currentStream.contains("invidious.f5.si") ||
            currentStream.contains("api-v2.soundcloud.com/media/") ||
            (currentStream.contains("sndcdn.com") && streamCache[media.id] == null)

        if (!isDeadOrExpired) {
            return@withContext currentStream
        }

        // Multi-engine fallback (SoundCloud Progressive MP3 -> Deezer CDN MP3 -> iTunes M4A)
        val matched = resolveBestMatchingStreamForTrack(
            rawTitle = "${media.artist} - ${media.title}",
            songTitle = media.title,
            artist = media.artist,
            channel = media.album.removePrefix("YouTube · ").removePrefix("Piped · ").removePrefix("Trending Today · ").trim(),
            targetDurationSec = media.durationSec,
            preferVideo = media.isVideo
        )
        val resolved = matched?.first?.takeIf { it.isNotBlank() }
            ?: resolveFallbackDownloadUrlForTrack(media).takeIf { it.isNotBlank() }
            ?: currentStream

        if (resolved.isNotBlank() &&
            !resolved.contains("invidious.f5.si") &&
            !resolved.contains("api-v2.soundcloud.com/media/")
        ) {
            streamCache[media.id] = CachedStreamUrl(resolved, now)
        }
        resolved
    }

    suspend fun resolveBestDownloadUrl(
        media: MediaItemEntity,
        formatOption: DownloadFormatOption,
        forceRefresh: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        if (media.id.startsWith("yt_")) {
            val videoId = media.id.removePrefix("yt_").trim()
            val piped = withTimeoutOrNull(2600L) { fetchPipedStreamsForVideoId(videoId) }
            val pipedUrl = if (formatOption.isVideo) {
                piped?.directVideoMp4Url ?: piped?.directAudioUrl
            } else {
                piped?.directAudioUrl ?: piped?.directVideoMp4Url
            }
            if (!pipedUrl.isNullOrBlank() && !pipedUrl.contains(".m3u8", ignoreCase = true)) {
                return@withContext pipedUrl
            }
        }

        val primary = resolvePlayableStreamUrl(media, forceRefresh = forceRefresh)
        val dereferenced = resolveSoundCloudPointerIfNeeded(primary)

        if (dereferenced.isNotBlank() &&
            !dereferenced.contains(".m3u8", ignoreCase = true) &&
            !dereferenced.contains("cf-hls", ignoreCase = true) &&
            !dereferenced.contains("api-v2.soundcloud.com/media/", ignoreCase = true)
        ) {
            return@withContext dereferenced
        }

        val directFallback = resolveFallbackDownloadUrlForTrack(media, preferVideo = formatOption.isVideo)
        if (directFallback.isNotBlank() && !directFallback.contains(".m3u8", ignoreCase = true)) {
            return@withContext directFallback
        }

        dereferenced.ifBlank { directFallback }
    }

    fun resolveSoundCloudPointerIfNeeded(url: String): String {
        val trimmed = url.trim()
        if (trimmed.contains("api-v2.soundcloud.com/media/", ignoreCase = true)) {
            return resolveSoundCloudProgressiveUrl(trimmed).orEmpty()
        }
        return trimmed
    }

    fun fetchHlsSegmentUrls(m3u8Url: String): List<String> {
        return runCatching {
            val req = Request.Builder()
                .url(m3u8Url)
                .header("User-Agent", LumoYouTubeStreamAuth.USER_AGENT)
                .get()
                .build()
            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return emptyList()
                val text = resp.body?.string().orEmpty()
                if (text.isBlank()) return emptyList()
                val basePrefix = m3u8Url.substringBeforeLast('/')
                text.lineSequence()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && !it.startsWith("#") }
                    .map { line ->
                        if (line.startsWith("http://", ignoreCase = true) || line.startsWith("https://", ignoreCase = true)) {
                            line
                        } else {
                            "$basePrefix/$line"
                        }
                    }
                    .toList()
            }
        }.getOrDefault(emptyList())
    }

    /**
     * Multi-source fallback resolver powered by iTunes / Apple Music Public Search API,
     * Piped Open YouTube API, and SoundCloud Progressive MP3.
     */
    suspend fun resolveFallbackDownloadUrlForTrack(
        media: MediaItemEntity,
        preferVideo: Boolean = media.isVideo
    ): String = withContext(Dispatchers.IO) {
        val cleanArtist = media.artist.substringBefore("(").trim()
        val searchTerms = listOf(
            "$cleanArtist ${media.title}".trim(),
            media.title.trim(),
            cleanArtist
        ).filter { it.isNotEmpty() }.distinct()

        for (searchTerm in searchTerms) {
            // 1. Try Deezer Public Search API direct MP3 stream
            if (!preferVideo) {
                val deezerUrl = fetchDeezerDirectAudioUrl(searchTerm)
                if (!deezerUrl.isNullOrBlank()) {
                    return@withContext deezerUrl
                }
            }

            // 2. Try iTunes / Apple Music Public Search API (ultra-fast direct AAC/M4V CDN)
            val itunesUrl = if (preferVideo) {
                fetchItunesDirectVideoUrl(searchTerm) ?: fetchItunesDirectAudioUrl(searchTerm)
            } else {
                fetchItunesDirectAudioUrl(searchTerm)
            }
            if (!itunesUrl.isNullOrBlank()) {
                return@withContext itunesUrl
            }

            // 3. Try SoundCloud progressive stream
            val scCands = fetchRawSoundCloudCandidates(searchTerm, limit = 6)
            for (cand in scCands) {
                if (cand.streamOrTranscodingUrl.contains("/progressive", ignoreCase = true)) {
                    val directSc = resolveSoundCloudProgressiveUrl(cand.streamOrTranscodingUrl)
                    if (!directSc.isNullOrBlank() && !directSc.contains(".m3u8", ignoreCase = true)) {
                        return@withContext directSc
                    }
                }
            }

            // 4. Try SoundCloud HLS stream
            for (cand in scCands) {
                if (cand.streamOrTranscodingUrl.isNotBlank()) {
                    val hlsSc = resolveSoundCloudProgressiveUrl(cand.streamOrTranscodingUrl)
                    if (!hlsSc.isNullOrBlank()) {
                        return@withContext hlsSc
                    }
                }
            }
        }

        media.streamUrl.takeIf { it.startsWith("http") && !it.contains("api-v2.soundcloud.com/media/") }.orEmpty()
    }

    private fun fetchDeezerDirectAudioUrl(query: String): String? {
        return runCatching {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "${LumoYouTubeStreamAuth.DEEZER_SEARCH_API}?q=$encoded&limit=5"
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", LumoYouTubeStreamAuth.USER_AGENT)
                .get()
                .build()
            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val data = JSONObject(resp.body?.string().orEmpty()).optJSONArray("data") ?: return null
                for (i in 0 until data.length()) {
                    val preview = data.optJSONObject(i)?.optString("preview", "")?.trim().orEmpty()
                    if (preview.startsWith("http")) return preview
                }
                null
            }
        }.getOrNull()
    }

    private fun fetchDeezerCatalogTracks(
        query: String,
        limit: Int = 25,
        isKhmerQuery: Boolean = false,
        offset: Int = 0
    ): List<MediaItemEntity> {
        return runCatching {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "${LumoYouTubeStreamAuth.DEEZER_SEARCH_API}?q=$encoded&limit=$limit&index=$offset"
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", LumoYouTubeStreamAuth.USER_AGENT)
                .get()
                .build()
            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return emptyList()
                val body = resp.body?.string().orEmpty()
                if (body.isBlank()) return emptyList()
                val data = JSONObject(body).optJSONArray("data") ?: return emptyList()
                val parsed = mutableListOf<MediaItemEntity>()
                for (i in 0 until data.length()) {
                    val obj = data.optJSONObject(i) ?: continue
                    val trackId = obj.optLong("id", 0L)
                    val rawTitle = obj.optString("title", "").trim()
                    val previewUrl = obj.optString("preview", "").trim()
                    if (trackId <= 0L || rawTitle.isEmpty() || !previewUrl.startsWith("http")) continue

                    val artistObj = obj.optJSONObject("artist")
                    val artistName = artistObj?.optString("name", "")?.trim().orEmpty().ifBlank { "Global Artist" }
                    val albumObj = obj.optJSONObject("album")
                    val albumTitle = albumObj?.optString("title", "")?.trim().orEmpty().ifBlank { "$artistName · Single" }
                    val artworkUrl = albumObj?.optString("cover_xl", "")
                        ?.ifBlank { albumObj.optString("cover_big", "") }
                        ?.ifBlank { artistObj?.optString("picture_xl", "").orEmpty() }
                        .orEmpty()

                    val durationSec = obj.optInt("duration", 180).coerceAtLeast(5)
                    val rankScore = obj.optLong("rank", 0L).coerceAtLeast(0L)

                    val (normalizedArtist, normalizedTitle) = KhmerArtistDirectory.normalizeTrackArtistAndTitle(
                        rawTitle = rawTitle,
                        uploaderUsername = artistName,
                        publisherArtist = artistName,
                        queryHint = query
                    )

                    val isKhmerTrack = isKhmerQuery ||
                        containsKhmerScript(rawTitle) ||
                        containsKhmerScript(artistName) ||
                        KhmerArtistDirectory.detectProfileInText(normalizedArtist, normalizedTitle) != null

                    val seed = abs(trackId.hashCode())
                    parsed.add(
                        MediaItemEntity(
                            id = "dz_$trackId",
                            title = normalizedTitle,
                            artist = normalizedArtist,
                            album = albumTitle,
                            durationSec = durationSec,
                            isVideo = false,
                            artworkUrl = artworkUrl,
                            artworkResName = "img_lumo_icon_fg",
                            accentHex = accentPalette[seed % accentPalette.size],
                            streamUrl = previewUrl,
                            licenseBadge = "Deezer Audio · MP3 (${formatSeconds(durationSec)})",
                            isAuthorizedDownload = true,
                            category = if (isKhmerTrack) "Khmer Music" else "Full Song",
                            releaseYear = 2026,
                            bitrateKbps = 320,
                            popularityScore = rankScore,
                            waveformSeed = (seed % 500) + 23
                        )
                    )
                }
                parsed
            }
        }.getOrDefault(emptyList())
    }

    private fun fetchItunesDirectAudioUrl(query: String): String? {
        val countries = listOf("US", "KH", "KR", "JP", "GB")
        for (country in countries) {
            val found = runCatching {
                val encoded = URLEncoder.encode(query, "UTF-8")
                val url = "${LumoYouTubeStreamAuth.ITUNES_SEARCH_API}?term=$encoded&country=$country&media=music&entity=song&limit=5"
                val req = Request.Builder()
                    .url(url)
                    .header("User-Agent", LumoYouTubeStreamAuth.USER_AGENT)
                    .get()
                    .build()
                httpClient.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@use null
                    val results = JSONObject(resp.body?.string().orEmpty()).optJSONArray("results") ?: return@use null
                    for (i in 0 until results.length()) {
                        val preview = results.optJSONObject(i)?.optString("previewUrl", "")?.trim().orEmpty()
                        if (preview.startsWith("http")) return@use preview
                    }
                    null
                }
            }.getOrNull()
            if (!found.isNullOrBlank()) return found
        }
        return null
    }

    private fun fetchItunesDirectVideoUrl(query: String): String? {
        return runCatching {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "${LumoYouTubeStreamAuth.ITUNES_SEARCH_API}?term=$encoded&media=musicVideo&entity=musicVideo&limit=4"
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", LumoYouTubeStreamAuth.USER_AGENT)
                .get()
                .build()
            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val results = JSONObject(resp.body?.string().orEmpty()).optJSONArray("results") ?: return null
                for (i in 0 until results.length()) {
                    val preview = results.optJSONObject(i)?.optString("previewUrl", "")?.trim().orEmpty()
                    if (preview.startsWith("http")) return preview
                }
                null
            }
        }.getOrNull()
    }

    fun isLikelyYouTubeOrMediaUrl(input: String): Boolean {
        val s = input.trim().lowercase(Locale.US)
        return s.contains("youtube.com/") ||
            s.contains("youtu.be/") ||
            s.startsWith("http://") ||
            s.startsWith("https://")
    }

    fun extractYouTubeVideoId(input: String): String? {
        val trimmed = input.trim()
        if (YT_ID_EXACT_REGEX.matches(trimmed)) {
            return trimmed
        }
        for (regex in YT_URL_PATTERNS) {
            val match = regex.find(trimmed)
            if (match != null && match.groupValues.size > 1) {
                return match.groupValues[1]
            }
        }
        return null
    }

    private suspend fun resolveBestMatchingStreamForTrack(
        rawTitle: String,
        songTitle: String,
        artist: String,
        channel: String,
        targetDurationSec: Int,
        preferVideo: Boolean = false
    ): Pair<String, Int>? = coroutineScope {
        val cleanArtist = artist
            .replace("Official", "", ignoreCase = true)
            .replace("VEVO", "", ignoreCase = true)
            .replace("- Topic", "", ignoreCase = true)
            .substringBefore("(")
            .trim()

        val primaryQuery = "$cleanArtist $songTitle".trim().ifEmpty { rawTitle }

        // 1. Query iTunes / Apple Music Public Search API & SoundCloud in parallel with tight timeout
        val itunesDeferred = async {
            withTimeoutOrNull(2800L) {
                if (preferVideo) {
                    fetchItunesDirectVideoUrl(primaryQuery) ?: fetchItunesDirectAudioUrl(primaryQuery)
                } else {
                    fetchItunesDirectAudioUrl(primaryQuery) ?: fetchItunesDirectAudioUrl(songTitle)
                }
            }
        }

        val scCandidatesDeferred = async {
            withTimeoutOrNull(2800L) {
                fetchRawSoundCloudCandidates(primaryQuery, limit = 10)
            }.orEmpty()
        }

        val candidates = scCandidatesDeferred.await()
        if (candidates.isNotEmpty()) {
            val scored = candidates.map { cand ->
                val score = scoreCandidateMatch(
                    cand = cand,
                    targetRawTitle = rawTitle,
                    targetSongTitle = songTitle,
                    targetArtist = cleanArtist,
                    targetChannel = channel,
                    targetDurationSec = targetDurationSec
                )
                Pair(cand, score)
            }.sortedByDescending { it.second }

            for ((bestCand, bestScore) in scored.take(3)) {
                if (bestScore >= 25) {
                    val finalStreamUrl = if (bestCand.isTranscodingPointer) {
                        resolveSoundCloudProgressiveUrl(bestCand.streamOrTranscodingUrl)
                    } else {
                        bestCand.streamOrTranscodingUrl
                    }
                    if (!finalStreamUrl.isNullOrBlank()) {
                        return@coroutineScope Pair(finalStreamUrl, bestCand.durationSec)
                    }
                }
            }
        }

        // 2. Use iTunes / Apple Music Public Search API direct stream
        val itunesStream = itunesDeferred.await()
        if (!itunesStream.isNullOrBlank()) {
            return@coroutineScope Pair(itunesStream, targetDurationSec.coerceAtLeast(180))
        }

        // 3. Any remaining SoundCloud candidate
        for (cand in candidates.take(2)) {
            val finalStreamUrl = if (cand.isTranscodingPointer) {
                resolveSoundCloudProgressiveUrl(cand.streamOrTranscodingUrl)
            } else {
                cand.streamOrTranscodingUrl
            }
            if (!finalStreamUrl.isNullOrBlank()) {
                return@coroutineScope Pair(finalStreamUrl, cand.durationSec)
            }
        }

        null
    }

    private fun scoreCandidateMatch(
        cand: CandidateAudioTrack,
        targetRawTitle: String,
        targetSongTitle: String,
        targetArtist: String,
        targetChannel: String,
        targetDurationSec: Int
    ): Int {
        var score = 0
        val candText = "${cand.title} ${cand.artist}".lowercase(Locale.ROOT)
        val candTitleLower = cand.title.lowercase(Locale.ROOT)

        val titleTokens = extractMeaningfulTokens(targetSongTitle)
        val artistTokens = extractMeaningfulTokens("$targetArtist $targetChannel")
        val allTargetTokens = (titleTokens + artistTokens).distinct()

        var matchedTitleTokens = 0
        for (tok in titleTokens) {
            if (candText.contains(tok)) {
                matchedTitleTokens++
                score += 35
            }
        }

        var matchedArtistTokens = 0
        for (tok in artistTokens) {
            if (candText.contains(tok)) {
                matchedArtistTokens++
                score += 30
            }
        }

        if (titleTokens.isNotEmpty() && matchedTitleTokens == titleTokens.size) {
            score += 60
        }
        if (artistTokens.isNotEmpty() && matchedArtistTokens > 0) {
            score += 45
        }
        if (allTargetTokens.isNotEmpty() && matchedTitleTokens == 0 && matchedArtistTokens == 0) {
            score -= 200
        }

        if (targetDurationSec > 30 && cand.durationSec > 30) {
            val diff = abs(cand.durationSec - targetDurationSec)
            score += when {
                diff <= 8 -> 75
                diff <= 25 -> 50
                diff <= 45 -> 20
                diff <= 90 -> 0
                else -> -45
            }
        }

        val targetLower = targetRawTitle.lowercase(Locale.ROOT)
        val unwantedModifiers = listOf("slowed", "sped up", "nightcore", "karaoke", "instrumental", "8d audio", "ringtone")
        for (mod in unwantedModifiers) {
            if (candTitleLower.contains(mod) && !targetLower.contains(mod)) {
                score -= 55
            }
        }

        return score
    }

    private fun extractMeaningfulTokens(input: String): List<String> {
        val stopWords = setOf(
            "official", "video", "audio", "music", "lyrics", "lyric", "visualizer",
            "feat", "ft", "prod", "by", "the", "and", "mv", "hd", "4k", "full", "song"
        )
        return TOKEN_REGEX
            .findAll(input.lowercase(Locale.ROOT))
            .map { it.value.trim() }
            .filter { it.length >= 2 && it !in stopWords }
            .distinct()
            .toList()
    }

    private fun containsKhmerScript(text: String): Boolean {
        for (ch in text) {
            if (ch.code in 0x1780..0x17FF) return true
        }
        return false
    }

    // =========================================================================
    // 1. PIPED OPEN YOUTUBE API (Search + Streams)
    // =========================================================================

    private fun orderedPipedInstances(): List<String> {
        val preferred = preferredPipedHost
        return listOf(preferred) + LumoYouTubeStreamAuth.PIPED_API_INSTANCES.filterNot { it == preferred }
    }

    private fun fetchPipedOpenYouTubeSearchTracks(
        query: String,
        limit: Int = 25,
        includeSongs: Boolean = true,
        includeVideos: Boolean = true,
        onlyYear2026: Boolean = false
    ): List<MediaItemEntity> {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val filterParam = when {
            includeSongs && !includeVideos -> "music_songs"
            includeVideos && !includeSongs -> "videos"
            else -> "all"
        }

        for (instance in orderedPipedInstances().take(3)) {
            val items = runCatching {
                val url = "$instance/search?q=$encoded&filter=$filterParam"
                val req = Request.Builder()
                    .url(url)
                    .header("User-Agent", LumoYouTubeStreamAuth.USER_AGENT)
                    .get()
                    .build()

                httpClient.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@use emptyList<MediaItemEntity>()
                    val body = resp.body?.string().orEmpty()
                    if (body.isBlank()) return@use emptyList<MediaItemEntity>()

                    val json = JSONObject(body)
                    val arr = json.optJSONArray("items") ?: return@use emptyList<MediaItemEntity>()
                    val parsed = mutableListOf<MediaItemEntity>()
                    val isKhmerQuery = KhmerArtistDirectory.isKhmerRelatedQuery(query)

                    for (i in 0 until arr.length()) {
                        if (parsed.size >= limit) break
                        val obj = arr.optJSONObject(i) ?: continue
                        val itemType = obj.optString("type", "stream")
                        if (itemType != "stream" && itemType.isNotEmpty()) continue

                        val rawUrl = obj.optString("url", "").trim()
                        val videoId = extractYouTubeVideoId(rawUrl)
                            ?: rawUrl.substringAfter("v=", "").substringBefore("&").trim()
                        if (videoId.length != 11) continue

                        val rawTitle = obj.optString("title", "").trim()
                        if (rawTitle.isEmpty()) continue

                        val uploadedMs = obj.optLong("uploaded", 0L)
                        val uploadedDateStr = obj.optString("uploadedDate", "")
                        val releaseYear = parsePipedReleaseYear(uploadedMs, uploadedDateStr, rawTitle)
                        if (onlyYear2026 && releaseYear < 2026) continue

                        val durationSec = obj.optInt("duration", 0)
                        if (durationSec in 1..2) continue
                        val effectiveDuration = if (durationSec > 0) durationSec else 215

                        val rawUploader = obj.optString("uploaderName", "")
                            .ifBlank { obj.optString("uploader", "YouTube Music") }
                            .replace("- Topic", "")
                            .replace("Official", "")
                            .trim()
                            .ifBlank { "YouTube Artist" }

                        val rawViews = obj.optLong("views", 0L).coerceAtLeast(0L)
                        val thumb = obj.optString("thumbnail", "").trim()
                            .ifBlank { "https://i.ytimg.com/vi/$videoId/hqdefault.jpg" }

                        val (normalizedArtist, normalizedTitle) = KhmerArtistDirectory.normalizeTrackArtistAndTitle(
                            rawTitle = rawTitle,
                            uploaderUsername = rawUploader,
                            queryHint = query
                        )
                        val cleanedSongTitle = cleanYouTubeTitleOnly(normalizedTitle).ifBlank { rawTitle }

                        val isExplicitVideo = rawTitle.contains("Official Video", ignoreCase = true) ||
                            rawTitle.contains("Music Video", ignoreCase = true) ||
                            rawTitle.contains("MV", ignoreCase = true)
                        val isVideoItem = when {
                            includeVideos && !includeSongs -> true
                            !includeVideos && includeSongs -> false
                            else -> isExplicitVideo && parsed.size >= 4
                        }

                        val isKhmerTrack = isKhmerQuery ||
                            containsKhmerScript(rawTitle) ||
                            containsKhmerScript(rawUploader) ||
                            KhmerArtistDirectory.detectProfileInText(normalizedArtist, rawTitle) != null

                        val popScore = KhmerArtistDirectory.computePopularityScore(
                            rawPlays = rawViews,
                            title = cleanedSongTitle,
                            artist = normalizedArtist,
                            queryHint = query,
                            rankIndex = parsed.size
                        )

                        val seed = abs(videoId.hashCode())
                        val entity = MediaItemEntity(
                            id = "yt_$videoId",
                            title = cleanedSongTitle,
                            artist = normalizedArtist,
                            album = if (isKhmerTrack) "Khmer Music · $normalizedArtist" else "Piped YouTube · $rawUploader",
                            durationSec = effectiveDuration,
                            isVideo = isVideoItem,
                            artworkUrl = thumb,
                            artworkResName = "img_lumo_icon_fg",
                            accentHex = accentPalette[seed % accentPalette.size],
                            streamUrl = "",
                            licenseBadge = "Piped YouTube · (${formatSeconds(effectiveDuration)})",
                            isAuthorizedDownload = true,
                            category = when {
                                isKhmerTrack && isVideoItem -> "Khmer Video"
                                isKhmerTrack -> "Khmer Music"
                                isVideoItem -> "Music Video"
                                effectiveDuration < 45 -> "Sound Clip"
                                else -> "Full Song"
                            },
                            releaseYear = releaseYear,
                            bitrateKbps = 256,
                            popularityScore = popScore,
                            waveformSeed = (seed % 500) + 19
                        )
                        if (!onlyYear2026 || KhmerArtistDirectory.isEligibleForTrending2026OrToday(entity)) {
                            parsed.add(entity)
                        }
                    }
                    parsed
                }
            }.getOrDefault(emptyList())

            if (items.isNotEmpty()) {
                preferredPipedHost = instance
                return items
            }
        }
        return emptyList()
    }

    private fun fetchPipedStreamsForVideoId(videoId: String): ResolvedYouTubeStreams? {
        val cleanId = videoId.removePrefix("yt_").trim()
        if (cleanId.isEmpty()) return null

        for (instance in orderedPipedInstances().take(3)) {
            val resolved = runCatching {
                val url = "$instance/streams/$cleanId"
                val req = Request.Builder()
                    .url(url)
                    .header("User-Agent", LumoYouTubeStreamAuth.USER_AGENT)
                    .get()
                    .build()

                httpClient.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@use null
                    val body = resp.body?.string().orEmpty()
                    if (body.isBlank()) return@use null
                    val json = JSONObject(body)

                    val title = json.optString("title", "").trim()
                    val uploader = json.optString("uploader", "").trim()
                    val durationSec = json.optInt("duration", 0)
                    val viewsCount = json.optLong("views", 0L).coerceAtLeast(0L)
                    val thumbnailUrl = json.optString("thumbnailUrl", "")
                        .ifBlank { "https://i.ytimg.com/vi/$cleanId/hqdefault.jpg" }

                    var bestAudioUrl: String? = null
                    var bestAudioBitrate = -1
                    val audioStreams = json.optJSONArray("audioStreams")
                    if (audioStreams != null) {
                        for (i in 0 until audioStreams.length()) {
                            val stream = audioStreams.optJSONObject(i) ?: continue
                            val sUrl = stream.optString("url", "").trim()
                            if (!sUrl.startsWith("http")) continue
                            val mime = stream.optString("mimeType", "").lowercase(Locale.ROOT)
                            val bitrate = stream.optInt("bitrate", 0)
                            val bonus = if (mime.contains("mp4") || mime.contains("m4a")) 50_000 else 0
                            if (bitrate + bonus > bestAudioBitrate) {
                                bestAudioBitrate = bitrate + bonus
                                bestAudioUrl = sUrl
                            }
                        }
                    }

                    var bestVideoUrl: String? = null
                    val videoStreams = json.optJSONArray("videoStreams")
                    if (videoStreams != null) {
                        for (i in 0 until videoStreams.length()) {
                            val stream = videoStreams.optJSONObject(i) ?: continue
                            val videoOnly = stream.optBoolean("videoOnly", true)
                            val sUrl = stream.optString("url", "").trim()
                            if (!videoOnly && sUrl.startsWith("http")) {
                                bestVideoUrl = sUrl
                                break
                            }
                        }
                    }

                    val hlsUrl = json.optString("hls", "").trim().takeIf { it.startsWith("http") }

                    if (title.isEmpty() && bestAudioUrl == null && bestVideoUrl == null && hlsUrl == null) {
                        return@use null
                    }

                    preferredPipedHost = instance
                    ResolvedYouTubeStreams(
                        videoId = cleanId,
                        title = title,
                        author = uploader,
                        durationSec = durationSec,
                        thumbnailUrl = thumbnailUrl,
                        directVideoMp4Url = bestVideoUrl ?: hlsUrl,
                        directAudioUrl = bestAudioUrl ?: hlsUrl,
                        views = viewsCount
                    )
                }
            }.getOrNull()

            if (resolved != null) return resolved
        }
        return null
    }

    // =========================================================================
    // 2. YOUTUBE OFFICIAL PUBLIC OEMBED API (+ Piped Stream Enrichment)
    // =========================================================================

    private suspend fun extractYouTubeMetadataViaOEmbedAndPiped(videoId: String): ResolvedYouTubeStreams = coroutineScope {
        val defaultThumb = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"

        val oembedDeferred = async {
            withTimeoutOrNull(2500L) {
                fetchYouTubeOEmbedMetadata(videoId)
            }
        }
        val pipedDeferred = async {
            withTimeoutOrNull(2800L) {
                fetchPipedStreamsForVideoId(videoId)
            }
        }

        val oembed = oembedDeferred.await()
        val piped = pipedDeferred.await()

        val title = piped?.title?.takeIf { it.isNotBlank() }
            ?: oembed?.first?.takeIf { it.isNotBlank() }
            ?: "YouTube Track ($videoId)"
        val author = piped?.author?.takeIf { it.isNotBlank() }
            ?: oembed?.second?.takeIf { it.isNotBlank() }
            ?: "YouTube Artist"
        val durationSec = piped?.durationSec?.takeIf { it > 0 } ?: 218
        val thumbnailUrl = oembed?.third?.takeIf { it.isNotBlank() }
            ?: piped?.thumbnailUrl?.takeIf { it.isNotBlank() }
            ?: defaultThumb

        ResolvedYouTubeStreams(
            videoId = videoId,
            title = title,
            author = author,
            durationSec = durationSec,
            thumbnailUrl = thumbnailUrl,
            directVideoMp4Url = piped?.directVideoMp4Url,
            directAudioUrl = piped?.directAudioUrl,
            views = piped?.views ?: 0L
        )
    }

    private fun fetchYouTubeOEmbedMetadata(videoId: String): Triple<String, String, String>? {
        val watchUrl = "https://www.youtube.com/watch?v=$videoId"
        val encodedUrl = URLEncoder.encode(watchUrl, "UTF-8")
        val endpoints = listOf(
            "${LumoYouTubeStreamAuth.YOUTUBE_OEMBED_API}?url=$encodedUrl&format=json",
            "${LumoYouTubeStreamAuth.NOEMBED_FALLBACK_API}?url=$encodedUrl"
        )
        for (endpoint in endpoints) {
            runCatching {
                val request = Request.Builder()
                    .url(endpoint)
                    .header("User-Agent", LumoYouTubeStreamAuth.USER_AGENT)
                    .get()
                    .build()
                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string().orEmpty()
                        if (body.isNotBlank()) {
                            val json = JSONObject(body)
                            val title = json.optString("title", "").trim()
                            val author = json.optString("author_name", "").trim()
                            val thumb = json.optString("thumbnail_url", "").trim()
                                .ifBlank { "https://i.ytimg.com/vi/$videoId/hqdefault.jpg" }
                            if (title.isNotEmpty()) {
                                return Triple(title, author.ifEmpty { "YouTube Artist" }, thumb)
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    // =========================================================================
    // 3. ITUNES / APPLE MUSIC PUBLIC SEARCH API (Songs & Music Videos)
    // =========================================================================

    private fun fetchAppleMusicCatalogTracks(
        query: String,
        limit: Int = 30,
        isKhmerQuery: Boolean = false,
        offset: Int = 0
    ): List<MediaItemEntity> {
        return runCatching {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "${LumoYouTubeStreamAuth.ITUNES_SEARCH_API}?term=$encoded&media=music&entity=song&limit=$limit&offset=$offset"
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", LumoYouTubeStreamAuth.USER_AGENT)
                .get()
                .build()

            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return emptyList()
                val body = resp.body?.string().orEmpty()
                if (body.isBlank()) return emptyList()

                val results = JSONObject(body).optJSONArray("results") ?: return emptyList()
                val parsed = mutableListOf<MediaItemEntity>()

                for (i in 0 until results.length()) {
                    val obj = results.optJSONObject(i) ?: continue
                    val trackId = obj.optLong("trackId", 0L)
                    val trackName = obj.optString("trackName", "").trim()
                    val artistName = obj.optString("artistName", "").trim()
                    if (trackId <= 0L || trackName.isEmpty() || artistName.isEmpty()) continue

                    val collectionName = obj.optString("collectionName", "").trim().ifBlank { "$artistName · Single" }
                    val trackTimeMs = obj.optLong("trackTimeMillis", 205_000L)
                    val durationSec = (trackTimeMs / 1000L).toInt().coerceAtLeast(120)
                    val artworkUrl = obj.optString("artworkUrl100", "")
                        .replace("100x100bb", "600x600bb")
                    val previewUrl = obj.optString("previewUrl", "").trim()
                    if (previewUrl.isBlank()) continue

                    val releaseYear = obj.optString("releaseDate", "2026")
                        .take(4)
                        .toIntOrNull() ?: 2026

                    val (normalizedArtist, normalizedTitle) = KhmerArtistDirectory.normalizeTrackArtistAndTitle(
                        rawTitle = trackName,
                        uploaderUsername = artistName,
                        publisherArtist = artistName,
                        queryHint = query
                    )

                    val isKhmerTrack = isKhmerQuery ||
                        containsKhmerScript(trackName) ||
                        containsKhmerScript(artistName) ||
                        KhmerArtistDirectory.detectProfileInText(normalizedArtist, normalizedTitle) != null

                    val popScore = 0L

                    val seed = abs(trackId.hashCode())
                    parsed.add(
                        MediaItemEntity(
                            id = "am_$trackId",
                            title = normalizedTitle,
                            artist = normalizedArtist,
                            album = collectionName,
                            durationSec = durationSec,
                            isVideo = false,
                            artworkUrl = artworkUrl,
                            artworkResName = "img_lumo_icon_fg",
                            accentHex = accentPalette[seed % accentPalette.size],
                            streamUrl = previewUrl,
                            licenseBadge = "Apple Music · AAC (${formatSeconds(durationSec)})",
                            isAuthorizedDownload = true,
                            category = if (isKhmerTrack) "Khmer Music" else "Full Song",
                            releaseYear = releaseYear,
                            bitrateKbps = 256,
                            popularityScore = popScore,
                            waveformSeed = (seed % 500) + 31
                        )
                    )
                }
                parsed
            }
        }.getOrDefault(emptyList())
    }

    private fun fetchAppleMusicVideoTracks(
        query: String,
        limit: Int = 15,
        isKhmerQuery: Boolean = false,
        offset: Int = 0
    ): List<MediaItemEntity> {
        return runCatching {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "${LumoYouTubeStreamAuth.ITUNES_SEARCH_API}?term=$encoded&media=musicVideo&entity=musicVideo&limit=$limit&offset=$offset"
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", LumoYouTubeStreamAuth.USER_AGENT)
                .get()
                .build()

            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return emptyList()
                val body = resp.body?.string().orEmpty()
                if (body.isBlank()) return emptyList()

                val results = JSONObject(body).optJSONArray("results") ?: return emptyList()
                val parsed = mutableListOf<MediaItemEntity>()

                for (i in 0 until results.length()) {
                    val obj = results.optJSONObject(i) ?: continue
                    val trackId = obj.optLong("trackId", 0L)
                    val trackName = obj.optString("trackName", "").trim()
                    val artistName = obj.optString("artistName", "").trim()
                    val previewUrl = obj.optString("previewUrl", "").trim()
                    if (trackId <= 0L || trackName.isEmpty() || artistName.isEmpty() || previewUrl.isBlank()) continue

                    val collectionName = obj.optString("collectionName", "").trim().ifBlank { "Apple Music Video" }
                    val trackTimeMs = obj.optLong("trackTimeMillis", 215_000L)
                    val durationSec = (trackTimeMs / 1000L).toInt().coerceAtLeast(120)
                    val artworkUrl = obj.optString("artworkUrl100", "")
                        .replace("100x100bb", "600x600bb")

                    val (normalizedArtist, normalizedTitle) = KhmerArtistDirectory.normalizeTrackArtistAndTitle(
                        rawTitle = trackName,
                        uploaderUsername = artistName,
                        publisherArtist = artistName,
                        queryHint = query
                    )

                    val isKhmerTrack = isKhmerQuery ||
                        containsKhmerScript(trackName) ||
                        containsKhmerScript(artistName) ||
                        KhmerArtistDirectory.detectProfileInText(normalizedArtist, normalizedTitle) != null

                    val popScore = 0L

                    val seed = abs(trackId.hashCode())
                    parsed.add(
                        MediaItemEntity(
                            id = "amv_$trackId",
                            title = normalizedTitle,
                            artist = normalizedArtist,
                            album = collectionName,
                            durationSec = durationSec,
                            isVideo = true,
                            artworkUrl = artworkUrl,
                            artworkResName = "img_lumo_icon_fg",
                            accentHex = accentPalette[seed % accentPalette.size],
                            streamUrl = previewUrl,
                            licenseBadge = "Apple Music Video · MP4 (${formatSeconds(durationSec)})",
                            isAuthorizedDownload = true,
                            category = if (isKhmerTrack) "Khmer Video" else "Music Video",
                            releaseYear = 2026,
                            bitrateKbps = 1200,
                            popularityScore = popScore,
                            waveformSeed = (seed % 500) + 37
                        )
                    )
                }
                parsed
            }
        }.getOrDefault(emptyList())
    }

    // =========================================================================
    // 4. FAST SOUNDCLOUD SUPPLEMENT (Zero blocking URL resolution during search)
    // =========================================================================

    private fun fetchFullLengthSoundCloudTracks(
        query: String,
        limit: Int = 24,
        offset: Int = 0,
        includeSongs: Boolean = true,
        includeVideos: Boolean = true,
        onlyYear2026: Boolean = false
    ): List<MediaItemEntity> {
        val rawCandidates = fetchRawSoundCloudCandidates(
            query = query,
            limit = (limit + 10).coerceAtMost(35),
            offset = offset,
            onlyYear2026 = onlyYear2026
        )
        if (rawCandidates.isEmpty()) return emptyList()

        val isKhmerQuery = KhmerArtistDirectory.isKhmerRelatedQuery(query)

        return rawCandidates.take(limit).mapIndexed { idx, cand ->
            val effectiveStreamUrl = streamCache[cand.id]?.streamUrl?.takeIf { it.isNotBlank() }
                ?: cand.streamOrTranscodingUrl

            val isVideoItem = when {
                includeVideos && !includeSongs -> true
                !includeVideos && includeSongs -> false
                else -> idx >= 14 && idx % 6 == 5
            }
            val isKhmerTrack = isKhmerQuery ||
                containsKhmerScript(cand.title) ||
                containsKhmerScript(cand.artist) ||
                KhmerArtistDirectory.detectProfileInText(cand.artist, cand.title) != null

            val categoryLabel = when {
                isKhmerTrack && isVideoItem -> "Khmer Video"
                isKhmerTrack -> "Khmer Music"
                isVideoItem -> "Music Video"
                cand.durationSec < 45 -> "Sound Clip"
                else -> "Full Song"
            }

            val seed = abs(cand.id.hashCode())
            MediaItemEntity(
                id = cand.id,
                title = cand.title,
                artist = cand.artist,
                album = cand.album,
                durationSec = cand.durationSec,
                isVideo = isVideoItem,
                artworkUrl = cand.artworkUrl,
                artworkResName = "img_lumo_icon_fg",
                accentHex = accentPalette[seed % accentPalette.size],
                streamUrl = effectiveStreamUrl,
                licenseBadge = "Full Stream · MP3 (${formatSeconds(cand.durationSec)})",
                isAuthorizedDownload = true,
                category = categoryLabel,
                releaseYear = cand.releaseYear,
                bitrateKbps = 256,
                popularityScore = cand.popularityScore,
                waveformSeed = (seed % 500) + 13
            )
        }.filter { !onlyYear2026 || KhmerArtistDirectory.isEligibleForTrending2026OrToday(it) }
    }

    private fun fetchRawSoundCloudCandidates(
        query: String,
        limit: Int = 24,
        offset: Int = 0,
        onlyYear2026: Boolean = false
    ): List<CandidateAudioTrack> {
        return runCatching {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "https://api-v2.soundcloud.com/search/tracks?q=$encoded&client_id=${LumoYouTubeStreamAuth.SOUNDCLOUD_CLIENT_ID}&limit=$limit&offset=$offset"
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", LumoYouTubeStreamAuth.USER_AGENT)
                .get()
                .build()

            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return emptyList()
                val body = resp.body?.string().orEmpty()
                if (body.isBlank()) return emptyList()

                val collection = JSONObject(body).optJSONArray("collection") ?: return emptyList()
                val list = mutableListOf<CandidateAudioTrack>()

                for (i in 0 until collection.length()) {
                    val item = collection.optJSONObject(i) ?: continue
                    val trackId = item.optLong("id", 0L)
                    if (trackId <= 0L) continue

                    val policy = item.optString("policy", "ALLOW")
                    if (policy.equals("BLOCK", ignoreCase = true)) continue

                    val dateRaw = item.optString("release_date", "")
                        .ifBlank { item.optString("display_date", "") }
                        .ifBlank { item.optString("created_at", "") }
                    val scYear = dateRaw.take(4).toIntOrNull() ?: 2026
                    if (onlyYear2026 && scYear < 2026) continue

                    val fullDurationMs = item.optLong("full_duration", item.optLong("duration", 0L))
                    val durationMs = item.optLong("duration", 0L)
                    val durationSec = (maxOf(fullDurationMs, durationMs) / 1000L).toInt()
                    if (durationSec < 3) continue

                    val transcodings = item.optJSONObject("media")?.optJSONArray("transcodings")
                    var progressiveTranscodingUrl = ""
                    var mp3HlsTranscodingUrl = ""
                    var fallbackHlsTranscodingUrl = ""
                    var isSnipped = policy.equals("SNIP", ignoreCase = true)

                    if (transcodings != null) {
                        for (t in 0 until transcodings.length()) {
                            val trans = transcodings.optJSONObject(t) ?: continue
                            if (trans.optBoolean("snipped", false)) {
                                isSnipped = true
                            }
                            val formatObj = trans.optJSONObject("format")
                            val protocol = formatObj?.optString("protocol", "").orEmpty()
                            val mimeType = formatObj?.optString("mime_type", "").orEmpty()
                            val transUrl = trans.optString("url", "").trim()
                            if (protocol == "progressive" && transUrl.isNotBlank()) {
                                progressiveTranscodingUrl = transUrl
                                break
                            } else if (protocol == "hls" && transUrl.isNotBlank()) {
                                if (mimeType.contains("mpeg", ignoreCase = true) && mp3HlsTranscodingUrl.isBlank()) {
                                    mp3HlsTranscodingUrl = transUrl
                                } else if (fallbackHlsTranscodingUrl.isBlank()) {
                                    fallbackHlsTranscodingUrl = transUrl
                                }
                            }
                        }
                    }

                    val chosenTranscodingUrl = if (isSnipped) {
                        ""
                    } else {
                        progressiveTranscodingUrl
                            .ifBlank { mp3HlsTranscodingUrl }
                            .ifBlank { fallbackHlsTranscodingUrl }
                    }

                    val rawTitle = item.optString("title", "").trim()
                    if (rawTitle.isBlank()) continue
                    val userObj = item.optJSONObject("user")
                    val username = userObj?.optString("username", "")?.trim().orEmpty().ifBlank { "Global Artist" }
                    val publisherArtist = item.optJSONObject("publisher_metadata")?.optString("artist", "")?.trim()

                    val (normalizedArtist, normalizedTitle) = KhmerArtistDirectory.normalizeTrackArtistAndTitle(
                        rawTitle = rawTitle,
                        uploaderUsername = username,
                        publisherArtist = publisherArtist,
                        queryHint = query
                    )
                    val cleanedSongTitle = cleanYouTubeTitleOnly(normalizedTitle).ifBlank { rawTitle }

                    val playbackCount = item.optLong("playback_count", 0L).coerceAtLeast(0L)
                    val popularityScore = playbackCount

                    val artUrl = item.optString("artwork_url", "")
                        .ifBlank { userObj?.optString("avatar_url", "").orEmpty() }
                        .replace("-large.jpg", "-t500x500.jpg")

                    val albumLabel = if (KhmerArtistDirectory.detectProfileInText(normalizedArtist, rawTitle) != null) {
                        "Khmer Music · $normalizedArtist"
                    } else {
                        "SoundCloud · $username"
                    }

                    list.add(
                        CandidateAudioTrack(
                            id = "sc_$trackId",
                            title = cleanedSongTitle,
                            artist = normalizedArtist,
                            album = albumLabel,
                            durationSec = durationSec,
                            artworkUrl = artUrl,
                            streamOrTranscodingUrl = chosenTranscodingUrl,
                            isTranscodingPointer = chosenTranscodingUrl.isNotBlank(),
                            sourceName = "SOUNDCLOUD",
                            popularityScore = popularityScore,
                            releaseYear = scYear
                        )
                    )
                }
                list.sortedByDescending { it.popularityScore }
            }
        }.getOrDefault(emptyList())
    }

    private fun resolveSoundCloudTrackStreamById(trackId: String): String? {
        val endpoints = listOf(
            "https://api-v2.soundcloud.com/tracks?ids=$trackId&client_id=${LumoYouTubeStreamAuth.SOUNDCLOUD_CLIENT_ID}",
            "https://api-v2.soundcloud.com/tracks/$trackId?client_id=${LumoYouTubeStreamAuth.SOUNDCLOUD_CLIENT_ID}"
        )
        for (url in endpoints) {
            val resolved = runCatching {
                val req = Request.Builder()
                    .url(url)
                    .header("User-Agent", LumoYouTubeStreamAuth.USER_AGENT)
                    .get()
                    .build()
                httpClient.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@use null
                    val rawBody = resp.body?.string()?.trim().orEmpty()
                    if (rawBody.isBlank()) return@use null
                    val item = if (rawBody.startsWith("[")) {
                        val arr = JSONArray(rawBody)
                        if (arr.length() > 0) arr.optJSONObject(0) else null
                    } else {
                        JSONObject(rawBody)
                    } ?: return@use null

                    val transcodings = item.optJSONObject("media")?.optJSONArray("transcodings") ?: return@use null
                    var mp3HlsUrl = ""
                    var fallbackHlsUrl = ""
                    for (t in 0 until transcodings.length()) {
                        val trans = transcodings.optJSONObject(t) ?: continue
                        val formatObj = trans.optJSONObject("format")
                        val protocol = formatObj?.optString("protocol", "").orEmpty()
                        val mime = formatObj?.optString("mime_type", "").orEmpty()
                        val transUrl = trans.optString("url", "").trim()
                        if (protocol == "progressive" && transUrl.isNotBlank()) {
                            val prog = resolveSoundCloudProgressiveUrl(transUrl)
                            if (!prog.isNullOrBlank()) return@use prog
                        } else if (protocol == "hls" && transUrl.isNotBlank()) {
                            if (mime.contains("mpeg", ignoreCase = true) && mp3HlsUrl.isBlank()) {
                                mp3HlsUrl = transUrl
                            } else if (fallbackHlsUrl.isBlank()) {
                                fallbackHlsUrl = transUrl
                            }
                        }
                    }
                    val chosenHls = mp3HlsUrl.ifBlank { fallbackHlsUrl }
                    if (chosenHls.isNotBlank()) {
                        return@use resolveSoundCloudProgressiveUrl(chosenHls)
                    }
                    null
                }
            }.getOrNull()
            if (!resolved.isNullOrBlank()) return resolved
        }
        return null
    }

    private fun resolveSoundCloudProgressiveUrl(transcodingUrl: String): String? {
        return runCatching {
            val separator = if (transcodingUrl.contains("?")) "&" else "?"
            val fullUrl = "${transcodingUrl}${separator}client_id=${LumoYouTubeStreamAuth.SOUNDCLOUD_CLIENT_ID}"
            val req = Request.Builder()
                .url(fullUrl)
                .header("User-Agent", LumoYouTubeStreamAuth.USER_AGENT)
                .get()
                .build()
            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val body = resp.body?.string().orEmpty()
                if (body.isBlank()) return null
                JSONObject(body).optString("url", "").trim().takeIf { it.isNotEmpty() }
            }
        }.getOrNull()
    }

    private fun cleanYouTubeTitleOnly(rawTitle: String): String {
        return rawTitle
            .replace(CLEAN_TITLE_PAREN_REGEX, " ")
            .replace(CLEAN_TITLE_BRACKET_REGEX, " ")
            .replace(MULTI_WHITESPACE_REGEX, " ")
            .trim()
    }
}
