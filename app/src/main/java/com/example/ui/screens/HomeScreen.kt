package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DownloadTaskEntity
import com.example.data.KhmerArtistDirectory
import com.example.data.MediaItemEntity
import com.example.data.PlaylistEntity
import com.example.data.TrendingFeedFilter
import com.example.ui.components.CreateOrEditCustomPlaylistDialog
import com.example.ui.components.CustomPlaylistPhotoPickerDialog
import com.example.ui.components.LumoArtworkImage
import com.example.ui.theme.LocalColorReactiveState
import com.example.ui.theme.LocalLumoLanguage
import com.example.ui.theme.LocalLumoPalette
import com.example.ui.theme.LumoIcons
import java.io.File
import java.util.Locale

private enum class HomeDownloadFilter(val kmLabel: String, val enLabel: String) {
    ALL("ទាំងអស់", "All"),
    AUDIO("បទចម្រៀង", "Music"),
    VIDEO("វីដេអូ", "Video"),
    FAVORITES("ចូលចិត្ត", "Favorites")
}

private enum class HomeDownloadSort(val kmLabel: String, val enLabel: String) {
    NEWEST("ថ្មីៗបំផុត", "Newest"),
    TITLE_AZ("ឈ្មោះ A–Z", "Title A–Z"),
    ARTIST("តារាចម្រៀង", "Artist");

    fun next(): HomeDownloadSort = when (this) {
        NEWEST -> TITLE_AZ
        TITLE_AZ -> ARTIST
        ARTIST -> NEWEST
    }
}

private data class MadeForYouMix(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String,
    val accent: Color
)

@Composable
fun HomeScreen(
    allMedia: List<MediaItemEntity>,
    worldTrendingMedia: List<MediaItemEntity> = emptyList(),
    completedDownloads: List<DownloadTaskEntity> = emptyList(),
    playlists: List<PlaylistEntity> = emptyList(),
    currentPlayingMediaId: String?,
    favoriteMedia: List<MediaItemEntity>,
    historyMedia: List<MediaItemEntity>,
    isLoadingCatalog: Boolean,
    selectedTrendingFilter: TrendingFeedFilter = TrendingFeedFilter.ALL_TRENDING,
    isRefreshingTrending: Boolean = false,
    isTrendingSubViewOpen: Boolean = false,
    isFavoritesSubViewOpen: Boolean,
    isHistorySubViewOpen: Boolean,
    onCloseSubView: () -> Unit,
    onSelectTrendingFilter: (TrendingFeedFilter) -> Unit = {},
    onRefreshTrendingNew: () -> Unit = {},
    onOpenTrendingNewQuick: () -> Unit = {},
    onPlayMedia: (MediaItemEntity, Boolean) -> Unit,
    onPlayDownloadedQueue: (MediaItemEntity, List<MediaItemEntity>, Boolean) -> Unit = { m, _, openFull ->
        onPlayMedia(m, openFull)
    },
    onToggleFavorite: (MediaItemEntity) -> Unit,
    onOpenDownloadSheet: (MediaItemEntity) -> Unit,
    onOpenTrackOptions: (MediaItemEntity) -> Unit,
    onOpenYouTubeLinkDownloader: () -> Unit,
    onOpenSearchAndDownload: () -> Unit,
    onOpenFavoritesQuick: () -> Unit,
    onOpenDownloadsQuick: () -> Unit,
    onOpenPlaylistsQuick: () -> Unit,
    onOpenPlaylistDetail: (Long) -> Unit = {},
    onCreateCustomPlaylist: (name: String, desc: String, coverResName: String, coverUrl: String) -> Unit = { _, _, _, _ -> },
    onPickGalleryPhotoUri: (Uri, (String) -> Unit) -> Unit = { _, _ -> },
    onUpdatePlaylistPhotoUri: (PlaylistEntity, Uri) -> Unit = { _, _ -> },
    onUpdatePlaylistCustomCover: (PlaylistEntity, String, String) -> Unit = { _, _, _ -> },
    onOpenHistoryQuick: () -> Unit,
    onOpenSettings: () -> Unit,
    onCycleReactiveIntensity: () -> Unit = {},
    onCycleLanguage: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val palette = LocalLumoPalette.current
    val reactive = LocalColorReactiveState.current
    val language = LocalLumoLanguage.current

    var showDownloadedOnlyMode by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(HomeDownloadFilter.ALL) }
    var selectedSort by remember { mutableStateOf(HomeDownloadSort.NEWEST) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var playlistForPhotoPicker by remember { mutableStateOf<PlaylistEntity?>(null) }
    var selectedArtistPage by remember { mutableStateOf<String?>(null) }
    var selectedAlbumPage by remember { mutableStateOf<String?>(null) }
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showListeningStatsSheet by remember { mutableStateOf(false) }

    val trendingNewItems = remember(worldTrendingMedia, allMedia, selectedTrendingFilter) {
        val globalSource = worldTrendingMedia.ifEmpty {
            allMedia.filter { it.popularityScore >= 1_000L }
        }
        KhmerArtistDirectory.sortTrendingNewFirst(
            items = globalSource,
            filter = selectedTrendingFilter
        )
    }

    // Artist Page sub-screen (Section 22)
    selectedArtistPage?.let { artistName ->
        ArtistDetailScreen(
            artistName = artistName,
            allMedia = allMedia,
            currentPlayingMediaId = currentPlayingMediaId,
            onBack = { selectedArtistPage = null },
            onPlayMedia = { media, queue, openFull ->
                onPlayDownloadedQueue(media, queue, openFull)
            },
            onToggleFavorite = onToggleFavorite,
            onOpenDownloadSheet = onOpenDownloadSheet,
            onOpenTrackOptions = onOpenTrackOptions,
            onSelectRelatedArtist = { related -> selectedArtistPage = related },
            onOpenAlbum = { album ->
                selectedArtistPage = null
                selectedAlbumPage = album
            },
            modifier = modifier
        )
        return
    }

    // Album Page sub-screen (Section 23)
    selectedAlbumPage?.let { albumName ->
        AlbumDetailScreen(
            albumName = albumName,
            allMedia = allMedia,
            currentPlayingMediaId = currentPlayingMediaId,
            onBack = { selectedAlbumPage = null },
            onPlayMedia = { media, queue, openFull ->
                onPlayDownloadedQueue(media, queue, openFull)
            },
            onToggleFavorite = onToggleFavorite,
            onOpenDownloadSheet = onOpenDownloadSheet,
            onOpenTrackOptions = onOpenTrackOptions,
            onAddAlbumToPlaylist = { rep -> onOpenTrackOptions(rep) },
            modifier = modifier
        )
        return
    }

    if (isTrendingSubViewOpen) {
        BackHandler(onBack = onCloseSubView)
        TrendingNewSubScreen(
            trendingItems = trendingNewItems,
            selectedFilter = selectedTrendingFilter,
            isRefreshing = isRefreshingTrending,
            currentPlayingMediaId = currentPlayingMediaId,
            onBack = onCloseSubView,
            onSelectFilter = onSelectTrendingFilter,
            onRefresh = onRefreshTrendingNew,
            onPlayMedia = onPlayMedia,
            onToggleFavorite = onToggleFavorite,
            onDownload = onOpenDownloadSheet,
            onOpenTrackOptions = onOpenTrackOptions,
            modifier = modifier
        )
        return
    }

    if (isFavoritesSubViewOpen) {
        BackHandler(onBack = onCloseSubView)
        FavoritesSubScreen(
            favoriteMedia = favoriteMedia,
            onBack = onCloseSubView,
            onPlayMedia = onPlayMedia,
            onToggleFavorite = onToggleFavorite,
            onOpenTrackOptions = onOpenTrackOptions,
            modifier = modifier
        )
        return
    }

    if (isHistorySubViewOpen) {
        BackHandler(onBack = onCloseSubView)
        HistorySubScreen(
            historyMedia = historyMedia,
            onBack = onCloseSubView,
            onPlayMedia = onPlayMedia,
            onOpenTrackOptions = onOpenTrackOptions,
            modifier = modifier
        )
        return
    }

    // Build Downloaded Music items
    val downloadedMediaWithTasks = remember(completedDownloads, allMedia) {
        val byMediaId = LinkedHashMap<String, Pair<MediaItemEntity, DownloadTaskEntity?>>()

        completedDownloads.forEach { task ->
            val matched = allMedia.find { it.id == task.mediaId }
            val resolvedMedia = matched?.copy(
                localFilePath = task.localFilePath.ifBlank { matched.localFilePath },
                isVideo = task.isVideo || matched.isVideo
            ) ?: MediaItemEntity(
                id = task.mediaId,
                title = task.title,
                artist = task.artist,
                album = "Offline Download",
                durationSec = 210,
                isVideo = task.isVideo,
                artworkUrl = task.artworkUrl,
                artworkResName = task.artworkResName,
                streamUrl = task.remoteUrl,
                localFilePath = task.localFilePath,
                inLibrary = true
            )
            byMediaId[resolvedMedia.id] = resolvedMedia to task
        }

        allMedia.forEach { media ->
            if (!byMediaId.containsKey(media.id) && media.localFilePath.isNotBlank()) {
                val f = File(media.localFilePath)
                if (f.exists() && f.length() > 1024L) {
                    byMediaId[media.id] = media to null
                }
            }
        }

        byMediaId.values.toList()
    }

    val filteredDownloadedPairs = remember(downloadedMediaWithTasks, selectedFilter, selectedSort) {
        val filtered = downloadedMediaWithTasks.filter { (media, _) ->
            when (selectedFilter) {
                HomeDownloadFilter.ALL -> true
                HomeDownloadFilter.AUDIO -> !media.isVideo
                HomeDownloadFilter.VIDEO -> media.isVideo
                HomeDownloadFilter.FAVORITES -> media.isFavorite
            }
        }
        when (selectedSort) {
            HomeDownloadSort.NEWEST -> filtered
            HomeDownloadSort.TITLE_AZ -> filtered.sortedBy { it.first.title.lowercase() }
            HomeDownloadSort.ARTIST -> filtered.sortedBy { it.first.artist.lowercase() }
        }
    }

    val downloadedMediaOnly = remember(filteredDownloadedPairs) {
        filteredDownloadedPairs.map { it.first }
    }

    // Discovery data sets (Strictly Trending Today & Trending 2026; Song Recommend For You removed)
    val continueListeningItems = remember(historyMedia, allMedia) {
        val partial = historyMedia.ifEmpty {
            allMedia.filter { it.lastPositionSec > 0 }.ifEmpty { allMedia.take(3) }
        }
        partial.take(4)
    }

    val newReleaseAlbums = remember(allMedia) {
        allMedia.filter { it.releaseYear >= 2026 }
            .ifEmpty { allMedia }
            .groupBy { it.album }
            .entries
            .map { (albumTitle, tracks) -> albumTitle to tracks.first() }
            .take(8)
    }

    val trending2026Songs = remember(trendingNewItems, allMedia) {
        val raw = trendingNewItems.ifEmpty { allMedia.filter { it.releaseYear >= 2026 } }
        KhmerArtistDirectory.sortTrendingNewFirst(raw, TrendingFeedFilter.TRENDING_2026).take(8)
    }

    val madeForYouMixes = remember(palette) {
        listOf(
            MadeForYouMix("lumo_mix", "LUMO Mix", "Signature blend of your top tracks", "Personal Mix", palette.homeAccent),
            MadeForYouMix("daily_mix", "Daily Mix", "Fresh daily rotation for today", "Updated Today", palette.searchAccent),
            MadeForYouMix("chill_mix", "Chill Mix", "Calm acoustic & late evening vibes", "Relax", palette.libraryAccent),
            MadeForYouMix("night_mix", "Night Mix", "Deep nocturnal synth & R&B", "After Hours", Color(0xFF6366F1)),
            MadeForYouMix("workout_mix", "Workout Mix", "High-energy beats & tempo", "Energy", palette.downloadsAccent),
            MadeForYouMix("discover_mix", "Discover Mix", "New artists & global gems", "Explore", Color(0xFFEC4899))
        )
    }

    val artistsYouMayLike = remember(allMedia) {
        val fromMedia = allMedia.groupBy { it.artist }.map { (artist, tracks) ->
            artist to tracks.first()
        }
        fromMedia.take(10)
    }

    val solidPillBg = if (palette.option.isDark) Color.White else Color(0xFF0F172A)
    val solidPillText = if (palette.option.isDark) Color(0xFF0B0F17) else Color.White

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(bottom = 148.dp)
    ) {
        // ==================================================
        // TOP BAR & GREETING & SEARCH BAR (Section 5 & 46)
        // ==================================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Section 1: Minimal LUMO wordmark with minimal music symbol (No photo avatar)
                        LumoWordmarkLogo(
                            accentColor = reactive.reactiveAccentColor,
                            textColor = palette.primaryText,
                            symbolSize = 18.dp,
                            fontSizeSp = 17
                        )

                        Surface(
                            color = reactive.dominantColor.copy(alpha = 0.28f),
                            shape = CircleShape,
                            modifier = Modifier
                                .border(1.dp, reactive.reactiveAccentColor.copy(alpha = 0.45f), CircleShape)
                                .clickable(onClick = onCycleReactiveIntensity)
                                .testTag("home_reactive_intensity_badge")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .scale(if (reactive.isPlaying) 1f + reactive.beatPulse * 0.35f else 1f)
                                        .clip(CircleShape)
                                        .background(reactive.reactiveAccentColor)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = reactive.intensityMode.label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = palette.primaryText
                                )
                            }
                        }

                        Surface(
                            color = palette.elevatedSurface,
                            shape = CircleShape,
                            modifier = Modifier
                                .border(1.dp, palette.homeAccent.copy(alpha = 0.55f), CircleShape)
                                .clickable(onClick = onCycleLanguage)
                                .testTag("home_language_toggle_badge")
                        ) {
                            Text(
                                text = language.badgeLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = palette.primaryText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { showNotificationsSheet = true },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(palette.elevatedSurface)
                                .border(1.dp, palette.divider, CircleShape)
                                .testTag("home_notifications_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Notifications",
                                tint = palette.primaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(palette.elevatedSurface)
                                .border(1.dp, palette.divider, CircleShape)
                                .testTag("home_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings",
                                tint = palette.primaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Real-time local Cambodia time greeting
                val cambodiaHour = remember {
                    java.util.Calendar.getInstance(
                        java.util.TimeZone.getTimeZone("Asia/Phnom_Penh")
                    ).get(java.util.Calendar.HOUR_OF_DAY)
                }
                val greetingText = when (cambodiaHour) {
                    in 4..11 -> language.tr(
                        "អរុណសួស្តី 👋",
                        "Good morning 👋",
                        "អរុណសួស្តី · Good morning 👋"
                    )
                    in 12..16 -> language.tr(
                        "ទិវាសួស្តី 👋",
                        "Good afternoon 👋",
                        "ទិវាសួស្តី · Good afternoon 👋"
                    )
                    else -> language.tr(
                        "សាយណ្ហសួស្តី 👋",
                        "Good evening 👋",
                        "សាយណ្ហសួស្តី · Good evening 👋"
                    )
                }

                Text(
                    text = greetingText,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = palette.primaryText
                )
                Text(
                    text = "Discover. Play. Save.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.secondaryText
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Section 5 & 46 Home Search Bar: “Search songs, artists, albums…”
                Surface(
                    color = palette.surface,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, palette.divider, RoundedCornerShape(16.dp))
                        .clickable(onClick = onOpenSearchAndDownload)
                        .testTag("home_search_bar")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = palette.secondaryText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = language.tr(
                                "ស្វែងរកបទចម្រៀង សិល្បករ អាល់ប៊ុម…",
                                "Search songs, artists, albums…",
                                "Search songs, artists, albums…"
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.secondaryText,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            color = palette.elevatedSurface,
                            shape = CircleShape,
                            modifier = Modifier
                                .clickable(onClick = onOpenYouTubeLinkDownloader)
                                .testTag("home_youtube_import_pill")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = LumoIcons.YouTubePlay,
                                    contentDescription = "Import link",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Import",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = palette.primaryText
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Navigation & Mode Strip (Discover vs Downloaded Only + Trending + Stats + Favorites + History)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Surface(
                            color = if (!showDownloadedOnlyMode) solidPillBg else palette.surface,
                            shape = CircleShape,
                            modifier = Modifier
                                .border(1.dp, if (!showDownloadedOnlyMode) solidPillBg else palette.divider, CircleShape)
                                .clickable { showDownloadedOnlyMode = false }
                                .testTag("home_mode_discover")
                        ) {
                            Text(
                                text = language.tr("ពិភពតន្ត្រី · Discover", "Discover"),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (!showDownloadedOnlyMode) solidPillText else palette.primaryText,
                                modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp)
                            )
                        }
                    }
                    item {
                        Surface(
                            color = if (showDownloadedOnlyMode) solidPillBg else palette.surface,
                            shape = CircleShape,
                            modifier = Modifier
                                .border(1.dp, if (showDownloadedOnlyMode) solidPillBg else palette.divider, CircleShape)
                                .clickable { showDownloadedOnlyMode = true }
                                .testTag("home_mode_downloaded_only")
                        ) {
                            Text(
                                text = language.tr(
                                    "បទបានទាញយក (${downloadedMediaWithTasks.size})",
                                    "Downloaded (${downloadedMediaWithTasks.size})"
                                ),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (showDownloadedOnlyMode) solidPillText else palette.primaryText,
                                modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp)
                            )
                        }
                    }
                    item {
                        CompactQuickPill(
                            icon = LumoIcons.Equalizer,
                            label = language.tr("បទល្បី · Trending", "Trending"),
                            accentColor = reactive.reactiveAccentColor,
                            onClick = onOpenTrendingNewQuick,
                            testTag = "trending_see_all_button"
                        )
                    }
                    item {
                        CompactQuickPill(
                            icon = LumoIcons.Equalizer,
                            label = language.tr("ស្ថិតិស្តាប់ · Stats", "Your Listening"),
                            accentColor = palette.homeAccent,
                            onClick = { showListeningStatsSheet = true },
                            testTag = "quick_action_stats"
                        )
                    }
                    item {
                        CompactQuickPill(
                            icon = Icons.Filled.Favorite,
                            label = language.tr("ចូលចិត្ត (${favoriteMedia.size})", "Liked (${favoriteMedia.size})"),
                            accentColor = Color(0xFFEF4444),
                            onClick = onOpenFavoritesQuick,
                            testTag = "quick_action_favorites"
                        )
                    }
                    item {
                        CompactQuickPill(
                            icon = LumoIcons.History,
                            label = language.tr("ប្រវត្តិ (${historyMedia.size})", "History (${historyMedia.size})"),
                            accentColor = palette.searchAccent,
                            onClick = onOpenHistoryQuick,
                            testTag = "quick_action_history"
                        )
                    }
                    item {
                        CompactQuickPill(
                            icon = LumoIcons.Download,
                            label = language.tr("គ្រប់គ្រងទាញយក", "Download Manager"),
                            accentColor = palette.downloadsAccent,
                            onClick = onOpenDownloadsQuick,
                            testTag = "quick_action_downloads"
                        )
                    }
                }
            }
        }

        if (!showDownloadedOnlyMode) {
            // ==================================================
            // SECTION A: TRENDING TODAY & TRENDING 2026 ONLY (Not All Years)
            // ==================================================
            item {
                val isTodaySelected = selectedTrendingFilter == TrendingFeedFilter.TRENDING_TODAY
                HomeSectionHeader(
                    title = if (isTodaySelected) {
                        language.tr("កំពុងពេញនិយមថ្ងៃនេះ · TRENDING TODAY", "TRENDING TODAY")
                    } else {
                        language.tr("កំពុងពេញនិយម 2026 · TRENDING 2026", "TRENDING 2026")
                    },
                    subtitle = language.tr(
                        "មានតែបទល្បីថ្ងៃនេះ និងឆ្នាំ 2026 ប៉ុណ្ណោះ · មិនមែនគ្រប់ឆ្នាំទេ",
                        "Only Trending Today & Trending 2026 · Not all years"
                    ),
                    actionLabel = language.tr("មើលទាំងអស់", "See All"),
                    onActionClick = onOpenTrendingNewQuick
                )

                // 2-Pill Filter: Trending Today vs Trending 2026
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 12.dp)
                ) {
                    listOf(
                        TrendingFeedFilter.TRENDING_TODAY to language.tr("🔥 ថ្ងៃនេះ · Trending Today", "🔥 Trending Today"),
                        TrendingFeedFilter.TRENDING_2026 to language.tr("🌟 ឆ្នាំ 2026 · Trending 2026", "🌟 Trending 2026")
                    ).forEach { (filterOption, pillLabel) ->
                        val active = selectedTrendingFilter == filterOption
                        Surface(
                            color = if (active) solidPillBg else palette.surface,
                            shape = CircleShape,
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = if (active) solidPillBg else palette.divider,
                                    shape = CircleShape
                                )
                                .clickable { onSelectTrendingFilter(filterOption) }
                                .testTag("home_trending_filter_${filterOption.id.lowercase()}")
                        ) {
                            Text(
                                text = pillLabel,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (active) solidPillText else palette.primaryText,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.testTag("home_trending_now_row")
                ) {
                    itemsIndexed(trendingNewItems.take(8), key = { _, m -> "home_trend_${m.id}" }) { index, media ->
                        TrendingNowCompactCard(
                            rank = index + 1,
                            media = media,
                            accentColor = reactive.reactiveAccentColor,
                            onClick = { onPlayMedia(media, false) },
                            onMoreClick = { onOpenTrackOptions(media) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            // ==================================================
            // SECTION C: CONTINUE LISTENING (Partially played with progress bar)
            // ==================================================
            if (continueListeningItems.isNotEmpty()) {
                item {
                    HomeSectionHeader(
                        title = language.tr("បន្តស្តាប់ · CONTINUE LISTENING", "CONTINUE LISTENING"),
                        actionLabel = language.tr("ប្រវត្តិ", "History"),
                        onActionClick = onOpenHistoryQuick
                    )
                }

                items(continueListeningItems, key = { "home_cont_${it.id}" }) { media ->
                    ContinueListeningRowCard(
                        media = media,
                        accentColor = reactive.reactiveAccentColor,
                        onClick = { onPlayMedia(media, false) },
                        onMoreClick = { onOpenTrackOptions(media) }
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // ==================================================
            // SECTION D: NEW RELEASES (Horizontal Album Cards)
            // ==================================================
            item {
                HomeSectionHeader(
                    title = language.tr("ចេញថ្មីៗ · NEW RELEASES", "NEW RELEASES")
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.testTag("home_new_releases_row")
                ) {
                    items(newReleaseAlbums, key = { "home_album_${it.first}" }) { (albumTitle, repMedia) ->
                        NewReleaseAlbumCard(
                            albumTitle = albumTitle,
                            repMedia = repMedia,
                            onClick = { selectedAlbumPage = albumTitle }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            // ==================================================
            // SECTION E: TRENDING 2026 (Numbered Vertical List - 2026 Only, Not All Years)
            // ==================================================
            item {
                HomeSectionHeader(
                    title = language.tr("បទល្បីប្រចាំឆ្នាំ 2026 · TRENDING 2026", "TRENDING 2026"),
                    subtitle = language.tr(
                        "បទចម្រៀងល្បីៗក្នុងឆ្នាំ 2026 ប៉ុណ្ណោះ (មិនមែនគ្រប់ឆ្នាំទេ)",
                        "Ranked by real 2026 worldwide plays · Not all years"
                    ),
                    actionLabel = language.tr("មើលតារាង 2026", "2026 Chart"),
                    onActionClick = {
                        onSelectTrendingFilter(TrendingFeedFilter.TRENDING_2026)
                        onOpenTrendingNewQuick()
                    }
                )
            }

            itemsIndexed(trending2026Songs.take(5), key = { _, m -> "home_top_week_${m.id}" }) { index, media ->
                CompactTrackRow(
                    media = media,
                    indexNumber = index + 1,
                    isCurrentlyPlaying = currentPlayingMediaId == media.id,
                    subtitleOverride = "${media.artist}  ·  2026  ·  🌍 ${media.formattedPopularity}  ·  ${media.formattedDuration}",
                    onPlay = { onPlayMedia(media, false) },
                    onToggleFavorite = { onToggleFavorite(media) },
                    onDownload = { onOpenDownloadSheet(media) },
                    onOpenOptions = { onOpenTrackOptions(media) }
                )
            }

            item { Spacer(modifier = Modifier.height(18.dp)) }

            // ==================================================
            // SECTION F: MADE FOR YOU & CUSTOM PHOTO PLAYLISTS
            // ==================================================
            item {
                HomeSectionHeader(
                    title = language.tr("បង្កើតសម្រាប់អ្នក · MADE FOR YOU", "MADE FOR YOU"),
                    actionLabel = language.tr("+ បង្កើតបញ្ជីចាក់", "+ Custom Playlist"),
                    onActionClick = { showCreatePlaylistDialog = true }
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.testTag("home_made_for_you_row")
                ) {
                    items(madeForYouMixes, key = { it.id }) { mix ->
                        val coverMedia = allMedia.getOrNull(madeForYouMixes.indexOf(mix) % allMedia.size.coerceAtLeast(1))
                        MadeForYouMixCard(
                            mix = mix,
                            coverMedia = coverMedia,
                            onClick = {
                                val shuffled = allMedia.shuffled()
                                if (shuffled.isNotEmpty()) {
                                    onPlayDownloadedQueue(shuffled.first(), shuffled, false)
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // Custom Photo Playlists Carousel (Always accessible on Home)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Column {
                        Text(
                            text = language.tr(
                                "បញ្ជីចាក់រូបភាពផ្ទាល់ខ្លួន · YOUR PLAYLISTS",
                                "YOUR CUSTOM PHOTO PLAYLISTS"
                            ),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = palette.primaryText
                        )
                        Text(
                            text = language.tr(
                                "ចុចលើរូបភាពដើម្បីប្តូររូបពីទូរស័ព្ទរបស់អ្នក",
                                "Tap Photo on any playlist to set your own cover image"
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.secondaryText
                        )
                    }

                    Text(
                        text = language.tr("+ បង្កើតថ្មី", "+ New"),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = palette.libraryAccent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showCreatePlaylistDialog = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("home_create_playlist_button")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.testTag("home_playlists_carousel")
                ) {
                    items(playlists, key = { "home_pl_${it.id}" }) { playlist ->
                        HomeCustomPlaylistCard(
                            playlist = playlist,
                            onOpenPlaylist = { onOpenPlaylistDetail(playlist.id) },
                            onChangePhoto = { playlistForPhotoPicker = playlist }
                        )
                    }

                    item {
                        HomeCreateNewPlaylistCard(
                            accentColor = palette.libraryAccent,
                            onClick = { showCreatePlaylistDialog = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }
        }

        if (!showDownloadedOnlyMode) {
            // ==================================================
            // SECTION G: ARTISTS YOU MAY LIKE (Circular Artist Images)
            // ==================================================
            item {
                HomeSectionHeader(
                    title = language.tr("សិល្បករដែលអ្នកអាចចូលចិត្ត · ARTISTS YOU MAY LIKE", "ARTISTS YOU MAY LIKE")
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.testTag("home_artists_row")
                ) {
                    items(artistsYouMayLike, key = { "home_art_${it.first}" }) { (artistName, repTrack) ->
                        CircularArtistCard(
                            artistName = artistName,
                            repTrack = repTrack,
                            onClick = { selectedArtistPage = artistName }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }
        }

        // ==================================================
        // DOWNLOADED MUSIC SECTION (Offline Saved Music)
        // ==================================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = palette.downloadsAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = language.tr(
                                "បទចម្រៀងបានទាញយក (${downloadedMediaOnly.size})",
                                "DOWNLOADED MUSIC (${downloadedMediaOnly.size})"
                            ),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = palette.primaryText
                        )
                    }

                    Surface(
                        color = palette.surface,
                        shape = CircleShape,
                        modifier = Modifier
                            .border(1.dp, palette.divider, CircleShape)
                            .clickable { selectedSort = selectedSort.next() }
                            .testTag("home_downloads_sort_button")
                    ) {
                        Text(
                            text = "⇅ " + language.tr(selectedSort.kmLabel, selectedSort.enLabel),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.secondaryText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HomeDownloadFilter.entries.forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            color = if (isSelected) solidPillBg else palette.surface,
                            shape = CircleShape,
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) solidPillBg else palette.divider,
                                    shape = CircleShape
                                )
                                .clickable { selectedFilter = filter }
                                .testTag("home_download_filter_${filter.name.lowercase()}")
                        ) {
                            Text(
                                text = language.tr(filter.kmLabel, filter.enLabel),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) solidPillText else palette.secondaryText,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                if (downloadedMediaOnly.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                onPlayDownloadedQueue(
                                    downloadedMediaOnly.first(),
                                    downloadedMediaOnly,
                                    false
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = solidPillBg,
                                contentColor = solidPillText
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(vertical = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("home_play_all_downloads_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = language.tr("ចាក់ទាំងអស់", "Play All"),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val shuffled = downloadedMediaOnly.shuffled()
                                onPlayDownloadedQueue(
                                    shuffled.first(),
                                    shuffled,
                                    false
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(vertical = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("home_shuffle_downloads_button")
                        ) {
                            Icon(
                                imageVector = LumoIcons.Shuffle,
                                contentDescription = null,
                                tint = palette.primaryText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = language.tr("ចាក់ចម្រុះ", "Shuffle"),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = palette.primaryText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (filteredDownloadedPairs.isEmpty()) {
            item {
                CleanOfflineEmptyStateCard(
                    hasAnyDownloads = downloadedMediaWithTasks.isNotEmpty(),
                    onResetFilter = { selectedFilter = HomeDownloadFilter.ALL },
                    onGoToSearchTab = onOpenSearchAndDownload
                )
            }
        } else {
            itemsIndexed(
                items = filteredDownloadedPairs,
                key = { _, pair -> "home_dl_${pair.first.id}" }
            ) { index, (media, downloadTask) ->
                val formatBadge = downloadTask?.let { "${it.format} · ${it.formattedSizeMb}" }
                    ?: "OFFLINE · ${media.formattedDuration}"
                CompactTrackRow(
                    media = media,
                    indexNumber = index + 1,
                    isCurrentlyPlaying = currentPlayingMediaId == media.id,
                    subtitleOverride = "${media.artist}  ·  ✓ $formatBadge",
                    onPlay = {
                        onPlayDownloadedQueue(media, downloadedMediaOnly, false)
                    },
                    onToggleFavorite = { onToggleFavorite(media) },
                    onDownload = null,
                    onOpenOptions = { onOpenTrackOptions(media) }
                )
            }
        }
    }

    if (showCreatePlaylistDialog) {
        CreateOrEditCustomPlaylistDialog(
            initialName = "",
            initialDescription = "",
            initialCoverResName = "img_album_neon_echoes",
            initialCoverUrl = "",
            availableMediaForCovers = downloadedMediaOnly.ifEmpty { allMedia },
            titleText = language.tr("បង្កើតបញ្ជីចាក់ថ្មី", "Create Custom Playlist"),
            accentColor = palette.libraryAccent,
            onPickGalleryPhotoUri = onPickGalleryPhotoUri,
            onDismiss = { showCreatePlaylistDialog = false },
            onConfirm = { name, desc, coverResName, coverUrl ->
                onCreateCustomPlaylist(name, desc, coverResName, coverUrl)
                showCreatePlaylistDialog = false
            }
        )
    }

    playlistForPhotoPicker?.let { targetPlaylist ->
        CustomPlaylistPhotoPickerDialog(
            playlist = targetPlaylist,
            availableMediaForCovers = downloadedMediaOnly.ifEmpty { allMedia },
            accentColor = palette.libraryAccent,
            onPickGalleryPhotoUri = { uri ->
                onUpdatePlaylistPhotoUri(targetPlaylist, uri)
            },
            onSelectCustomCover = { coverRes, coverUrl ->
                onUpdatePlaylistCustomCover(targetPlaylist, coverRes, coverUrl)
            },
            onDismiss = { playlistForPhotoPicker = null }
        )
    }

    if (showNotificationsSheet) {
        NotificationsBottomSheet(
            completedDownloadsCount = completedDownloads.size,
            recommendedTrack = trendingNewItems.firstOrNull(),
            onPlayRecommended = { onPlayMedia(it, true) },
            onDismiss = { showNotificationsSheet = false }
        )
    }

    if (showListeningStatsSheet) {
        ListeningStatsBottomSheet(
            allMedia = allMedia,
            historyMedia = historyMedia,
            favoriteMedia = favoriteMedia,
            completedDownloads = completedDownloads,
            onDismiss = { showListeningStatsSheet = false }
        )
    }
}

@Composable
private fun HomeSectionHeader(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val palette = LocalLumoPalette.current
    val reactive = LocalColorReactiveState.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp
                ),
                color = palette.primaryText
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.secondaryText
                )
            }
        }
        if (actionLabel != null && onActionClick != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = reactive.reactiveAccentColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

/**
 * Section 5A: Clean, compact Trending Now card with Ranking number, Artwork, Song title, Artist, More button, and subtle "Trending" indicator.
 */
@Composable
private fun TrendingNowCompactCard(
    rank: Int,
    media: MediaItemEntity,
    accentColor: Color,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    val palette = LocalLumoPalette.current
    Column(
        modifier = Modifier
            .width(152.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(palette.surface)
            .border(1.dp, palette.divider, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(116.dp)
                .clip(RoundedCornerShape(14.dp))
        ) {
            LumoArtworkImage(
                artworkUrl = media.artworkUrl,
                fallbackResId = media.artworkResId,
                contentDescription = media.title,
                modifier = Modifier.fillMaxSize()
            )
            // Ranking badge (01, 02...)
            Surface(
                color = Color.Black.copy(alpha = 0.72f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
            ) {
                Text(
                    text = String.format(Locale.US, "%02d", rank),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
            // Real worldwide listener count indicator
            Surface(
                color = accentColor.copy(alpha = 0.88f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
            ) {
                Text(
                    text = if (media.popularityScore > 0L) {
                        "🌍 ${media.formattedPopularity.removeSuffix(" global plays")}"
                    } else {
                        "🌍 Global"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = media.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = palette.primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = media.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = LumoIcons.MoreHoriz,
                    contentDescription = "More",
                    tint = palette.secondaryText,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}


/**
 * Section 5C: Continue Listening row (Artwork, Song title, Artist, Progress bar).
 */
@Composable
private fun ContinueListeningRowCard(
    media: MediaItemEntity,
    accentColor: Color,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    val palette = LocalLumoPalette.current
    val progress = media.progressFraction.coerceIn(0.18f, 0.92f)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(palette.surface)
            .border(1.dp, palette.divider, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        LumoArtworkImage(
            artworkUrl = media.artworkUrl,
            fallbackResId = media.artworkResId,
            contentDescription = media.title,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = media.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = palette.primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = media.artist,
                style = MaterialTheme.typography.bodySmall,
                color = palette.secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                color = accentColor,
                trackColor = palette.divider,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onMoreClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = LumoIcons.MoreHoriz,
                contentDescription = "More",
                tint = palette.secondaryText,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Section 5D: New Releases horizontal album card (Artwork, Album title, Artist).
 */
@Composable
private fun NewReleaseAlbumCard(
    albumTitle: String,
    repMedia: MediaItemEntity,
    onClick: () -> Unit
) {
    val palette = LocalLumoPalette.current
    Column(
        modifier = Modifier
            .width(136.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(palette.surface)
            .border(1.dp, palette.divider, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        LumoArtworkImage(
            artworkUrl = repMedia.artworkUrl,
            fallbackResId = repMedia.artworkResId,
            contentDescription = albumTitle,
            modifier = Modifier
                .fillMaxWidth()
                .height(108.dp)
                .clip(RoundedCornerShape(13.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = albumTitle,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = palette.primaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = repMedia.artist,
            style = MaterialTheme.typography.bodySmall,
            color = palette.secondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Section 5F: Made For You personalized playlist card (LUMO Mix, Daily Mix, Chill Mix, Night Mix, Workout Mix, Discover Mix).
 */
@Composable
private fun MadeForYouMixCard(
    mix: MadeForYouMix,
    coverMedia: MediaItemEntity?,
    onClick: () -> Unit
) {
    val palette = LocalLumoPalette.current
    Column(
        modifier = Modifier
            .width(142.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(palette.surface)
            .border(1.dp, mix.accent.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
                .clip(RoundedCornerShape(13.dp))
        ) {
            if (coverMedia != null) {
                LumoArtworkImage(
                    artworkUrl = coverMedia.artworkUrl,
                    fallbackResId = coverMedia.artworkResId,
                    contentDescription = mix.title,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Surface(
                color = mix.accent.copy(alpha = 0.90f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
            ) {
                Text(
                    text = mix.badge,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = mix.title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = palette.primaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = mix.subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = palette.secondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Section 5G: Circular Artist Card (Tapping opens dedicated Artist Page).
 */
@Composable
private fun CircularArtistCard(
    artistName: String,
    repTrack: MediaItemEntity,
    onClick: () -> Unit
) {
    val palette = LocalLumoPalette.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(88.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        LumoArtworkImage(
            artworkUrl = repTrack.artworkUrl,
            fallbackResId = repTrack.artworkResId,
            contentDescription = artistName,
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .border(1.dp, palette.divider, CircleShape)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = artistName,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = palette.primaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CompactQuickPill(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val palette = LocalLumoPalette.current
    Surface(
        color = palette.surface,
        shape = CircleShape,
        modifier = Modifier
            .border(1.dp, palette.divider, CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = palette.primaryText,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun HomeCustomPlaylistCard(
    playlist: PlaylistEntity,
    onOpenPlaylist: () -> Unit,
    onChangePhoto: () -> Unit
) {
    val palette = LocalLumoPalette.current
    val language = LocalLumoLanguage.current
    Column(
        modifier = Modifier
            .width(136.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(palette.surface)
            .border(1.dp, palette.divider, RoundedCornerShape(18.dp))
            .clickable(onClick = onOpenPlaylist)
            .padding(9.dp)
            .testTag("home_playlist_card_${playlist.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(106.dp)
                .clip(RoundedCornerShape(13.dp))
        ) {
            LumoArtworkImage(
                artworkUrl = playlist.coverUrl,
                fallbackResId = playlist.coverResId,
                contentDescription = playlist.name,
                modifier = Modifier.fillMaxSize()
            )
            Surface(
                color = Color.Black.copy(alpha = 0.72f),
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                    .clickable(onClick = onChangePhoto)
                    .testTag("home_playlist_photo_btn_${playlist.id}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = LumoIcons.Image,
                        contentDescription = "Change playlist photo",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = language.tr("រូបភាព", "Photo"),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = playlist.name,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = palette.primaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = language.tr("${playlist.trackIds.size} បទ", "${playlist.trackIds.size} tracks"),
            style = MaterialTheme.typography.labelSmall,
            color = palette.secondaryText,
            maxLines = 1
        )
    }
}

@Composable
private fun HomeCreateNewPlaylistCard(
    accentColor: Color,
    onClick: () -> Unit
) {
    val palette = LocalLumoPalette.current
    val language = LocalLumoLanguage.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(126.dp)
            .height(158.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(palette.surface)
            .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
            .testTag("home_new_playlist_card")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "New Playlist",
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = language.tr("បង្កើតបញ្ជីចាក់", "New Playlist"),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = palette.primaryText,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Text(
            text = language.tr("ដាក់រូបផ្ទាល់ខ្លួន", "Custom photo"),
            style = MaterialTheme.typography.labelSmall,
            color = palette.secondaryText,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun CleanOfflineEmptyStateCard(
    hasAnyDownloads: Boolean,
    onResetFilter: () -> Unit,
    onGoToSearchTab: () -> Unit
) {
    val palette = LocalLumoPalette.current
    val language = LocalLumoLanguage.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(palette.surface)
            .border(1.dp, palette.divider, RoundedCornerShape(22.dp))
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .testTag("home_empty_downloads_card")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(palette.downloadsAccent.copy(alpha = 0.14f))
        ) {
            Icon(
                imageVector = LumoIcons.Download,
                contentDescription = null,
                tint = palette.downloadsAccent,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (hasAnyDownloads) {
                language.tr("មិនមានបទចម្រៀងក្នុងប្រភេទនេះទេ", "No downloaded tracks match this filter")
            } else {
                language.tr("មិនទាន់មានបទចម្រៀងបានទាញយកទេ", "No Downloads Yet")
            },
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = palette.primaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (hasAnyDownloads) {
                language.tr("សាកល្បងប្តូរទៅ «ទាំងអស់» ដើម្បីមើលបទដែលបានទាញយកទាំងអស់។", "Switch back to All to see your offline library.")
            } else {
                language.tr("បទចម្រៀងដែលអ្នកបានរក្សាទុកនឹងបង្ហាញនៅទីនេះ។", "Your saved music will appear here.")
            },
            style = MaterialTheme.typography.bodySmall,
            color = palette.secondaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = if (hasAnyDownloads) onResetFilter else onGoToSearchTab,
            colors = ButtonDefaults.buttonColors(containerColor = palette.downloadsAccent),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.testTag("home_empty_action_button")
        ) {
            Text(
                text = if (hasAnyDownloads) {
                    language.tr("បង្ហាញបទទាញយកទាំងអស់", "Show All Downloads")
                } else {
                    language.tr("ស្វែងរកបទចម្រៀង · Explore Music", "Explore Music")
                },
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SectionTitleRow(
    title: String,
    accentColor: Color
) {
    val palette = LocalLumoPalette.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = palette.primaryText
        )
    }
}

@Composable
fun CompactTrackRow(
    media: MediaItemEntity,
    indexNumber: Int? = null,
    isCurrentlyPlaying: Boolean = false,
    subtitleOverride: String? = null,
    minimalMode: Boolean = false,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDownload: (() -> Unit)? = null,
    onOpenOptions: () -> Unit
) {
    val palette = LocalLumoPalette.current
    val rowBg = if (isCurrentlyPlaying) {
        LocalColorReactiveState.current.dominantColor.copy(alpha = 0.28f)
    } else Color.Transparent
    val rowBorder = if (isCurrentlyPlaying) {
        LocalColorReactiveState.current.accentColor.copy(alpha = 0.38f)
    } else Color.Transparent

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(rowBg)
            .border(
                width = 1.dp,
                color = rowBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onPlay)
            .padding(horizontal = 10.dp, vertical = if (minimalMode) 8.dp else 10.dp)
            .testTag("track_row_${media.id}")
    ) {
        if (indexNumber != null && !minimalMode) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.width(28.dp)
            ) {
                if (isCurrentlyPlaying) {
                    val reactive = LocalColorReactiveState.current
                    Icon(
                        imageVector = LumoIcons.Equalizer,
                        contentDescription = "Playing",
                        tint = reactive.reactiveAccentColor,
                        modifier = Modifier
                            .size(18.dp)
                            .scale(1f + reactive.beatPulse * 0.14f)
                    )
                } else {
                    Text(
                        text = String.format(Locale.US, "%02d", indexNumber),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.secondaryText
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Box(
            modifier = Modifier
                .size(if (minimalMode) 42.dp else 46.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            LumoArtworkImage(
                artworkUrl = media.artworkUrl,
                fallbackResId = media.artworkResId,
                contentDescription = media.title,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = media.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = palette.primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitleOverride ?: "${media.artist}  ·  ${media.formattedDuration}",
                style = MaterialTheme.typography.bodySmall,
                color = palette.secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (onDownload != null && media.isAuthorizedDownload) {
            IconButton(
                onClick = onDownload,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = LumoIcons.Download,
                    contentDescription = "Download",
                    tint = palette.secondaryText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        IconButton(
            onClick = onOpenOptions,
            modifier = Modifier
                .size(38.dp)
                .testTag("track_more_${media.id}")
        ) {
            Icon(
                imageVector = LumoIcons.MoreHoriz,
                contentDescription = "More options",
                tint = palette.secondaryText,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun FavoritesSubScreen(
    favoriteMedia: List<MediaItemEntity>,
    onBack: () -> Unit,
    onPlayMedia: (MediaItemEntity, Boolean) -> Unit,
    onToggleFavorite: (MediaItemEntity) -> Unit,
    onOpenTrackOptions: (MediaItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalLumoPalette.current
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("favorites_sub_screen"),
        contentPadding = PaddingValues(bottom = 140.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = palette.primaryText
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Liked Songs",
                        style = MaterialTheme.typography.headlineLarge,
                        color = palette.primaryText
                    )
                    Text(
                        text = "${favoriteMedia.size} saved songs, videos & artists",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.secondaryText
                    )
                }
            }
        }
        itemsIndexed(favoriteMedia, key = { _, m -> "fav_${m.id}" }) { index, media ->
            CompactTrackRow(
                media = media,
                indexNumber = index + 1,
                onPlay = { onPlayMedia(media, true) },
                onToggleFavorite = { onToggleFavorite(media) },
                onOpenOptions = { onOpenTrackOptions(media) }
            )
        }
    }
}

@Composable
private fun HistorySubScreen(
    historyMedia: List<MediaItemEntity>,
    onBack: () -> Unit,
    onPlayMedia: (MediaItemEntity, Boolean) -> Unit,
    onOpenTrackOptions: (MediaItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalLumoPalette.current
    val now = System.currentTimeMillis()
    val oneDayMs = 24 * 3_600_000L

    val todayList = historyMedia.filter { now - it.lastPlayedTimestamp < oneDayMs }
    val yesterdayList = historyMedia.filter { (now - it.lastPlayedTimestamp) in oneDayMs..(2 * oneDayMs) }
    val earlierList = historyMedia.filter { now - it.lastPlayedTimestamp > 2 * oneDayMs }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("history_sub_screen"),
        contentPadding = PaddingValues(bottom = 140.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = palette.primaryText
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Recently Played",
                        style = MaterialTheme.typography.headlineLarge,
                        color = palette.primaryText
                    )
                    Text(
                        text = "Today · Yesterday · This Week · Earlier",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.secondaryText
                    )
                }
            }
        }

        if (todayList.isNotEmpty()) {
            item {
                SectionTitleRow(title = "Today", accentColor = palette.searchAccent)
            }
            items(todayList, key = { "hist_today_${it.id}" }) { media ->
                HistoryTimelineItem(
                    media = media,
                    onClick = { onPlayMedia(media, true) },
                    onOpenOptions = { onOpenTrackOptions(media) }
                )
            }
        }

        if (yesterdayList.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                SectionTitleRow(title = "Yesterday", accentColor = palette.homeAccent)
            }
            items(yesterdayList, key = { "hist_yest_${it.id}" }) { media ->
                HistoryTimelineItem(
                    media = media,
                    onClick = { onPlayMedia(media, true) },
                    onOpenOptions = { onOpenTrackOptions(media) }
                )
            }
        }

        if (earlierList.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                SectionTitleRow(title = "Earlier", accentColor = palette.libraryAccent)
            }
            items(earlierList, key = { "hist_early_${it.id}" }) { media ->
                HistoryTimelineItem(
                    media = media,
                    onClick = { onPlayMedia(media, true) },
                    onOpenOptions = { onOpenTrackOptions(media) }
                )
            }
        }
    }
}

@Composable
private fun HistoryTimelineItem(
    media: MediaItemEntity,
    onClick: () -> Unit,
    onOpenOptions: () -> Unit
) {
    val palette = LocalLumoPalette.current
    val pct = (media.progressFraction * 100).toInt().coerceIn(5, 100)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LumoArtworkImage(
                artworkUrl = media.artworkUrl,
                fallbackResId = media.artworkResId,
                contentDescription = media.title,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = media.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = palette.primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${media.artist} · ${media.formattedDuration}",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.secondaryText
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { media.progressFraction.coerceIn(0.05f, 1f) },
                        color = if (media.isVideo) palette.searchAccent else palette.homeAccent,
                        trackColor = palette.divider,
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(CircleShape)
                    )
                    Text(
                        text = "$pct%",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.secondaryText
                    )
                }
            }
            IconButton(onClick = onOpenOptions) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Options",
                    tint = palette.secondaryText
                )
            }
        }
    }
}

/**
 * Section 6: Dedicated Trending Page with Global, Cambodia, Pop, Hip-Hop, EDM, Rock, K-Pop, Khmer, R&B categories and ↑ Rising / NEW / HOT indicators.
 */
@Composable
private fun TrendingNewSubScreen(
    trendingItems: List<MediaItemEntity>,
    selectedFilter: TrendingFeedFilter,
    isRefreshing: Boolean,
    currentPlayingMediaId: String?,
    onBack: () -> Unit,
    onSelectFilter: (TrendingFeedFilter) -> Unit,
    onRefresh: () -> Unit,
    onPlayMedia: (MediaItemEntity, Boolean) -> Unit,
    onToggleFavorite: (MediaItemEntity) -> Unit,
    onDownload: (MediaItemEntity) -> Unit,
    onOpenTrackOptions: (MediaItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalLumoPalette.current
    val reactive = LocalColorReactiveState.current
    val solidPillBg = if (palette.option.isDark) Color.White else Color(0xFF0F172A)
    val solidPillText = if (palette.option.isDark) Color(0xFF0B0F17) else Color.White

    val genreCategories = listOf(
        "Trending Today" to TrendingFeedFilter.TRENDING_TODAY,
        "Trending 2026" to TrendingFeedFilter.TRENDING_2026
    )
    var selectedCategoryName by remember(selectedFilter) {
        mutableStateOf(
            genreCategories.firstOrNull { it.second == selectedFilter }?.first ?: "Trending Today"
        )
    }

    val displayedItems = remember(trendingItems) { trendingItems }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("trending_new_sub_screen"),
        contentPadding = PaddingValues(bottom = 140.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("trending_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = palette.primaryText
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = selectedCategoryName,
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.primaryText
                        )
                        Text(
                            text = "${displayedItems.size} live worldwide tracks · Only Today & 2026 (Not All Years)",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.secondaryText
                        )
                    }
                }

                Surface(
                    color = palette.elevatedSurface,
                    shape = CircleShape,
                    modifier = Modifier
                        .border(1.dp, reactive.reactiveAccentColor.copy(alpha = 0.5f), CircleShape)
                        .clickable(onClick = onRefresh)
                        .testTag("trending_subscreen_refresh")
                ) {
                    Text(
                        text = if (isRefreshing) "Syncing…" else "Refresh Live",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = palette.primaryText,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Strictly Only 2 Categories: Trending Today & Trending 2026 (Not All Years)
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                items(genreCategories, key = { "trend_cat_${it.first}" }) { (catName, feedFilter) ->
                    val isSelected = selectedCategoryName == catName
                    Surface(
                        color = if (isSelected) solidPillBg else palette.surface,
                        shape = CircleShape,
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = if (isSelected) solidPillBg else palette.divider,
                                shape = CircleShape
                            )
                            .clickable {
                                selectedCategoryName = catName
                                onSelectFilter(feedFilter)
                            }
                    ) {
                        Text(
                            text = catName,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) solidPillText else palette.primaryText,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        if (displayedItems.isNotEmpty()) {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .height(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(solidPillBg)
                        .clickable { onPlayMedia(displayedItems.first(), true) }
                        .testTag("play_all_trending_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = solidPillText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Play All $selectedCategoryName Trending (${displayedItems.size})",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = solidPillText
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        itemsIndexed(
            items = displayedItems,
            key = { _, m -> "trend_full_${m.id}" }
        ) { index, media ->
            val indicatorBadge = when {
                index < 3 -> "HOT 🌍"
                index % 2 == 0 -> "↑ Rising"
                else -> "GLOBAL"
            }
            CompactTrackRow(
                media = media,
                indexNumber = index + 1,
                isCurrentlyPlaying = currentPlayingMediaId == media.id,
                subtitleOverride = "$indicatorBadge  ·  ${media.artist}  ·  ${media.formattedPopularity}  ·  ${media.formattedDuration}",
                onPlay = { onPlayMedia(media, true) },
                onToggleFavorite = { onToggleFavorite(media) },
                onDownload = { onDownload(media) },
                onOpenOptions = { onOpenTrackOptions(media) }
            )
        }
    }
}
