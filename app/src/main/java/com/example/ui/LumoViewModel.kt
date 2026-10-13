package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DownloadFormatOption
import com.example.data.DownloadTaskEntity
import com.example.data.LumoDatabase
import com.example.data.LumoRepository
import com.example.data.LumoSettingsEntity
import com.example.data.MediaItemEntity
import com.example.data.PlaylistEntity
import com.example.media.LumoPlayerController
import com.example.media.PlayerPlaybackState
import com.example.ui.theme.LumoDestination
import com.example.ui.theme.LumoThemeOption
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import com.example.data.TrendingFeedFilter
import com.example.ui.theme.LumoLanguage
import kotlinx.coroutines.launch

enum class SearchCategory(val label: String, val khmerLabel: String) {
    ALL("All", "ទាំងអស់"),
    TRENDING("Trending New", "កំពុងពេញនិយម"),
    SONGS("Songs", "បទចម្រៀង"),
    VIDEOS("Videos", "វីដេអូ"),
    ARTISTS("Artists", "តារាចម្រៀង");

    fun localizedLabel(language: LumoLanguage): String =
        language.tr(km = khmerLabel, en = label, bilingual = "$khmerLabel · $label")
}

enum class LibrarySubTab(val label: String, val khmerLabel: String) {
    SONGS("Songs", "បទចម្រៀង"),
    ALBUMS("Albums", "អាល់ប៊ុម"),
    ARTISTS("Artists", "តារាចម្រៀង"),
    PLAYLISTS("Playlists", "បញ្ជីចាក់"),
    DOWNLOADS("Downloads", "ទាញយក");

    fun localizedLabel(language: LumoLanguage): String =
        language.tr(km = khmerLabel, en = label, bilingual = "$khmerLabel · $label")
}

data class LumoUiNavigationState(
    val currentTab: LumoDestination = LumoDestination.HOME,
    val searchQuery: String = "",
    val searchCategory: SearchCategory = SearchCategory.ALL,
    val isSearchingOnline: Boolean = false,
    val isLoadingMoreSearch: Boolean = false,
    val searchOffset: Int = 0,
    val onlineSearchResults: List<MediaItemEntity> = emptyList(),
    val isLinkDownloaderMode: Boolean = false,
    val youtubeLinkInput: String = "",
    val isResolvingYouTubeLink: Boolean = false,
    val resolvedYouTubeMedia: MediaItemEntity? = null,
    val librarySubTab: LibrarySubTab = LibrarySubTab.SONGS,
    val libraryQuery: String = "",
    val selectedPlaylistId: Long? = null,
    val selectedTrendingFilter: TrendingFeedFilter = TrendingFeedFilter.ALL_TRENDING,
    val isRefreshingTrending: Boolean = false,
    val worldTrendingResults: List<MediaItemEntity> = emptyList(),
    val isTrendingViewOpen: Boolean = false,
    val isFavoritesViewOpen: Boolean = false,
    val isHistoryViewOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val downloadPromptMedia: MediaItemEntity? = null,
    val trackOptionsMedia: MediaItemEntity? = null,
    val addToPlaylistMedia: MediaItemEntity? = null,
    val editMetadataMedia: MediaItemEntity? = null,
    val renameDownloadTarget: DownloadTaskEntity? = null,
    val statusBannerMessage: String? = null
)

class LumoViewModel(application: Application) : AndroidViewModel(application) {

    private val database = LumoDatabase.getInstance(application)
    private val repository = LumoRepository(
        context = application.applicationContext,
        dao = database.lumoDao(),
        externalScope = viewModelScope
    )

    val playerController = LumoPlayerController(
        context = application.applicationContext,
        scope = viewModelScope,
        onRecordPlayback = { mediaId, pos -> repository.recordPlayback(mediaId, pos) },
        onUpdatePosition = { mediaId, pos -> repository.updatePlaybackPosition(mediaId, pos) },
        onUpdateDuration = { mediaId, dur -> repository.updateDuration(mediaId, dur) },
        onRefreshStreamUrl = { media, forceRefresh ->
            repository.refreshPlayableStreamUrl(media, forceRefresh)
        }
    )

    val playerState: StateFlow<PlayerPlaybackState> = playerController.state

    val currentPlayingMediaId: StateFlow<String?> = playerController.state
        .map { it.currentMedia?.id }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allMedia: StateFlow<List<MediaItemEntity>> = repository.allMedia.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoriteMedia: StateFlow<List<MediaItemEntity>> = repository.favoriteMedia.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val historyMedia: StateFlow<List<MediaItemEntity>> = repository.historyMedia.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val playlists: StateFlow<List<PlaylistEntity>> = repository.playlists.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allDownloads: StateFlow<List<DownloadTaskEntity>> = repository.allDownloads.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val settings: StateFlow<LumoSettingsEntity> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LumoSettingsEntity()
    )

    private val _navState = MutableStateFlow(LumoUiNavigationState())
    val navState: StateFlow<LumoUiNavigationState> = _navState.asStateFlow()

    private var onlineSearchJob: Job? = null

    init {
        viewModelScope.launch {
            _navState.update { it.copy(isSearchingOnline = true, isRefreshingTrending = true) }
            val initialTrending = repository.initializeSettingsAndDiscoverLiveMusic().getOrDefault(emptyList())
            _navState.update {
                it.copy(
                    isSearchingOnline = false,
                    isRefreshingTrending = false,
                    worldTrendingResults = initialTrending
                )
            }
        }
        viewModelScope.launch {
            allMedia.collect { catalog ->
                if (catalog.isNotEmpty()) {
                    playerController.syncCatalogUpdate(catalog)
                }
            }
        }
    }

    fun selectTab(destination: LumoDestination) {
        _navState.update {
            it.copy(
                currentTab = destination,
                selectedPlaylistId = null,
                isTrendingViewOpen = false,
                isFavoritesViewOpen = false,
                isHistoryViewOpen = false
            )
        }
    }

    fun setLinkDownloaderMode(enabled: Boolean) {
        _navState.update { it.copy(isLinkDownloaderMode = enabled) }
    }

    fun openYouTubeLinkDownloader() {
        _navState.update {
            it.copy(
                currentTab = LumoDestination.SEARCH,
                isLinkDownloaderMode = true,
                isTrendingViewOpen = false,
                isFavoritesViewOpen = false,
                isHistoryViewOpen = false
            )
        }
    }

    private var linkResolveJob: Job? = null

    fun updateYouTubeLinkInput(input: String) {
        _navState.update { it.copy(youtubeLinkInput = input) }
        linkResolveJob?.cancel()
        val trimmed = input.trim()
        if (trimmed.contains("youtube.com/", ignoreCase = true) ||
            trimmed.contains("youtu.be/", ignoreCase = true) ||
            trimmed.contains("soundcloud.com/", ignoreCase = true) ||
            trimmed.contains("tiktok.com/", ignoreCase = true) ||
            trimmed.contains("spotify.com/", ignoreCase = true) ||
            trimmed.contains("music.apple.com/", ignoreCase = true) ||
            trimmed.contains("deezer.com/", ignoreCase = true) ||
            trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.matches(Regex("^[a-zA-Z0-9_-]{11}$"))
        ) {
            linkResolveJob = viewModelScope.launch {
                delay(240L)
                resolveYouTubeLink(trimmed, null)
            }
        }
    }

    fun resolveYouTubeLink(
        rawUrl: String = _navState.value.youtubeLinkInput,
        autoDownloadFormat: DownloadFormatOption? = null
    ) {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) {
            showStatusMessage("Paste a sound link or enter any song/sound title first")
            return
        }
        linkResolveJob?.cancel()
        _navState.update {
            it.copy(
                youtubeLinkInput = trimmed,
                isResolvingYouTubeLink = true
            )
        }
        viewModelScope.launch {
            val preferVideo = autoDownloadFormat?.isVideo == true
            val result = repository.resolveYouTubeOrDirectLink(trimmed, preferVideoStream = preferVideo)
            result.onSuccess { media ->
                val updatedOnline = (listOf(media) + _navState.value.onlineSearchResults).distinctBy { it.id }
                _navState.update {
                    it.copy(
                        isResolvingYouTubeLink = false,
                        resolvedYouTubeMedia = media,
                        onlineSearchResults = updatedOnline,
                        statusBannerMessage = if (autoDownloadFormat == null) {
                            "Sound Found: \"${media.title}\" (${media.formattedDuration}) — Play or Download MP4 / MP3"
                        } else null
                    )
                }
                if (autoDownloadFormat != null) {
                    startAuthorizedDownload(media, autoDownloadFormat)
                }
            }.onFailure {
                _navState.update {
                    it.copy(
                        isResolvingYouTubeLink = false,
                        statusBannerMessage = "Could not resolve sound. Try another link or sound name."
                    )
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _navState.update { it.copy(searchQuery = query, searchOffset = 0, isLoadingMoreSearch = false) }
        onlineSearchJob?.cancel()
        val trimmed = query.trim()

        // If the user pastes any sound/music link directly into the search bar, also resolve it immediately!
        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.contains("youtube.com/", ignoreCase = true) ||
            trimmed.contains("youtu.be/", ignoreCase = true) ||
            trimmed.contains("soundcloud.com/", ignoreCase = true) ||
            trimmed.contains("tiktok.com/", ignoreCase = true) ||
            trimmed.contains("spotify.com/", ignoreCase = true)
        ) {
            resolveYouTubeLink(trimmed)
        }

        if (trimmed.isNotEmpty()) {
            onlineSearchJob = viewModelScope.launch {
                delay(280L)
                _navState.update { it.copy(isSearchingOnline = true) }
                val cat = _navState.value.searchCategory
                val result = repository.searchLiveOnlineCatalog(
                    query = trimmed,
                    includeSongs = cat != SearchCategory.VIDEOS,
                    includeVideos = cat == SearchCategory.ALL || cat == SearchCategory.VIDEOS,
                    limit = 80,
                    offset = 0
                )
                val liveList = result.getOrDefault(emptyList())
                _navState.update {
                    it.copy(
                        isSearchingOnline = false,
                        searchOffset = liveList.size,
                        onlineSearchResults = liveList
                    )
                }
            }
        } else {
            _navState.update {
                it.copy(
                    isSearchingOnline = false,
                    isLoadingMoreSearch = false,
                    searchOffset = 0,
                    onlineSearchResults = emptyList()
                )
            }
        }
    }

    fun loadMoreOnlineSearchResults() {
        val currentState = _navState.value
        val trimmed = currentState.searchQuery.trim()
        if (trimmed.isEmpty() || currentState.isSearchingOnline || currentState.isLoadingMoreSearch) return

        val nextOffset = currentState.searchOffset.coerceAtLeast(24)
        viewModelScope.launch {
            _navState.update { it.copy(isLoadingMoreSearch = true) }
            val cat = _navState.value.searchCategory
            val result = repository.searchLiveOnlineCatalog(
                query = trimmed,
                includeSongs = cat != SearchCategory.VIDEOS,
                includeVideos = cat == SearchCategory.ALL || cat == SearchCategory.VIDEOS,
                limit = 65,
                offset = nextOffset
            )
            val moreItems = result.getOrDefault(emptyList())
            _navState.update { state ->
                val merged = (state.onlineSearchResults + moreItems).distinctBy { it.id }
                state.copy(
                    isLoadingMoreSearch = false,
                    searchOffset = nextOffset + 35,
                    onlineSearchResults = merged,
                    statusBannerMessage = if (moreItems.isNotEmpty()) {
                        "Loaded +${moreItems.size} more songs (សរុប ${merged.size} បទ · មិនកំណត់)"
                    } else {
                        "All matching songs loaded (${merged.size} tracks)"
                    }
                )
            }
        }
    }

    fun selectSearchCategory(category: SearchCategory) {
        _navState.update { it.copy(searchCategory = category) }
        val q = _navState.value.searchQuery.trim()
        if (q.isNotEmpty()) {
            updateSearchQuery(q)
        } else if (category == SearchCategory.TRENDING) {
            refreshTrendingNewCatalog(_navState.value.selectedTrendingFilter, silent = true)
        }
    }

    fun selectTrendingFilter(filter: TrendingFeedFilter) {
        _navState.update { it.copy(selectedTrendingFilter = filter) }
        refreshTrendingNewCatalog(filter, silent = true)
    }

    fun refreshTrendingNewCatalog(
        filter: TrendingFeedFilter = _navState.value.selectedTrendingFilter,
        silent: Boolean = false
    ) {
        viewModelScope.launch {
            _navState.update { it.copy(isRefreshingTrending = true) }
            val result = repository.fetchRealWorldTrending(filter)
            val fetched = result.getOrDefault(emptyList())
            _navState.update {
                it.copy(
                    isRefreshingTrending = false,
                    worldTrendingResults = if (fetched.isNotEmpty()) fetched else it.worldTrendingResults,
                    statusBannerMessage = if (!silent && fetched.isNotEmpty()) {
                        "Updated World Trending · ${filter.label} (${fetched.size} global chart tracks)"
                    } else it.statusBannerMessage
                )
            }
        }
    }

    fun openQuickTrendingNew() {
        _navState.update {
            it.copy(
                isTrendingViewOpen = true,
                isFavoritesViewOpen = false,
                isHistoryViewOpen = false
            )
        }
        refreshTrendingNewCatalog(_navState.value.selectedTrendingFilter, silent = true)
    }

    fun selectLibrarySubTab(subTab: LibrarySubTab) {
        _navState.update { it.copy(librarySubTab = subTab, selectedPlaylistId = null) }
    }

    fun updateLibraryQuery(query: String) {
        _navState.update { it.copy(libraryQuery = query) }
    }

    fun openPlaylistDetail(playlistId: Long?) {
        _navState.update { it.copy(selectedPlaylistId = playlistId) }
    }

    fun openQuickFavorites() {
        _navState.update {
            it.copy(
                isFavoritesViewOpen = true,
                isHistoryViewOpen = false,
                isTrendingViewOpen = false
            )
        }
    }

    fun openQuickHistory() {
        _navState.update {
            it.copy(
                isHistoryViewOpen = true,
                isFavoritesViewOpen = false,
                isTrendingViewOpen = false
            )
        }
    }

    fun openQuickPlaylists() {
        _navState.update {
            it.copy(
                currentTab = LumoDestination.LIBRARY,
                librarySubTab = LibrarySubTab.PLAYLISTS,
                isTrendingViewOpen = false,
                isFavoritesViewOpen = false,
                isHistoryViewOpen = false
            )
        }
    }

    fun openQuickDownloads() {
        _navState.update {
            it.copy(
                currentTab = LumoDestination.DOWNLOADS,
                isTrendingViewOpen = false,
                isFavoritesViewOpen = false,
                isHistoryViewOpen = false
            )
        }
    }

    fun closeSecondaryViews() {
        _navState.update {
            it.copy(
                isTrendingViewOpen = false,
                isFavoritesViewOpen = false,
                isHistoryViewOpen = false,
                selectedPlaylistId = null,
                isSettingsOpen = false
            )
        }
    }

    fun setSettingsOpen(open: Boolean) {
        _navState.update { it.copy(isSettingsOpen = open) }
    }

    fun openDownloadSheet(media: MediaItemEntity?) {
        _navState.update { it.copy(downloadPromptMedia = media, trackOptionsMedia = null) }
    }

    fun openTrackOptionsSheet(media: MediaItemEntity?) {
        _navState.update { it.copy(trackOptionsMedia = media) }
    }

    fun openAddToPlaylistSheet(media: MediaItemEntity?) {
        _navState.update { it.copy(addToPlaylistMedia = media, trackOptionsMedia = null) }
    }

    fun openRenameDownloadDialog(task: DownloadTaskEntity?) {
        _navState.update { it.copy(renameDownloadTarget = task) }
    }

    fun showStatusMessage(message: String?) {
        _navState.update { it.copy(statusBannerMessage = message) }
    }

    fun toggleFavorite(media: MediaItemEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(media)
            val newFav = !media.isFavorite
            _navState.update { state ->
                state.copy(
                    worldTrendingResults = state.worldTrendingResults.map {
                        if (it.id == media.id) it.copy(isFavorite = newFav) else it
                    },
                    onlineSearchResults = state.onlineSearchResults.map {
                        if (it.id == media.id) it.copy(isFavorite = newFav) else it
                    }
                )
            }
            val msg = if (newFav) "Added \"${media.title}\" to Favorites" else "Removed \"${media.title}\" from Favorites"
            showStatusMessage(msg)
        }
    }

    fun removeTrackFromCatalog(media: MediaItemEntity) {
        viewModelScope.launch {
            repository.removeMediaItem(media.id)
            _navState.update {
                it.copy(
                    trackOptionsMedia = null,
                    statusBannerMessage = "Removed \"${media.title}\" from your library"
                )
            }
        }
    }

    fun startAuthorizedDownload(media: MediaItemEntity, formatOption: DownloadFormatOption) {
        repository.startOrResumeDownload(media, formatOption) { _, finishMessage ->
            showStatusMessage(finishMessage)
        }
        _navState.update {
            it.copy(
                downloadPromptMedia = null,
                statusBannerMessage = "Downloading \"${media.title}\" (${formatOption.label})..."
            )
        }
    }

    fun pauseOrResumeDownload(task: DownloadTaskEntity) {
        val media = allMedia.value.find { it.id == task.mediaId }
        repository.pauseOrResumeExistingDownload(task, media)
    }

    fun renameDownload(taskId: String, newTitle: String) {
        viewModelScope.launch {
            repository.renameDownload(taskId, newTitle)
            _navState.update {
                it.copy(
                    renameDownloadTarget = null,
                    statusBannerMessage = "Renamed download to \"$newTitle\""
                )
            }
        }
    }

    fun deleteDownload(task: DownloadTaskEntity) {
        viewModelScope.launch {
            repository.deleteDownload(task)
            showStatusMessage("Deleted \"${task.title}\" (${task.format})")
        }
    }

    fun clearAllCompletedDownloads() {
        viewModelScope.launch {
            repository.clearAllCompletedDownloads()
            showStatusMessage("Cleared downloaded files cache")
        }
    }

    fun cleanAppClutter(
        clearUnsavedSearchCache: Boolean = true,
        clearHistory: Boolean = false,
        clearIncompleteDownloads: Boolean = true
    ) {
        viewModelScope.launch {
            val cleaned = repository.cleanAppClutter(
                clearUnsavedSearchCache = clearUnsavedSearchCache,
                clearHistory = clearHistory,
                clearIncompleteDownloads = clearIncompleteDownloads
            )
            showStatusMessage("✨ App cleaned ($cleaned cached items removed · Downloads & Playlists safe)")
        }
    }

    fun openPlaylistDetailFromHome(playlistId: Long) {
        _navState.update {
            it.copy(
                currentTab = LumoDestination.LIBRARY,
                librarySubTab = LibrarySubTab.PLAYLISTS,
                selectedPlaylistId = playlistId,
                isTrendingViewOpen = false,
                isFavoritesViewOpen = false,
                isHistoryViewOpen = false
            )
        }
    }

    fun saveCustomPhotoUriToFile(uri: android.net.Uri, onSaved: (String) -> Unit) {
        viewModelScope.launch {
            val savedPath = repository.saveCustomPlaylistCoverFromUri(uri)
            if (!savedPath.isNullOrBlank()) {
                onSaved(savedPath)
                showStatusMessage("Custom photo selected · បានជ្រើសរើសរូបភាព")
            } else {
                showStatusMessage("Could not load selected photo")
            }
        }
    }

    fun updatePlaylistPhotoFromUri(playlist: PlaylistEntity, uri: android.net.Uri) {
        viewModelScope.launch {
            val savedPath = repository.saveCustomPlaylistCoverFromUri(uri)
            if (!savedPath.isNullOrBlank()) {
                repository.updatePlaylistCover(
                    playlistId = playlist.id,
                    coverResName = playlist.coverResName,
                    coverUrl = savedPath
                )
                showStatusMessage("Updated custom photo for \"${playlist.name}\" · បានប្តូររូបភាព")
            } else {
                showStatusMessage("Could not load selected photo")
            }
        }
    }

    fun updatePlaylistCustomCover(
        playlist: PlaylistEntity,
        coverResName: String = playlist.coverResName,
        coverUrl: String = ""
    ) {
        viewModelScope.launch {
            repository.updatePlaylistCover(
                playlistId = playlist.id,
                coverResName = coverResName,
                coverUrl = coverUrl
            )
            showStatusMessage("Updated cover for \"${playlist.name}\"")
        }
    }

    fun createPlaylist(
        name: String,
        description: String,
        coverResName: String,
        coverUrl: String = "",
        initialTrackId: String? = null
    ) {
        viewModelScope.launch {
            val initialIds = if (initialTrackId != null) listOf(initialTrackId) else emptyList()
            repository.createPlaylist(name, description, coverResName, coverUrl, initialIds)
            showStatusMessage("Created playlist \"$name\" · បានបង្កើតបញ្ជីចាក់")
        }
    }

    fun renamePlaylist(playlistId: Long, newName: String, newDescription: String) {
        viewModelScope.launch {
            repository.renamePlaylist(playlistId, newName, newDescription)
            showStatusMessage("Updated playlist \"$newName\"")
        }
    }

    fun cyclePlaylistArtwork(playlist: PlaylistEntity) {
        val covers = listOf(
            "img_album_neon_echoes",
            "img_album_velvet_drift",
            "img_album_solar_pulse",
            "img_video_aurora_session"
        )
        val currentIdx = covers.indexOf(playlist.coverResName)
        val nextCover = covers[(currentIdx + 1) % covers.size]
        viewModelScope.launch {
            repository.updatePlaylistCover(playlist.id, nextCover, coverUrl = "")
            showStatusMessage("Updated artwork for \"${playlist.name}\"")
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            _navState.update { it.copy(selectedPlaylistId = null) }
            showStatusMessage("Playlist deleted")
        }
    }

    fun addTrackToPlaylist(playlistId: Long, media: MediaItemEntity) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, media)
            _navState.update {
                it.copy(
                    addToPlaylistMedia = null,
                    statusBannerMessage = "Added \"${media.title}\" to playlist"
                )
            }
        }
    }

    fun removeTrackFromPlaylist(playlistId: Long, mediaId: String) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, mediaId)
            showStatusMessage("Removed track from playlist")
        }
    }

    fun moveTrackInPlaylist(playlistId: Long, fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            repository.moveTrackInPlaylist(playlistId, fromIndex, toIndex)
        }
    }

    fun selectTheme(theme: LumoThemeOption) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(themeId = theme.id) }
            showStatusMessage("Theme switched to ${theme.numberLabel} — ${theme.displayName}")
        }
    }

    fun selectLanguage(language: LumoLanguage) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(appLanguage = language.id) }
            showStatusMessage("ភាសា / Language: ${language.displayName}")
        }
    }

    fun cycleLanguage() {
        val nextLang = settings.value.languageOption.next()
        selectLanguage(nextLang)
    }

    fun setColorReactiveIntensity(intensity: com.example.ui.theme.ColorReactiveIntensity) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(colorReactiveIntensity = intensity.id) }
            showStatusMessage("Color Reactive Engine: ${intensity.label} (${intensity.description})")
        }
    }

    fun cycleColorReactiveIntensity() {
        val current = settings.value.reactiveIntensity
        setColorReactiveIntensity(current.next())
    }

    fun openEditMetadataDialog(media: MediaItemEntity?) {
        _navState.update { it.copy(editMetadataMedia = media, trackOptionsMedia = null) }
    }

    fun saveEditedMediaMetadata(updated: MediaItemEntity) {
        viewModelScope.launch {
            repository.updateMediaMetadata(updated)
            _navState.update {
                it.copy(
                    editMetadataMedia = null,
                    statusBannerMessage = "Saved metadata for \"${updated.title}\""
                )
            }
        }
    }

    fun updateSettings(transform: (LumoSettingsEntity) -> LumoSettingsEntity) {
        viewModelScope.launch {
            repository.updateSettings(transform)
        }
    }

    override fun onCleared() {
        playerController.releaseMediaPlayer()
        super.onCleared()
    }
}
