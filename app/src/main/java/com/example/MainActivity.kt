package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.LumoViewModel
import com.example.ui.components.AddToPlaylistBottomSheet
import com.example.ui.components.LumoAdaptiveSideRail
import com.example.ui.components.LumoDownloadBottomSheet
import com.example.ui.components.LumoMiniPlayer
import com.example.ui.components.LumoSignatureBottomBar
import com.example.ui.components.LumoTrackOptionsBottomSheet
import com.example.ui.components.RenameDownloadDialog
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.MusicPlayerScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SongMetadataEditorDialog
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.theme.LocalLumoPalette
import com.example.ui.theme.LumoDestination
import com.example.ui.theme.LumoTheme
import com.example.ui.theme.rememberLumoColorReactiveState
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LumoAppRoot()
        }
    }
}

@Composable
fun LumoAppRoot(
    viewModel: LumoViewModel = viewModel()
) {
    val navState by viewModel.navState.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val allMedia by viewModel.allMedia.collectAsStateWithLifecycle()
    val favoriteMedia by viewModel.favoriteMedia.collectAsStateWithLifecycle()
    val historyMedia by viewModel.historyMedia.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val allDownloads by viewModel.allDownloads.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    LaunchedEffect(navState.statusBannerMessage) {
        if (navState.statusBannerMessage != null) {
            delay(2800L)
            viewModel.showStatusMessage(null)
        }
    }

    BackHandler(
        enabled = !playerState.isFullPlayerOpen &&
            !playerState.isVideoPlayerOpen &&
            !navState.isSettingsOpen &&
            !navState.isTrendingViewOpen &&
            !navState.isFavoritesViewOpen &&
            !navState.isHistoryViewOpen &&
            navState.selectedPlaylistId == null &&
            navState.currentTab != LumoDestination.HOME
    ) {
        viewModel.selectTab(LumoDestination.HOME)
    }

    val colorReactiveState = rememberLumoColorReactiveState(
        currentMedia = playerState.currentMedia,
        isPlaying = playerState.isPlaying,
        positionSec = if (playerState.isFullPlayerOpen) playerState.positionSec else 0,
        durationSec = playerState.durationSec,
        liveAudioBass = playerState.liveAudioBass,
        liveAudioEnergy = playerState.liveAudioEnergy,
        intensityMode = settings.reactiveIntensity,
        isFullPlayerActive = playerState.isFullPlayerOpen
    )

    LumoTheme(
        themeOption = settings.themeOption,
        activeDestination = navState.currentTab,
        colorReactiveState = colorReactiveState,
        language = settings.languageOption
    ) {
        val palette = LocalLumoPalette.current
        val activeDownloadsCount = remember(allDownloads) { allDownloads.count { it.status != "COMPLETED" } }
        val completedDownloads = remember(allDownloads) { allDownloads.filter { it.status == "COMPLETED" } }
        val currentTabAccent = colorReactiveState.accentColor

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.background)
                .drawBehind {
                    // Ambient music-reactive gradient across the main app backdrop
                    val glowAlpha = colorReactiveState.ambientGlowAlpha
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                colorReactiveState.dominantColor.copy(alpha = (glowAlpha * 0.95f).coerceIn(0.05f, 0.36f)),
                                colorReactiveState.secondaryColor.copy(alpha = (glowAlpha * 0.60f).coerceIn(0.03f, 0.24f)),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.14f),
                            radius = size.maxDimension * (0.64f * colorReactiveState.beatScaleExpansion)
                        )
                    )
                }
        ) {
            val isTabletOrWide = this.maxWidth >= 600.dp

            if (isTabletOrWide) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    LumoAdaptiveSideRail(
                        currentDestination = navState.currentTab,
                        activeDownloadsCount = activeDownloadsCount,
                        onSelectDestination = { viewModel.selectTab(it) }
                    )

                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        MainTabContentArea(
                            viewModel = viewModel,
                            allMedia = allMedia,
                            favoriteMedia = favoriteMedia,
                            historyMedia = historyMedia,
                            playlists = playlists,
                            allDownloads = allDownloads,
                            completedDownloads = completedDownloads
                        )

                        LumoMiniPlayer(
                            media = playerState.currentMedia,
                            isVisible = playerState.isMiniPlayerVisible && !navState.isSettingsOpen,
                            isPlaying = playerState.isPlaying,
                            positionSec = playerState.positionSec,
                            durationSec = playerState.durationSec,
                            accentColor = currentTabAccent,
                            onTapExpand = { viewModel.playerController.openFullPlayer() },
                            onTogglePlayPause = { viewModel.playerController.togglePlayPause() },
                            onSkipPrevious = { viewModel.playerController.skipPrevious() },
                            onSkipNext = { viewModel.playerController.skipNext() },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onDismiss = { viewModel.playerController.dismissMiniPlayer() },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                        )
                    }
                }
            } else {
                Scaffold(
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            LumoMiniPlayer(
                                media = playerState.currentMedia,
                                isVisible = playerState.isMiniPlayerVisible && !navState.isSettingsOpen,
                                isPlaying = playerState.isPlaying,
                                positionSec = playerState.positionSec,
                                durationSec = playerState.durationSec,
                                accentColor = currentTabAccent,
                                onTapExpand = { viewModel.playerController.openFullPlayer() },
                                onTogglePlayPause = { viewModel.playerController.togglePlayPause() },
                                onSkipPrevious = { viewModel.playerController.skipPrevious() },
                                onSkipNext = { viewModel.playerController.skipNext() },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onDismiss = { viewModel.playerController.dismissMiniPlayer() }
                            )

                            LumoSignatureBottomBar(
                                currentDestination = navState.currentTab,
                                activeDownloadsCount = activeDownloadsCount,
                                onSelectDestination = { viewModel.selectTab(it) }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .padding(bottom = innerPadding.calculateBottomPadding() * 0.1f)
                    ) {
                        MainTabContentArea(
                            viewModel = viewModel,
                            allMedia = allMedia,
                            favoriteMedia = favoriteMedia,
                            historyMedia = historyMedia,
                            playlists = playlists,
                            allDownloads = allDownloads,
                            completedDownloads = completedDownloads
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = navState.isSettingsOpen,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                SettingsScreen(
                    settings = settings,
                    completedDownloads = completedDownloads,
                    onBack = { viewModel.setSettingsOpen(false) },
                    onSelectTheme = { viewModel.selectTheme(it) },
                    onUpdateSettings = { transform ->
                        viewModel.updateSettings(transform)
                    },
                    onManageClearStorage = { viewModel.clearAllCompletedDownloads() },
                    modifier = Modifier.statusBarsPadding()
                )
            }

            AnimatedVisibility(
                visible = playerState.isFullPlayerOpen,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                MusicPlayerScreen(
                    playerState = playerState,
                    onClose = { viewModel.playerController.closeFullOverlays() },
                    onTogglePlayPause = { viewModel.playerController.togglePlayPause() },
                    onSkipPrevious = { viewModel.playerController.skipPrevious() },
                    onSkipNext = { viewModel.playerController.skipNext() },
                    onSeekToSec = { viewModel.playerController.seekTo(it) },
                    onToggleShuffle = { viewModel.playerController.toggleShuffle() },
                    onCycleRepeat = { viewModel.playerController.cycleRepeatMode() },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onOpenDownloadSheet = { viewModel.openDownloadSheet(it) },
                    onOpenTrackOptions = { viewModel.openTrackOptionsSheet(it) },
                    onOpenAddToPlaylist = { viewModel.openAddToPlaylistSheet(it) },
                    onSelectQueueTrack = {
                        viewModel.playerController.playMedia(it, playerState.queue, openFullExperience = true)
                    },
                    onMoveQueueItem = { from, to -> viewModel.playerController.moveQueueItem(from, to) },
                    onRemoveQueueItem = { viewModel.playerController.removeFromQueue(it) },
                    onClearQueue = { viewModel.playerController.clearQueueExceptCurrent() },
                    onToggleVisualizer = { viewModel.playerController.toggleVisualizer() },
                    onSelectEqPreset = { viewModel.playerController.setEqualizerPreset(it) },
                    onSelectSleepTimer = { viewModel.playerController.setSleepTimer(it) },
                    onSwitchToVideoPlayer = { viewModel.playerController.openVideoPlayerExplicitly() },
                    onChangeSpeed = { viewModel.playerController.setPlaybackSpeed(it) },
                    onCycleReactiveIntensity = { viewModel.cycleColorReactiveIntensity() }
                )
            }

            AnimatedVisibility(
                visible = playerState.isVideoPlayerOpen,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                VideoPlayerScreen(
                    playerState = playerState,
                    onClose = { viewModel.playerController.closeFullOverlays() },
                    onTogglePlayPause = { viewModel.playerController.togglePlayPause() },
                    onSkipPrevious = { viewModel.playerController.skipPrevious() },
                    onSkipNext = { viewModel.playerController.skipNext() },
                    onSeekToSec = { viewModel.playerController.seekTo(it) },
                    onToggleSubtitles = { viewModel.playerController.toggleSubtitles() },
                    onChangeSpeed = { viewModel.playerController.setPlaybackSpeed(it) },
                    onChangeBrightness = { viewModel.playerController.setVideoBrightness(it) },
                    onChangeVolume = { viewModel.playerController.setVideoVolume(it) },
                    onToggleLandscapeLayout = { viewModel.playerController.toggleLandscapeVideoLayout() },
                    onSwitchToAudioPlayer = { viewModel.playerController.openAudioPlayerExplicitly() },
                    onOpenDownloadSheet = { viewModel.openDownloadSheet(it) },
                    onOpenTrackOptions = { viewModel.openTrackOptionsSheet(it) }
                )
            }

            AnimatedVisibility(
                visible = navState.statusBannerMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 12.dp, start = 24.dp, end = 24.dp)
            ) {
                navState.statusBannerMessage?.let { msg ->
                    Surface(
                        color = palette.elevatedSurface,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 10.dp,
                        modifier = Modifier
                            .border(1.dp, currentTabAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .clickable { viewModel.showStatusMessage(null) }
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.primaryText,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                        )
                    }
                }
            }

            navState.downloadPromptMedia?.let { targetMedia ->
                LumoDownloadBottomSheet(
                    media = targetMedia,
                    defaultFormatId = settings.defaultDownloadFormat,
                    onDismiss = { viewModel.openDownloadSheet(null) },
                    onConfirmDownload = { formatOption ->
                        viewModel.startAuthorizedDownload(targetMedia, formatOption)
                    }
                )
            }

            navState.trackOptionsMedia?.let { targetMedia ->
                LumoTrackOptionsBottomSheet(
                    media = targetMedia,
                    onDismiss = { viewModel.openTrackOptionsSheet(null) },
                    onPlayNow = {
                        viewModel.playerController.playMedia(targetMedia, allMedia, openFullExperience = false)
                    },
                    onOpenDetailsPlayer = {
                        viewModel.playerController.playMedia(targetMedia, allMedia, openFullExperience = true)
                    },
                    onPlayNextInQueue = {
                        viewModel.playerController.playNextInQueue(targetMedia)
                        viewModel.showStatusMessage("Playing next: ${targetMedia.title}")
                    },
                    onAddToPlaylist = {
                        viewModel.openAddToPlaylistSheet(targetMedia)
                    },
                    onToggleFavorite = {
                        viewModel.toggleFavorite(targetMedia)
                    },
                    onDownload = {
                        viewModel.openDownloadSheet(targetMedia)
                    },
                    onEditMetadata = {
                        viewModel.openEditMetadataDialog(targetMedia)
                    },
                    onRemoveFromApp = {
                        viewModel.removeTrackFromCatalog(targetMedia)
                    }
                )
            }

            navState.editMetadataMedia?.let { targetMedia ->
                SongMetadataEditorDialog(
                    media = targetMedia,
                    onDismiss = { viewModel.openEditMetadataDialog(null) },
                    onSaveChanges = { updatedMedia ->
                        viewModel.saveEditedMediaMetadata(updatedMedia)
                    }
                )
            }

            navState.addToPlaylistMedia?.let { targetMedia ->
                AddToPlaylistBottomSheet(
                    media = targetMedia,
                    playlists = playlists,
                    onDismiss = { viewModel.openAddToPlaylistSheet(null) },
                    onSelectPlaylist = { playlist ->
                        viewModel.addTrackToPlaylist(playlist.id, targetMedia)
                    },
                    onCreateNewPlaylistWithTrack = { name, coverRes ->
                        viewModel.createPlaylist(
                            name = name,
                            description = "Created with ${targetMedia.title}",
                            coverResName = coverRes,
                            coverUrl = targetMedia.artworkUrl,
                            initialTrackId = targetMedia.id
                        )
                        viewModel.openAddToPlaylistSheet(null)
                    }
                )
            }

            navState.renameDownloadTarget?.let { targetTask ->
                RenameDownloadDialog(
                    task = targetTask,
                    onDismiss = { viewModel.openRenameDownloadDialog(null) },
                    onConfirmRename = { newTitle ->
                        viewModel.renameDownload(targetTask.id, newTitle)
                    }
                )
            }
        }
    }
}

@Composable
private fun MainTabContentArea(
    viewModel: LumoViewModel,
    allMedia: List<com.example.data.MediaItemEntity>,
    favoriteMedia: List<com.example.data.MediaItemEntity>,
    historyMedia: List<com.example.data.MediaItemEntity>,
    playlists: List<com.example.data.PlaylistEntity>,
    allDownloads: List<com.example.data.DownloadTaskEntity>,
    completedDownloads: List<com.example.data.DownloadTaskEntity>
) {
    val navState by viewModel.navState.collectAsStateWithLifecycle()
    val currentPlayingMediaId by viewModel.currentPlayingMediaId.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = navState.currentTab,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "mainTabTransition"
    ) { tab ->
        when (tab) {
            LumoDestination.HOME -> {
                val homePlayableCatalog = remember(navState.worldTrendingResults, allMedia) {
                    (navState.worldTrendingResults + allMedia).distinctBy { it.id }
                }
                HomeScreen(
                    allMedia = allMedia,
                    worldTrendingMedia = navState.worldTrendingResults,
                    completedDownloads = completedDownloads,
                    playlists = playlists,
                    currentPlayingMediaId = currentPlayingMediaId,
                    favoriteMedia = favoriteMedia,
                    historyMedia = historyMedia,
                    isLoadingCatalog = navState.isSearchingOnline,
                    selectedTrendingFilter = navState.selectedTrendingFilter,
                    isRefreshingTrending = navState.isRefreshingTrending,
                    isTrendingSubViewOpen = navState.isTrendingViewOpen,
                    isFavoritesSubViewOpen = navState.isFavoritesViewOpen,
                    isHistorySubViewOpen = navState.isHistoryViewOpen,
                    onCloseSubView = { viewModel.closeSecondaryViews() },
                    onSelectTrendingFilter = { viewModel.selectTrendingFilter(it) },
                    onRefreshTrendingNew = { viewModel.refreshTrendingNewCatalog(silent = false) },
                    onOpenTrendingNewQuick = { viewModel.openQuickTrendingNew() },
                    onPlayMedia = { media, openFull ->
                        viewModel.playerController.playMedia(media, homePlayableCatalog, openFullExperience = openFull)
                    },
                    onPlayDownloadedQueue = { media, queue, openFull ->
                        viewModel.playerController.playMedia(
                            media,
                            queue.ifEmpty { homePlayableCatalog },
                            openFullExperience = openFull
                        )
                    },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onOpenDownloadSheet = { viewModel.openDownloadSheet(it) },
                    onOpenTrackOptions = { viewModel.openTrackOptionsSheet(it) },
                    onOpenYouTubeLinkDownloader = { viewModel.openYouTubeLinkDownloader() },
                    onOpenSearchAndDownload = {
                        viewModel.setLinkDownloaderMode(false)
                        viewModel.selectTab(LumoDestination.SEARCH)
                    },
                    onOpenFavoritesQuick = { viewModel.openQuickFavorites() },
                    onOpenDownloadsQuick = { viewModel.openQuickDownloads() },
                    onOpenPlaylistsQuick = { viewModel.openQuickPlaylists() },
                    onOpenPlaylistDetail = { playlistId ->
                        viewModel.openPlaylistDetailFromHome(playlistId)
                    },
                    onCreateCustomPlaylist = { name, desc, coverRes, coverUrl ->
                        viewModel.createPlaylist(name, desc, coverRes, coverUrl)
                    },
                    onPickGalleryPhotoUri = { uri, onSaved ->
                        viewModel.saveCustomPhotoUriToFile(uri, onSaved)
                    },
                    onUpdatePlaylistPhotoUri = { playlist, uri ->
                        viewModel.updatePlaylistPhotoFromUri(playlist, uri)
                    },
                    onUpdatePlaylistCustomCover = { playlist, coverRes, coverUrl ->
                        viewModel.updatePlaylistCustomCover(playlist, coverRes, coverUrl)
                    },
                    onOpenHistoryQuick = { viewModel.openQuickHistory() },
                    onOpenSettings = { viewModel.setSettingsOpen(true) },
                    onCycleReactiveIntensity = { viewModel.cycleColorReactiveIntensity() },
                    onCycleLanguage = { viewModel.cycleLanguage() }
                )
            }

            LumoDestination.SEARCH -> {
                SearchScreen(
                    allMedia = allMedia,
                    worldTrendingMedia = navState.worldTrendingResults,
                    onlineSearchResults = navState.onlineSearchResults,
                    currentPlayingMediaId = currentPlayingMediaId,
                    searchQuery = navState.searchQuery,
                    isSearchingOnline = navState.isSearchingOnline,
                    isLoadingMoreSearch = navState.isLoadingMoreSearch,
                    selectedCategory = navState.searchCategory,
                    isLinkDownloaderMode = navState.isLinkDownloaderMode,
                    youtubeLinkInput = navState.youtubeLinkInput,
                    isResolvingYouTubeLink = navState.isResolvingYouTubeLink,
                    resolvedYouTubeMedia = navState.resolvedYouTubeMedia,
                    onSetLinkDownloaderMode = { viewModel.setLinkDownloaderMode(it) },
                    onYouTubeLinkInputChange = { viewModel.updateYouTubeLinkInput(it) },
                    onResolveYouTubeLink = { url, autoFormat ->
                        viewModel.resolveYouTubeLink(url, autoFormat)
                    },
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    onLoadMoreOnlineSearch = { viewModel.loadMoreOnlineSearchResults() },
                    onSelectCategory = { viewModel.selectSearchCategory(it) },
                    onPlayMedia = { media, openFull ->
                        val activeQueue = if (navState.onlineSearchResults.isNotEmpty()) {
                            (listOf(media) + navState.onlineSearchResults).distinctBy { it.id }
                        } else {
                            (listOf(media) + navState.worldTrendingResults + allMedia).distinctBy { it.id }
                        }
                        viewModel.playerController.playMedia(media, activeQueue, openFullExperience = openFull)
                    },
                    onQuickDownloadFormat = { media, format ->
                        viewModel.startAuthorizedDownload(media, format)
                    },
                    onOpenDownloadSheet = { viewModel.openDownloadSheet(it) },
                    onOpenTrackOptions = { viewModel.openTrackOptionsSheet(it) }
                )
            }

            LumoDestination.LIBRARY -> {
                LibraryScreen(
                    allMedia = allMedia,
                    currentPlayingMediaId = currentPlayingMediaId,
                    playlists = playlists,
                    completedDownloads = completedDownloads,
                    selectedSubTab = navState.librarySubTab,
                    libraryQuery = navState.libraryQuery,
                    selectedPlaylistId = navState.selectedPlaylistId,
                    onSelectSubTab = { viewModel.selectLibrarySubTab(it) },
                    onQueryChange = { viewModel.updateLibraryQuery(it) },
                    onOpenPlaylistDetail = { viewModel.openPlaylistDetail(it) },
                    onCreatePlaylist = { name, desc, cover ->
                        viewModel.createPlaylist(name, desc, cover)
                    },
                    onCreateCustomPlaylist = { name, desc, coverRes, coverUrl ->
                        viewModel.createPlaylist(name, desc, coverRes, coverUrl)
                    },
                    onPickGalleryPhotoUri = { uri, onSaved ->
                        viewModel.saveCustomPhotoUriToFile(uri, onSaved)
                    },
                    onUpdatePlaylistPhotoUri = { playlist, uri ->
                        viewModel.updatePlaylistPhotoFromUri(playlist, uri)
                    },
                    onUpdatePlaylistCustomCover = { playlist, coverRes, coverUrl ->
                        viewModel.updatePlaylistCustomCover(playlist, coverRes, coverUrl)
                    },
                    onRenamePlaylist = { id, name, desc ->
                        viewModel.renamePlaylist(id, name, desc)
                    },
                    onCyclePlaylistCover = { viewModel.cyclePlaylistArtwork(it) },
                    onDeletePlaylist = { viewModel.deletePlaylist(it) },
                    onMoveTrackInPlaylist = { id, from, to ->
                        viewModel.moveTrackInPlaylist(id, from, to)
                    },
                    onRemoveTrackFromPlaylist = { id, mediaId ->
                        viewModel.removeTrackFromPlaylist(id, mediaId)
                    },
                    onAddTrackToPlaylist = { id, media ->
                        viewModel.addTrackToPlaylist(id, media)
                    },
                    onPlayMedia = { media, customQueue, openFull ->
                        viewModel.playerController.playMedia(media, customQueue ?: allMedia, openFull)
                    },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onOpenDownloadSheet = { viewModel.openDownloadSheet(it) },
                    onOpenTrackOptions = { viewModel.openTrackOptionsSheet(it) }
                )
            }

            LumoDestination.DOWNLOADS -> {
                DownloadsScreen(
                    allDownloads = allDownloads,
                    allMedia = allMedia,
                    youtubeLinkInput = navState.youtubeLinkInput,
                    isResolvingYouTubeLink = navState.isResolvingYouTubeLink,
                    onYouTubeLinkInputChange = { viewModel.updateYouTubeLinkInput(it) },
                    onResolveAndDownloadYouTubeLink = { url, format ->
                        viewModel.resolveYouTubeLink(url, format)
                    },
                    onOpenSearchToDownload = {
                        viewModel.setLinkDownloaderMode(false)
                        viewModel.selectTab(LumoDestination.SEARCH)
                    },
                    onPauseOrResume = { viewModel.pauseOrResumeDownload(it) },
                    onPlayDownloaded = { task ->
                        val media = allMedia.find { it.id == task.mediaId } ?: com.example.data.MediaItemEntity(
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
                        viewModel.playerController.playMedia(media, allMedia, openFullExperience = false)
                    },
                    onOpenDownloadedFull = { task ->
                        val media = allMedia.find { it.id == task.mediaId } ?: com.example.data.MediaItemEntity(
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
                        viewModel.playerController.playMedia(media, allMedia, openFullExperience = true)
                    },
                    onRenameDownload = { viewModel.openRenameDownloadDialog(it) },
                    onDeleteDownload = { viewModel.deleteDownload(it) },
                    onAddToPlaylist = { task ->
                        val media = allMedia.find { it.id == task.mediaId }
                        if (media != null) {
                            viewModel.openAddToPlaylistSheet(media)
                        }
                    },
                    onOpenDownloadPickerForNew = { viewModel.openDownloadSheet(it) }
                )
            }
        }
    }
}
