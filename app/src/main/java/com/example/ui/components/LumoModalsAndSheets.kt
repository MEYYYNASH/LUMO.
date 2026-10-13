package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import com.example.data.DownloadFormatOption
import com.example.data.DownloadTaskEntity
import com.example.data.MediaItemEntity
import com.example.data.PlaylistEntity
import com.example.ui.theme.LocalLumoPalette
import com.example.ui.theme.LumoIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LumoDownloadBottomSheet(
    media: MediaItemEntity,
    defaultFormatId: String,
    onDismiss: () -> Unit,
    onConfirmDownload: (DownloadFormatOption) -> Unit
) {
    val palette = LocalLumoPalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedFormat by remember(media.id) {
        val initial = if (media.isVideo) {
            DownloadFormatOption.MP4_720P
        } else {
            DownloadFormatOption.entries.find { it.id == defaultFormatId && !it.isVideo }
                ?: DownloadFormatOption.M4A
        }
        mutableStateOf(initial)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.surface,
        contentColor = palette.primaryText,
        modifier = Modifier.testTag("download_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = "Download",
                style = MaterialTheme.typography.headlineMedium,
                color = palette.primaryText
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.elevatedSurface)
                    .padding(12.dp)
            ) {
                LumoArtworkImage(
                    artworkUrl = media.artworkUrl,
                    fallbackResId = media.artworkResId,
                    contentDescription = media.title,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.primaryText
                    )
                    Text(
                        text = "${media.artist} · ${media.formattedDuration}",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.secondaryText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = LumoIcons.VerifiedUser,
                            contentDescription = "Authorized source",
                            tint = palette.libraryAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = media.licenseBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.libraryAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Audio",
                style = MaterialTheme.typography.labelLarge,
                color = palette.secondaryText
            )
            HorizontalDivider(
                color = palette.divider,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            listOf(DownloadFormatOption.MP3, DownloadFormatOption.M4A).forEach { format ->
                FormatOptionRow(
                    format = format,
                    isSelected = selectedFormat == format,
                    accentColor = palette.downloadsAccent,
                    onSelect = { selectedFormat = format }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Video",
                style = MaterialTheme.typography.labelLarge,
                color = palette.secondaryText
            )
            HorizontalDivider(
                color = palette.divider,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            listOf(
                DownloadFormatOption.MP4_360P,
                DownloadFormatOption.MP4_720P,
                DownloadFormatOption.MP4_1080P
            ).forEach { format ->
                FormatOptionRow(
                    format = format,
                    isSelected = selectedFormat == format,
                    accentColor = palette.downloadsAccent,
                    onSelect = { selectedFormat = format }
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = { onConfirmDownload(selectedFormat) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.downloadsAccent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("confirm_download_button")
            ) {
                Icon(
                    imageVector = LumoIcons.Download,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Download ${selectedFormat.label}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun FormatOptionRow(
    format: DownloadFormatOption,
    isSelected: Boolean,
    accentColor: Color,
    onSelect: () -> Unit
) {
    val palette = LocalLumoPalette.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = onSelect)
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .testTag("format_option_${format.id.replace(" ", "_")}")
    ) {
        Icon(
            imageVector = if (isSelected) LumoIcons.RadioButtonChecked else LumoIcons.RadioButtonUnchecked,
            contentDescription = format.label,
            tint = if (isSelected) accentColor else palette.secondaryText,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = format.label,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = palette.primaryText
            )
            Text(
                text = format.subLabel,
                style = MaterialTheme.typography.bodySmall,
                color = palette.secondaryText
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LumoTrackOptionsBottomSheet(
    media: MediaItemEntity,
    onDismiss: () -> Unit,
    onPlayNow: () -> Unit,
    onPlayNextInQueue: () -> Unit = {},
    onOpenDetailsPlayer: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDownload: () -> Unit,
    onEditMetadata: () -> Unit = {},
    onRemoveFromApp: () -> Unit
) {
    val palette = LocalLumoPalette.current
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.surface,
        contentColor = palette.primaryText,
        modifier = Modifier.testTag("track_options_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                LumoArtworkImage(
                    artworkUrl = media.artworkUrl,
                    fallbackResId = media.artworkResId,
                    contentDescription = media.title,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.primaryText
                    )
                    Text(
                        text = "${media.artist} · ${media.album}",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.secondaryText
                    )
                }
            }

            HorizontalDivider(color = palette.divider)
            Spacer(modifier = Modifier.height(8.dp))

            OptionSheetRow(
                icon = Icons.Filled.PlayArrow,
                label = "Play Now",
                tint = palette.primaryAccent,
                onClick = {
                    onPlayNow()
                    onDismiss()
                }
            )
            OptionSheetRow(
                icon = Icons.Filled.Info,
                label = if (media.isVideo) "Open Fullscreen Video Player" else "Open Hero Music Player",
                tint = palette.primaryText,
                onClick = {
                    onOpenDetailsPlayer()
                    onDismiss()
                }
            )
            OptionSheetRow(
                icon = LumoIcons.PlaylistAdd,
                label = "Add to Playlist",
                tint = palette.libraryAccent,
                onClick = onAddToPlaylist
            )
            OptionSheetRow(
                icon = if (media.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                label = if (media.isFavorite) "Remove from Favorites" else "Add to Favorites",
                tint = if (media.isFavorite) Color(0xFFEF4444) else palette.primaryText,
                onClick = {
                    onToggleFavorite()
                    onDismiss()
                }
            )
            if (media.isAuthorizedDownload) {
                OptionSheetRow(
                    icon = LumoIcons.Download,
                    label = "Download Authorized Media",
                    tint = palette.downloadsAccent,
                    onClick = onDownload
                )
            }
            OptionSheetRow(
                icon = LumoIcons.QueueMusic,
                label = "Play Next in Queue",
                tint = palette.homeAccent,
                onClick = {
                    onPlayNextInQueue()
                    onDismiss()
                }
            )
            OptionSheetRow(
                icon = Icons.Filled.Edit,
                label = "Edit Song Metadata & Artwork",
                tint = palette.searchAccent,
                onClick = {
                    onEditMetadata()
                    onDismiss()
                }
            )
            OptionSheetRow(
                icon = Icons.Filled.Share,
                label = "Share Track",
                tint = palette.secondaryText,
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Listening to \"${media.title}\" by ${media.artist} on LUMO — Discover. Play. Save."
                        )
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                    onDismiss()
                }
            )
            OptionSheetRow(
                icon = Icons.Filled.Delete,
                label = "Remove Song from App",
                tint = Color(0xFFEF4444),
                onClick = onRemoveFromApp
            )
        }
    }
}

@Composable
private fun OptionSheetRow(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    val palette = LocalLumoPalette.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = palette.primaryText
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistBottomSheet(
    media: MediaItemEntity,
    playlists: List<PlaylistEntity>,
    onDismiss: () -> Unit,
    onSelectPlaylist: (PlaylistEntity) -> Unit,
    onCreateNewPlaylistWithTrack: (String, String) -> Unit
) {
    val palette = LocalLumoPalette.current
    var showCreateField by remember { mutableStateOf(playlists.isEmpty()) }
    var newPlaylistName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        contentColor = palette.primaryText
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = "Add to Playlist",
                style = MaterialTheme.typography.headlineMedium,
                color = palette.primaryText
            )
            Text(
                text = "${media.title} · ${media.artist}",
                style = MaterialTheme.typography.bodySmall,
                color = palette.secondaryText
            )
            Spacer(modifier = Modifier.height(16.dp))

            playlists.forEach { playlist ->
                val alreadyIn = playlist.trackIds.contains(media.id)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectPlaylist(playlist) }
                        .padding(vertical = 10.dp, horizontal = 8.dp)
                ) {
                    LumoArtworkImage(
                        artworkUrl = playlist.coverUrl,
                        fallbackResId = playlist.coverResId,
                        contentDescription = playlist.name,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = playlist.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = palette.primaryText
                        )
                        Text(
                            text = "${playlist.trackIds.size} tracks",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.secondaryText
                        )
                    }
                    if (alreadyIn) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Already in playlist",
                            tint = palette.libraryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            if (showCreateField) {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("New Playlist Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            onCreateNewPlaylistWithTrack(newPlaylistName, media.artworkResName)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.libraryAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create & Add Track", color = Color.White)
                }
            } else {
                TextButton(
                    onClick = { showCreateField = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("+ Create New Playlist", color = palette.libraryAccent)
                }
            }
        }
    }
}

@Composable
fun RenameDownloadDialog(
    task: DownloadTaskEntity,
    onDismiss: () -> Unit,
    onConfirmRename: (String) -> Unit
) {
    val palette = LocalLumoPalette.current
    var titleText by remember(task.id) { mutableStateOf(task.title) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        titleContentColor = palette.primaryText,
        textContentColor = palette.secondaryText,
        title = { Text("Rename Downloaded Media") },
        text = {
            Column {
                Text(
                    text = "Format: ${task.format} · ${task.formattedSizeMb}",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.secondaryText
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    singleLine = true,
                    label = { Text("Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rename_download_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmRename(titleText) },
                colors = ButtonDefaults.buttonColors(containerColor = palette.downloadsAccent)
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
