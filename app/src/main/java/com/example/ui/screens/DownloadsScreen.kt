package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DownloadFormatOption
import com.example.data.DownloadTaskEntity
import com.example.data.MediaItemEntity
import com.example.ui.components.LumoArtworkImage
import com.example.ui.theme.LocalLumoLanguage
import com.example.ui.theme.LocalLumoPalette
import com.example.ui.theme.LumoIcons

@Composable
fun DownloadsScreen(
    allDownloads: List<DownloadTaskEntity>,
    allMedia: List<MediaItemEntity>,
    youtubeLinkInput: String,
    isResolvingYouTubeLink: Boolean,
    onYouTubeLinkInputChange: (String) -> Unit,
    onResolveAndDownloadYouTubeLink: (String, DownloadFormatOption) -> Unit,
    onOpenSearchToDownload: () -> Unit,
    onPauseOrResume: (DownloadTaskEntity) -> Unit,
    onPlayDownloaded: (DownloadTaskEntity) -> Unit,
    onOpenDownloadedFull: (DownloadTaskEntity) -> Unit,
    onRenameDownload: (DownloadTaskEntity) -> Unit,
    onDeleteDownload: (DownloadTaskEntity) -> Unit,
    onAddToPlaylist: (DownloadTaskEntity) -> Unit,
    onOpenDownloadPickerForNew: (MediaItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalLumoPalette.current
    val language = LocalLumoLanguage.current
    val downloadsAccent = palette.downloadsAccent
    val context = LocalContext.current

    var selectedFilterTab by remember { mutableStateOf("ALL") } // ALL, AUDIO, VIDEO, COMPLETED

    val activeDownloads = remember(allDownloads, selectedFilterTab) {
        allDownloads.filter { task ->
            val isNotComp = task.status != "COMPLETED"
            val matchesTab = when (selectedFilterTab) {
                "AUDIO" -> !task.isVideo
                "VIDEO" -> task.isVideo
                "COMPLETED" -> false
                else -> true
            }
            isNotComp && matchesTab
        }
    }
    val completedDownloads = remember(allDownloads, selectedFilterTab) {
        allDownloads.filter { task ->
            val isComp = task.status == "COMPLETED"
            val matchesTab = when (selectedFilterTab) {
                "AUDIO" -> !task.isVideo
                "VIDEO" -> task.isVideo
                else -> true
            }
            isComp && matchesTab
        }
    }
    val quickDownloadCandidates = remember(allMedia, allDownloads) {
        val completedIds = allDownloads.filter { it.status == "COMPLETED" }.mapTo(HashSet()) { it.mediaId }
        com.example.data.KhmerArtistDirectory.sortPopularSongsFirst(
            allMedia.filter { it.isAuthorizedDownload && it.id !in completedIds }
        ).take(12)
    }

    val solidPillBg = if (palette.option.isDark) Color.White else Color(0xFF0F172A)
    val solidPillText = if (palette.option.isDark) Color(0xFF0B0F17) else Color.White

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("downloads_screen_list"),
        contentPadding = PaddingValues(bottom = 148.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = language.tr("ការទាញយក", "Downloads", "ការទាញយក · Downloads"),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = palette.primaryText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = language.tr(
                                "ទាញយកបទចម្រៀង និងវីដេអូសម្រាប់ស្តាប់ក្រៅបណ្តាញ (MP3 / MP4)",
                                "Download YouTube videos by link or search songs"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.secondaryText
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(solidPillBg)
                            .clickable(onClick = onOpenSearchToDownload)
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                            .testTag("new_authorized_download_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = solidPillText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Search Song",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = solidPillText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Inline YouTube Video / Audio Link Downloader Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(palette.surface)
                        .border(1.dp, palette.divider, RoundedCornerShape(22.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = LumoIcons.YouTubePlay,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Download YouTube Video or MP3 by Link",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = palette.primaryText
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = youtubeLinkInput,
                        onValueChange = onYouTubeLinkInputChange,
                        placeholder = {
                            Text(
                                text = "Paste YouTube link (https://youtu.be/...)",
                                color = palette.secondaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = LumoIcons.Link,
                                contentDescription = "Link",
                                tint = downloadsAccent
                            )
                        },
                        trailingIcon = {
                            if (isResolvingYouTubeLink) {
                                CircularProgressIndicator(
                                    color = downloadsAccent,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else if (youtubeLinkInput.isNotEmpty()) {
                                IconButton(onClick = { onYouTubeLinkInputChange("") }) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Clear",
                                        tint = palette.secondaryText
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        val clipText = clipboard?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()?.trim().orEmpty()
                                        val url = clipText.ifEmpty { "https://www.youtube.com/watch?v=4NRXx6U8ABQ" }
                                        onYouTubeLinkInputChange(url)
                                    }
                                ) {
                                    Icon(
                                        imageVector = LumoIcons.ContentPaste,
                                        contentDescription = "Paste link",
                                        tint = palette.primaryText
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = palette.primaryText.copy(alpha = 0.4f),
                            unfocusedBorderColor = palette.divider,
                            focusedContainerColor = palette.elevatedSurface,
                            unfocusedContainerColor = palette.elevatedSurface,
                            focusedTextColor = palette.primaryText,
                            unfocusedTextColor = palette.primaryText
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("downloads_youtube_link_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Exact Image 3 Dual Pill Row: [ ⬇ Download MP4 Video ] [ 🎵 Download MP3 Audio ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(solidPillBg)
                                .clickable {
                                    val target = youtubeLinkInput.ifBlank { "https://www.youtube.com/watch?v=4NRXx6U8ABQ" }
                                    onYouTubeLinkInputChange(target)
                                    onResolveAndDownloadYouTubeLink(target, DownloadFormatOption.MP4_1080P)
                                }
                                .testTag("downloads_link_mp4_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = LumoIcons.Videocam,
                                    contentDescription = null,
                                    tint = solidPillText,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Download MP4",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = solidPillText
                                )
                            }
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(palette.elevatedSurface)
                                .border(1.dp, palette.divider, RoundedCornerShape(13.dp))
                                .clickable {
                                    val target = youtubeLinkInput.ifBlank { "https://www.youtube.com/watch?v=4NRXx6U8ABQ" }
                                    onYouTubeLinkInputChange(target)
                                    onResolveAndDownloadYouTubeLink(target, DownloadFormatOption.MP3_320)
                                }
                                .testTag("downloads_link_mp3_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = LumoIcons.MusicNote,
                                    contentDescription = null,
                                    tint = palette.primaryText,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Download MP3",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = palette.primaryText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = language.tr(
                            "ការរក្សាទុក និងទាញយកមានសម្រាប់តែមាតិកាដែលអ្នកមានសិទ្ធិអនុញ្ញាតប៉ុណ្ណោះ។",
                            "Authorized media saving only: Downloading/importing is available for content you have permission to save."
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.secondaryText
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 16: Download Manager Filter Tabs (All, Audio, Video, Completed)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("download_manager_filter_tabs"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf(
                        "ALL" to language.tr("ទាំងអស់", "All"),
                        "AUDIO" to language.tr("សំឡេង", "Audio"),
                        "VIDEO" to language.tr("វីដេអូ", "Video"),
                        "COMPLETED" to language.tr("បានបញ្ចប់", "Completed")
                    )
                    tabs.forEach { (tabId, tabLabel) ->
                        val isSelected = selectedFilterTab == tabId
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .background(if (isSelected) solidPillBg else palette.surface)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) solidPillBg else palette.divider,
                                    shape = CircleShape
                                )
                                .clickable { selectedFilterTab = tabId }
                                .padding(vertical = 8.dp)
                                .testTag("download_tab_${tabId.lowercase()}")
                        ) {
                            Text(
                                text = tabLabel,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) solidPillText else palette.secondaryText,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(downloadsAccent)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Downloading (${activeDownloads.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.primaryText
                )
            }
        }

        if (activeDownloads.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.surface)
                        .border(1.dp, palette.divider, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "No active downloads. Paste a YouTube link above or tap 'Search Song' to download MP3 / MP4.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.secondaryText
                    )
                }
            }
        } else {
            items(activeDownloads, key = { "active_dl_${it.id}" }) { task ->
                ActiveDownloadCard(
                    task = task,
                    accentColor = downloadsAccent,
                    onPauseOrResume = { onPauseOrResume(task) },
                    onCancel = { onDeleteDownload(task) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(palette.libraryAccent)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Downloaded (${completedDownloads.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.primaryText
                )
            }
        }

        if (completedDownloads.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.surface)
                        .border(1.dp, palette.divider, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Your downloaded MP3 songs and MP4 videos will appear here for offline playback.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.secondaryText
                    )
                }
            }
        } else {
            items(completedDownloads, key = { "comp_dl_${it.id}" }) { task ->
                CompletedDownloadCard(
                    task = task,
                    onPlay = { onPlayDownloaded(task) },
                    onOpenFull = { onOpenDownloadedFull(task) },
                    onRename = { onRenameDownload(task) },
                    onDelete = { onDeleteDownload(task) },
                    onAddToPlaylist = { onAddToPlaylist(task) },
                    onShare = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Downloaded \"${task.title}\" (${task.format} · ${task.formattedSizeMb}) on LUMO"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share media"))
                    }
                )
            }
        }

        if (quickDownloadCandidates.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(downloadsAccent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Quick Download · World & Popular Songs",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = palette.primaryText
                    )
                }
            }

            items(quickDownloadCandidates, key = { "quick_dl_cand_${it.id}" }) { media ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.surface)
                        .border(1.dp, palette.divider, RoundedCornerShape(16.dp))
                        .clickable { onOpenDownloadPickerForNew(media) }
                        .padding(12.dp)
                        .testTag("quick_download_item_${media.id}")
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
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${media.artist} · ${media.formattedPopularity} · ${media.formattedDuration}",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.secondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = solidPillBg,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .clickable { onOpenDownloadPickerForNew(media) }
                            .testTag("quick_download_btn_${media.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = LumoIcons.Download,
                                contentDescription = "Download ${media.title}",
                                tint = solidPillText,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Download",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = solidPillText
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveDownloadCard(
    task: DownloadTaskEntity,
    accentColor: Color,
    onPauseOrResume: () -> Unit,
    onCancel: () -> Unit
) {
    val palette = LocalLumoPalette.current
    val isDownloading = task.status == "DOWNLOADING"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(palette.surface)
            .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
            .padding(14.dp)
            .testTag("active_download_card_${task.id}")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${task.format} · ${task.formattedSizeMb} · ${if (isDownloading) "Downloading…" else "Paused"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.secondaryText
                )
            }
            IconButton(
                onClick = onPauseOrResume,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.16f))
                    .testTag("active_download_toggle_${task.id}")
            ) {
                Icon(
                    imageVector = if (isDownloading) LumoIcons.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isDownloading) "Pause download" else "Resume download",
                    tint = accentColor
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Cancel download",
                    tint = palette.secondaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LinearProgressIndicator(
                progress = { (task.progressPercent / 100f).coerceIn(0f, 1f) },
                color = accentColor,
                trackColor = palette.divider,
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(CircleShape)
            )
            Text(
                text = "${task.progressPercent}%",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = accentColor
            )
        }
    }
}

@Composable
private fun CompletedDownloadCard(
    task: DownloadTaskEntity,
    onPlay: () -> Unit,
    onOpenFull: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onShare: () -> Unit
) {
    val palette = LocalLumoPalette.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(palette.surface)
            .border(1.dp, palette.divider, RoundedCornerShape(18.dp))
            .clickable(onClick = onPlay)
            .padding(14.dp)
            .testTag("completed_download_card_${task.id}")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                LumoArtworkImage(
                    artworkUrl = task.artworkUrl,
                    fallbackResId = task.artworkResId,
                    contentDescription = task.title,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(3.dp)
                ) {
                    Icon(
                        imageVector = if (task.isVideo) LumoIcons.Videocam else LumoIcons.MusicNote,
                        contentDescription = if (task.isVideo) "Video" else "Audio",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(3.dp)
                            .size(11.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${task.artist} · ${task.format} · ${task.formattedSizeMb}",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.secondaryText
                )
            }

            IconButton(
                onClick = onPlay,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(palette.elevatedSurface)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play",
                    tint = palette.primaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SmallActionChip(
                icon = LumoIcons.OpenInFull,
                label = "Open",
                onClick = onOpenFull
            )
            SmallActionChip(
                icon = Icons.Filled.Edit,
                label = "Rename",
                onClick = onRename
            )
            SmallActionChip(
                icon = LumoIcons.PlaylistAdd,
                label = "Playlist",
                onClick = onAddToPlaylist
            )
            SmallActionChip(
                icon = Icons.Filled.Share,
                label = "Share",
                onClick = onShare
            )
            SmallActionChip(
                icon = Icons.Filled.Delete,
                label = "Delete",
                tint = Color(0xFFEF4444),
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun SmallActionChip(
    icon: ImageVector,
    label: String,
    tint: Color? = null,
    onClick: () -> Unit
) {
    val palette = LocalLumoPalette.current
    val resolvedTint = tint ?: palette.secondaryText
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(palette.elevatedSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = resolvedTint,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = resolvedTint
        )
    }
}
