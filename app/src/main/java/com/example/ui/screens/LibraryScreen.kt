package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.example.data.DownloadTaskEntity
import com.example.data.MediaItemEntity
import com.example.data.PlaylistEntity
import com.example.ui.LibrarySubTab
import com.example.ui.components.CreateOrEditCustomPlaylistDialog
import com.example.ui.components.CustomPlaylistPhotoPickerDialog
import com.example.ui.components.LumoArtworkImage
import com.example.ui.theme.LocalLumoLanguage
import com.example.ui.theme.LocalLumoPalette
import com.example.ui.theme.LumoIcons

@Composable
fun LibraryScreen(
    allMedia: List<MediaItemEntity>,
    currentPlayingMediaId: String? = null,
    playlists: List<PlaylistEntity>,
    completedDownloads: List<DownloadTaskEntity>,
    selectedSubTab: LibrarySubTab,
    libraryQuery: String,
    selectedPlaylistId: Long?,
    onSelectSubTab: (LibrarySubTab) -> Unit,
    onQueryChange: (String) -> Unit,
    onOpenPlaylistDetail: (Long?) -> Unit,
    onCreatePlaylist: (String, String, String) -> Unit,
    onCreateCustomPlaylist: (String, String, String, String) -> Unit = { name, desc, res, _ ->
        onCreatePlaylist(name, desc, res)
    },
    onPickGalleryPhotoUri: (Uri, (String) -> Unit) -> Unit = { _, _ -> },
    onUpdatePlaylistPhotoUri: (PlaylistEntity, Uri) -> Unit = { _, _ -> },
    onUpdatePlaylistCustomCover: (PlaylistEntity, String, String) -> Unit = { _, _, _ -> },
    onRenamePlaylist: (Long, String, String) -> Unit,
    onCyclePlaylistCover: (PlaylistEntity) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onMoveTrackInPlaylist: (Long, Int, Int) -> Unit,
    onRemoveTrackFromPlaylist: (Long, String) -> Unit,
    onAddTrackToPlaylist: (Long, MediaItemEntity) -> Unit,
    onPlayMedia: (MediaItemEntity, List<MediaItemEntity>?, Boolean) -> Unit,
    onToggleFavorite: (MediaItemEntity) -> Unit,
    onOpenDownloadSheet: ((MediaItemEntity) -> Unit)? = null,
    onOpenTrackOptions: (MediaItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalLumoPalette.current
    val language = LocalLumoLanguage.current
    val libraryAccent = palette.libraryAccent
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var playlistForPhotoPicker by remember { mutableStateOf<PlaylistEntity?>(null) }

    val activePlaylist = playlists.find { it.id == selectedPlaylistId }
    if (activePlaylist != null) {
        BackHandler { onOpenPlaylistDetail(null) }
        PlaylistDetailView(
            playlist = activePlaylist,
            allMedia = allMedia,
            onBack = { onOpenPlaylistDetail(null) },
            onPlayPlaylist = { shuffle ->
                val tracks = activePlaylist.trackIds.mapNotNull { id -> allMedia.find { it.id == id } }
                if (tracks.isNotEmpty()) {
                    val ordered = if (shuffle) tracks.shuffled() else tracks
                    onPlayMedia(ordered.first(), ordered, true)
                }
            },
            onRename = { newName, newDesc -> onRenamePlaylist(activePlaylist.id, newName, newDesc) },
            onOpenPhotoPicker = { playlistForPhotoPicker = activePlaylist },
            onPickGalleryPhotoUri = onPickGalleryPhotoUri,
            onUpdateCustomCover = { coverRes, coverUrl ->
                onUpdatePlaylistCustomCover(activePlaylist, coverRes, coverUrl)
            },
            onDelete = { onDeletePlaylist(activePlaylist.id) },
            onMoveTrack = { fromIdx, toIdx -> onMoveTrackInPlaylist(activePlaylist.id, fromIdx, toIdx) },
            onRemoveTrack = { mediaId -> onRemoveTrackFromPlaylist(activePlaylist.id, mediaId) },
            onAddTrack = { media -> onAddTrackToPlaylist(activePlaylist.id, media) },
            modifier = modifier
        )
        playlistForPhotoPicker?.let { targetPlaylist ->
            CustomPlaylistPhotoPickerDialog(
                playlist = targetPlaylist,
                availableMediaForCovers = allMedia,
                accentColor = libraryAccent,
                onPickGalleryPhotoUri = { uri ->
                    onUpdatePlaylistPhotoUri(targetPlaylist, uri)
                },
                onSelectCustomCover = { coverRes, coverUrl ->
                    onUpdatePlaylistCustomCover(targetPlaylist, coverRes, coverUrl)
                },
                onDismiss = { playlistForPhotoPicker = null }
            )
        }
        return
    }

    val q = libraryQuery.trim()
    val filteredMedia = remember(allMedia, q) {
        com.example.data.KhmerArtistDirectory.sortPopularSongsFirst(
            items = allMedia.filter {
                com.example.data.KhmerArtistDirectory.matchesMediaItem(it, q)
            },
            queryHint = q,
            prioritizeSongsBeforeVideos = true
        )
    }
    val groupedAlbums = remember(filteredMedia) {
        filteredMedia.groupBy { it.album }.entries.toList()
    }
    val groupedArtists = remember(filteredMedia) {
        filteredMedia.groupBy { it.artist }.map { (artistName, tracks) ->
            artistName to com.example.data.KhmerArtistDirectory.sortPopularSongsFirst(tracks)
        }
    }
    val matchingKhmerProfiles = remember(q, groupedArtists) {
        com.example.data.KhmerArtistDirectory.findMatchingArtists(q)
            .filter { profile ->
                groupedArtists.none { (name, _) ->
                    name.contains(profile.englishName, ignoreCase = true) ||
                        name.contains(profile.khmerName)
                }
            }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("library_screen_list"),
        contentPadding = PaddingValues(bottom = 140.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Text(
                    text = language.tr("បណ្ណាល័យរបស់អ្នក", "Your Library", "បណ្ណាល័យ · Your Library"),
                    style = MaterialTheme.typography.displayMedium,
                    color = palette.primaryText
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = libraryQuery,
                    onValueChange = onQueryChange,
                    placeholder = {
                        Text(
                            text = language.tr(
                                "ស្វែងរកក្នុងបណ្ណាល័យ (បទចម្រៀង តារាចម្រៀងខ្មែរ អាល់ប៊ុម…)",
                                "Search library (songs, Khmer artists, albums…)"
                            ),
                            color = palette.secondaryText
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search library",
                            tint = libraryAccent
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = libraryAccent,
                        unfocusedBorderColor = palette.divider,
                        focusedContainerColor = palette.surface,
                        unfocusedContainerColor = palette.surface,
                        focusedTextColor = palette.primaryText,
                        unfocusedTextColor = palette.primaryText
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("library_search_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.testTag("library_tabs_row")
                ) {
                    items(LibrarySubTab.entries) { tab ->
                        val isSelected = tab == selectedSubTab
                        val solidPillBg = if (palette.option.isDark) Color.White else Color(0xFF0F172A)
                        val solidPillText = if (palette.option.isDark) Color(0xFF0B0F17) else Color.White
                        val bg by animateColorAsState(
                            targetValue = if (isSelected) solidPillBg else palette.surface,
                            label = "libTabBg"
                        )
                        val textColor by animateColorAsState(
                            targetValue = if (isSelected) solidPillText else palette.secondaryText,
                            label = "libTabText"
                        )

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(bg)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) solidPillBg else palette.divider,
                                    shape = CircleShape
                                )
                                .clickable { onSelectSubTab(tab) }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .testTag("library_subtab_${tab.label.lowercase()}")
                        ) {
                            Text(
                                text = tab.localizedLabel(language),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = textColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sections 13 & 15: Quick Library Filter Pills (Liked Songs, Downloaded Music, Downloaded Videos, Recently Played, Local Music)
                val likedCount = remember(allMedia) { allMedia.count { it.isFavorite } }
                val downloadedMusicCount = remember(completedDownloads) { completedDownloads.count { !it.isVideo } }
                val downloadedVideoCount = remember(completedDownloads) { completedDownloads.count { it.isVideo } }
                val recentCount = remember(allMedia) { allMedia.count { it.lastPlayedTimestamp > 0L } }
                val localCount = remember(allMedia) { allMedia.count { it.localFilePath.isNotBlank() || it.inLibrary } }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("library_quick_sections_row")
                ) {
                    val quickSections = listOf(
                        Triple("LIKED", language.tr("បទចូលចិត្ត ($likedCount)", "Liked Songs ($likedCount)"), Icons.Filled.Favorite),
                        Triple("DL_AUDIO", language.tr("ចម្រៀងទាញយក ($downloadedMusicCount)", "Downloaded Music ($downloadedMusicCount)"), LumoIcons.MusicNote),
                        Triple("DL_VIDEO", language.tr("វីដេអូទាញយក ($downloadedVideoCount)", "Downloaded Videos ($downloadedVideoCount)"), LumoIcons.Videocam),
                        Triple("RECENT", language.tr("ទើបស្តាប់ថ្មីៗ ($recentCount)", "Recently Played ($recentCount)"), LumoIcons.History),
                        Triple("LOCAL", language.tr("តន្ត្រីក្នុងឧបករណ៍ ($localCount)", "Local Music ($localCount)"), LumoIcons.Download)
                    )
                    items(quickSections, key = { "lib_quick_${it.first}" }) { (id, title, icon) ->
                        Surface(
                            color = palette.elevatedSurface,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .border(1.dp, palette.divider, RoundedCornerShape(14.dp))
                                .clickable {
                                    when (id) {
                                        "DL_AUDIO", "DL_VIDEO" -> onSelectSubTab(LibrarySubTab.DOWNLOADS)
                                        else -> onSelectSubTab(LibrarySubTab.SONGS)
                                    }
                                }
                                .testTag("library_quick_section_${id.lowercase()}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = libraryAccent,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = palette.primaryText
                                )
                            }
                        }
                    }
                }
            }
        }

        when (selectedSubTab) {
            LibrarySubTab.SONGS -> {
                item {
                    Text(
                        text = language.tr(
                            "បទល្បីៗមុនគេ (${filteredMedia.size} បទ)",
                            "Popular Songs First (${filteredMedia.size})"
                        ),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = palette.primaryText,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
                itemsIndexed(filteredMedia, key = { _, m -> "lib_song_${m.id}" }) { index, media ->
                    CompactTrackRow(
                        media = media,
                        indexNumber = index + 1,
                        isCurrentlyPlaying = media.id == currentPlayingMediaId,
                        onPlay = { onPlayMedia(media, filteredMedia, true) },
                        onToggleFavorite = { onToggleFavorite(media) },
                        onDownload = if (onOpenDownloadSheet != null) {
                            { onOpenDownloadSheet(media) }
                        } else null,
                        onOpenOptions = { onOpenTrackOptions(media) }
                    )
                }
            }

            LibrarySubTab.ALBUMS -> {
                items(groupedAlbums, key = { "lib_album_${it.key}" }) { (albumName, tracks) ->
                    val rep = tracks.first()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(palette.surface)
                            .border(1.dp, palette.divider, RoundedCornerShape(18.dp))
                            .clickable { onPlayMedia(rep, tracks, false) }
                            .padding(12.dp)
                    ) {
                        LumoArtworkImage(
                            artworkUrl = rep.artworkUrl,
                            fallbackResId = rep.artworkResId,
                            contentDescription = albumName,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = albumName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = palette.primaryText
                            )
                            Text(
                                text = "${rep.artist} · ${tracks.size} tracks · ${rep.releaseYear}",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.secondaryText
                            )
                        }
                        IconButton(onClick = { onPlayMedia(rep, tracks, false) }) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play album",
                                tint = libraryAccent
                            )
                        }
                    }
                }
            }

            LibrarySubTab.ARTISTS -> {
                items(groupedArtists, key = { "lib_artist_${it.first}" }) { (artistName, sortedTracks) ->
                    val rep = sortedTracks.first()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(palette.surface)
                            .border(1.dp, palette.divider, RoundedCornerShape(18.dp))
                            .clickable {
                                onQueryChange(artistName)
                                onSelectSubTab(LibrarySubTab.SONGS)
                            }
                            .padding(12.dp)
                    ) {
                        LumoArtworkImage(
                            artworkUrl = rep.artworkUrl,
                            fallbackResId = rep.artworkResId,
                            contentDescription = artistName,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = artistName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = palette.primaryText
                            )
                            Text(
                                text = "${sortedTracks.size} songs & videos · Top: ${rep.title}",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.secondaryText
                            )
                        }
                        IconButton(onClick = { onPlayMedia(rep, sortedTracks, true) }) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play $artistName",
                                tint = libraryAccent
                            )
                        }
                    }
                }

                // Also list matching Khmer artists from directory so Library -> Artists always shows Khmer artists
                items(matchingKhmerProfiles, key = { "lib_khmer_dir_${it.id}" }) { profile ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(palette.surface)
                            .border(1.dp, palette.divider, RoundedCornerShape(18.dp))
                            .clickable {
                                onQueryChange(profile.englishName)
                                onSelectSubTab(LibrarySubTab.SONGS)
                            }
                            .padding(12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(libraryAccent.copy(alpha = 0.16f))
                                .border(1.dp, libraryAccent.copy(alpha = 0.45f), CircleShape)
                        ) {
                            Text(
                                text = profile.englishName.take(2).uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = libraryAccent
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.displayName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = palette.primaryText
                            )
                            Text(
                                text = "${profile.genreBadge} · Khmer Artist",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.secondaryText
                            )
                        }
                    }
                }
            }

            LibrarySubTab.PLAYLISTS -> {
                val chunked = (playlists.map<PlaylistEntity?, PlaylistEntity?> { it } + listOf(null)).chunked(2)
                items(chunked.size) { rowIndex ->
                    val rowItems = chunked[rowIndex]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        rowItems.forEach { playlist ->
                            if (playlist != null) {
                                PlaylistGridCard(
                                    playlist = playlist,
                                    onClick = { onOpenPlaylistDetail(playlist.id) },
                                    onChangePhoto = { playlistForPhotoPicker = playlist },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                NewPlaylistGridCard(
                                    accentColor = libraryAccent,
                                    onClick = { showCreatePlaylistDialog = true },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            LibrarySubTab.DOWNLOADS -> {
                if (completedDownloads.isEmpty()) {
                    item {
                        Text(
                            text = "No offline downloads yet. Tap the Download icon on any song or video to save it offline.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.secondaryText,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                        )
                    }
                } else {
                    items(completedDownloads, key = { "lib_dl_${it.id}" }) { task ->
                        val matchedMedia = allMedia.find { it.id == task.mediaId }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(palette.surface)
                                .border(1.dp, palette.divider, RoundedCornerShape(16.dp))
                                .clickable {
                                    if (matchedMedia != null) {
                                        onPlayMedia(matchedMedia, allMedia, matchedMedia.isVideo)
                                    }
                                }
                                .padding(12.dp)
                        ) {
                            LumoArtworkImage(
                                artworkUrl = task.artworkUrl,
                                fallbackResId = task.artworkResId,
                                contentDescription = task.title,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = palette.primaryText
                                )
                                Text(
                                    text = "${task.artist} · ${task.format} · ${task.formattedSizeMb}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.secondaryText
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreatePlaylistDialog) {
        CreateOrEditCustomPlaylistDialog(
            initialName = "",
            initialDescription = "",
            initialCoverResName = "img_album_neon_echoes",
            initialCoverUrl = "",
            availableMediaForCovers = allMedia,
            titleText = language.tr("បង្កើតបញ្ជីចាក់ថ្មី", "Create Custom Playlist"),
            accentColor = libraryAccent,
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
            availableMediaForCovers = allMedia,
            accentColor = libraryAccent,
            onPickGalleryPhotoUri = { uri ->
                onUpdatePlaylistPhotoUri(targetPlaylist, uri)
            },
            onSelectCustomCover = { coverRes, coverUrl ->
                onUpdatePlaylistCustomCover(targetPlaylist, coverRes, coverUrl)
            },
            onDismiss = { playlistForPhotoPicker = null }
        )
    }
}

@Composable
private fun PlaylistGridCard(
    playlist: PlaylistEntity,
    onClick: () -> Unit,
    onChangePhoto: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val palette = LocalLumoPalette.current
    val language = LocalLumoLanguage.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(palette.surface)
            .border(1.dp, palette.divider, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag("playlist_card_${playlist.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
                .clip(RoundedCornerShape(14.dp))
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
                    .testTag("library_playlist_photo_btn_${playlist.id}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = LumoIcons.Image,
                        contentDescription = "Change playlist photo",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = language.tr("រូបភាព", "Photo"),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = playlist.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = palette.primaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${playlist.trackIds.size} tracks",
            style = MaterialTheme.typography.bodySmall,
            color = palette.secondaryText
        )
    }
}

@Composable
private fun NewPlaylistGridCard(
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalLumoPalette.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .height(198.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(palette.surface)
            .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag("create_new_playlist_card")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.16f))
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "New Playlist",
                tint = accentColor,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "New Playlist",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = palette.primaryText
        )
        Text(
            text = "Custom cover & order",
            style = MaterialTheme.typography.bodySmall,
            color = palette.secondaryText
        )
    }
}

@Composable
private fun PlaylistDetailView(
    playlist: PlaylistEntity,
    allMedia: List<MediaItemEntity>,
    onBack: () -> Unit,
    onPlayPlaylist: (Boolean) -> Unit,
    onRename: (String, String) -> Unit,
    onOpenPhotoPicker: () -> Unit,
    onPickGalleryPhotoUri: (Uri, (String) -> Unit) -> Unit = { _, _ -> },
    onUpdateCustomCover: (String, String) -> Unit = { _, _ -> },
    onDelete: () -> Unit,
    onMoveTrack: (Int, Int) -> Unit,
    onRemoveTrack: (String) -> Unit,
    onAddTrack: (MediaItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalLumoPalette.current
    var showRenameDialog by remember { mutableStateOf(false) }
    val playlistTracks = playlist.trackIds.mapNotNull { id -> allMedia.find { it.id == id } }
    val availableToAdd = allMedia.filter { m -> !playlist.trackIds.contains(m.id) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("playlist_detail_screen"),
        contentPadding = PaddingValues(bottom = 140.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = palette.primaryText
                        )
                    }
                    Row {
                        IconButton(onClick = onOpenPhotoPicker) {
                            Icon(
                                imageVector = LumoIcons.Image,
                                contentDescription = "Change custom artwork",
                                tint = palette.libraryAccent
                            )
                        }
                        IconButton(onClick = { showRenameDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Rename playlist",
                                tint = palette.primaryText
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete playlist",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, palette.divider, RoundedCornerShape(20.dp))
                            .clickable(onClick = onOpenPhotoPicker)
                    ) {
                        LumoArtworkImage(
                            artworkUrl = playlist.coverUrl,
                            fallbackResId = playlist.coverResId,
                            contentDescription = playlist.name,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp)
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.75f))
                        ) {
                            Icon(
                                imageVector = LumoIcons.Image,
                                contentDescription = "Custom photo",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = playlist.name,
                            style = MaterialTheme.typography.headlineLarge,
                            color = palette.primaryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = playlist.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.secondaryText,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${playlistTracks.size} tracks",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.libraryAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onPlayPlaylist(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = palette.libraryAccent),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Play All", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { onPlayPlaylist(true) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(LumoIcons.Shuffle, contentDescription = null, tint = palette.primaryText)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Shuffle", color = palette.primaryText, fontWeight = FontWeight.Bold)
                    }
                    val context = LocalContext.current
                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Check out my playlist \"${playlist.name}\" (${playlistTracks.size} songs) on LUMO — Discover. Play. Save."
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Playlist"))
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("playlist_share_button")
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share Playlist", tint = palette.primaryText)
                    }
                }
            }
        }

        itemsIndexed(playlistTracks, key = { _, item -> "pl_track_${item.id}" }) { index, media ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.surface)
                    .padding(10.dp)
            ) {
                LumoArtworkImage(
                    artworkUrl = media.artworkUrl,
                    fallbackResId = media.artworkResId,
                    contentDescription = media.title,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = palette.primaryText,
                        maxLines = 1
                    )
                    Text(
                        text = media.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.secondaryText
                    )
                }
                IconButton(
                    onClick = { if (index > 0) onMoveTrack(index, index - 1) },
                    enabled = index > 0
                ) {
                    Icon(
                        imageVector = LumoIcons.ArrowUpward,
                        contentDescription = "Move Up",
                        tint = if (index > 0) palette.primaryText else palette.divider,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { if (index < playlistTracks.lastIndex) onMoveTrack(index, index + 1) },
                    enabled = index < playlistTracks.lastIndex
                ) {
                    Icon(
                        imageVector = LumoIcons.ArrowDownward,
                        contentDescription = "Move Down",
                        tint = if (index < playlistTracks.lastIndex) palette.primaryText else palette.divider,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = { onRemoveTrack(media.id) }) {
                    Icon(
                        imageVector = LumoIcons.RemoveCircleOutline,
                        contentDescription = "Remove from playlist",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (availableToAdd.isNotEmpty()) {
            item {
                Text(
                    text = "Quick Add Tracks",
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.secondaryText,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
            items(availableToAdd, key = { "add_cand_${it.id}" }) { media ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    LumoArtworkImage(
                        artworkUrl = media.artworkUrl,
                        fallbackResId = media.artworkResId,
                        contentDescription = media.title,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = media.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = palette.primaryText
                        )
                        Text(
                            text = media.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.secondaryText
                        )
                    }
                    TextButton(onClick = { onAddTrack(media) }) {
                        Text("+ Add", color = palette.libraryAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        CreateOrEditPlaylistDialog(
            initialName = playlist.name,
            initialDescription = playlist.description,
            titleText = "Rename Playlist",
            accentColor = palette.libraryAccent,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName, newDesc ->
                onRename(newName, newDesc)
                showRenameDialog = false
            }
        )
    }
}

@Composable
private fun CreateOrEditPlaylistDialog(
    initialName: String,
    initialDescription: String,
    titleText: String,
    accentColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    val palette = LocalLumoPalette.current
    var name by remember { mutableStateOf(initialName) }
    var desc by remember { mutableStateOf(initialDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        titleContentColor = palette.primaryText,
        title = { Text(titleText) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Playlist Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, desc) },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = palette.secondaryText)
            }
        }
    )
}
