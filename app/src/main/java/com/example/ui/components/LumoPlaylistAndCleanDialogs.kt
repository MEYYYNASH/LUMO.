package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.MediaItemEntity
import com.example.data.PlaylistEntity
import com.example.data.resolveArtworkRes
import com.example.ui.theme.LocalLumoLanguage
import com.example.ui.theme.LocalLumoPalette
import com.example.ui.theme.LumoIcons

data class StudioCoverPreset(
    val resName: String,
    val label: String
)

val studioCoverPresets = listOf(
    StudioCoverPreset("img_album_neon_echoes", "Neon"),
    StudioCoverPreset("img_album_velvet_drift", "Velvet"),
    StudioCoverPreset("img_album_solar_pulse", "Solar"),
    StudioCoverPreset("img_video_aurora_session", "Aurora")
)

@Composable
fun CreateOrEditCustomPlaylistDialog(
    initialName: String = "",
    initialDescription: String = "",
    initialCoverResName: String = "img_album_neon_echoes",
    initialCoverUrl: String = "",
    availableMediaForCovers: List<MediaItemEntity> = emptyList(),
    titleText: String,
    accentColor: Color,
    onPickGalleryPhotoUri: (Uri, (String) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String, coverResName: String, coverUrl: String) -> Unit
) {
    val palette = LocalLumoPalette.current
    val language = LocalLumoLanguage.current
    var name by remember { mutableStateOf(initialName) }
    var desc by remember { mutableStateOf(initialDescription) }
    var selectedCoverResName by remember {
        mutableStateOf(initialCoverResName.ifBlank { "img_album_neon_echoes" })
    }
    var selectedCoverUrl by remember { mutableStateOf(initialCoverUrl) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onPickGalleryPhotoUri(uri) { savedFilePath ->
                selectedCoverUrl = savedFilePath
            }
        }
    }

    val mediaCovers = remember(availableMediaForCovers) {
        availableMediaForCovers.filter { it.artworkUrl.isNotBlank() }.distinctBy { it.artworkUrl }.take(12)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        titleContentColor = palette.primaryText,
        title = {
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Custom Cover Preview + Gallery Button Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.elevatedSurface)
                        .border(1.dp, palette.divider, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    ) {
                        LumoArtworkImage(
                            artworkUrl = selectedCoverUrl,
                            fallbackResId = resolveArtworkRes(selectedCoverResName),
                            contentDescription = "Playlist Cover Preview",
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.75f))
                        ) {
                            Icon(
                                imageVector = LumoIcons.Image,
                                contentDescription = "Pick custom photo",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = language.tr(
                                "រូបភាពបញ្ជីចាក់ផ្ទាល់ខ្លួន",
                                "Custom Playlist Photo",
                                "រូបភាពផ្ទាល់ខ្លួន · Custom Photo"
                            ),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = palette.primaryText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pick_playlist_gallery_photo_button")
                        ) {
                            Icon(
                                imageVector = LumoIcons.Image,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = language.tr("ជ្រើសរើសរូបពីទូរស័ព្ទ", "Pick from Gallery", "ជ្រើសរើសរូប · Gallery"),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = palette.primaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Studio presets & song covers
                Text(
                    text = language.tr(
                        "ឬជ្រើសរើសរូបភាពគំរូ / បទចម្រៀង៖",
                        "Or pick a studio / song cover:"
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.secondaryText
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(studioCoverPresets, key = { it.resName }) { preset ->
                        val isSelected = selectedCoverUrl.isBlank() && selectedCoverResName == preset.resName
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) accentColor else palette.divider,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedCoverResName = preset.resName
                                    selectedCoverUrl = ""
                                }
                        ) {
                            LumoArtworkImage(
                                artworkUrl = "",
                                fallbackResId = resolveArtworkRes(preset.resName),
                                contentDescription = preset.label,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (isSelected) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.35f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    items(mediaCovers, key = { "dlg_cov_${it.id}" }) { media ->
                        val isSelected = selectedCoverUrl == media.artworkUrl
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) accentColor else palette.divider,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedCoverUrl = media.artworkUrl
                                }
                        ) {
                            LumoArtworkImage(
                                artworkUrl = media.artworkUrl,
                                fallbackResId = media.artworkResId,
                                contentDescription = media.title,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (isSelected) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.35f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = {
                        Text(language.tr("ឈ្មោះបញ្ជីចាក់", "Playlist Name", "ឈ្មោះ · Playlist Name"))
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("playlist_name_input")
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = {
                        Text(language.tr("ការពិពណ៌នា", "Description", "ការពិពណ៌នា · Description"))
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("playlist_desc_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        name.trim().ifEmpty { "My Playlist" },
                        desc.trim().ifEmpty { "Custom Playlist" },
                        selectedCoverResName,
                        selectedCoverUrl
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                modifier = Modifier.testTag("playlist_confirm_save_button")
            ) {
                Text(
                    text = language.tr("រក្សាទុក", "Save", "រក្សាទុក · Save"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = language.tr("បោះបង់", "Cancel", "Cancel"),
                    color = palette.secondaryText
                )
            }
        }
    )
}

@Composable
fun CustomPlaylistPhotoPickerDialog(
    playlist: PlaylistEntity,
    availableMediaForCovers: List<MediaItemEntity>,
    accentColor: Color,
    onPickGalleryPhotoUri: (Uri) -> Unit,
    onSelectCustomCover: (coverResName: String, coverUrl: String) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalLumoPalette.current
    val language = LocalLumoLanguage.current
    var customUrlInput by remember(playlist.id) {
        mutableStateOf(if (playlist.coverUrl.startsWith("http")) playlist.coverUrl else "")
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onPickGalleryPhotoUri(uri)
            onDismiss()
        }
    }

    val mediaCovers = remember(availableMediaForCovers) {
        availableMediaForCovers.filter { it.artworkUrl.isNotBlank() }.distinctBy { it.artworkUrl }.take(16)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        titleContentColor = palette.primaryText,
        title = {
            Text(
                text = language.tr(
                    "ប្តូររូបភាពបញ្ជីចាក់ · \"${playlist.name}\"",
                    "Custom Cover · \"${playlist.name}\""
                ),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Current preview + Gallery Picker button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.elevatedSurface)
                        .border(1.dp, palette.divider, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    LumoArtworkImage(
                        artworkUrl = playlist.coverUrl,
                        fallbackResId = playlist.coverResId,
                        contentDescription = playlist.name,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("choose_gallery_photo_for_playlist_button")
                    ) {
                        Icon(
                            imageVector = LumoIcons.Image,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = language.tr(
                                "ជ្រើសរើសរូបពីទូរស័ព្ទ",
                                "Pick Photo from Phone",
                                "រូបពីទូរស័ព្ទ · Gallery"
                            ),
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (mediaCovers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = language.tr(
                            "ជ្រើសរើសរូបពីបទចម្រៀងដែលបានទាញយក៖",
                            "Use Cover from Your Songs:"
                        ),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.primaryText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(mediaCovers, key = { "pick_song_cov_${it.id}" }) { media ->
                            val isSelected = playlist.coverUrl == media.artworkUrl
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) accentColor else palette.divider,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onSelectCustomCover(playlist.coverResName, media.artworkUrl)
                                        onDismiss()
                                    }
                            ) {
                                LumoArtworkImage(
                                    artworkUrl = media.artworkUrl,
                                    fallbackResId = media.artworkResId,
                                    contentDescription = media.title,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = language.tr(
                        "រូបភាពស្ទូឌីយោ LUMO៖",
                        "LUMO Studio Covers:"
                    ),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.primaryText
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(studioCoverPresets, key = { "preset_${it.resName}" }) { preset ->
                        val isSelected = playlist.coverUrl.isBlank() && playlist.coverResName == preset.resName
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) accentColor else palette.divider,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    onSelectCustomCover(preset.resName, "")
                                    onDismiss()
                                }
                        ) {
                            LumoArtworkImage(
                                artworkUrl = "",
                                fallbackResId = resolveArtworkRes(preset.resName),
                                contentDescription = preset.label,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = customUrlInput,
                    onValueChange = { customUrlInput = it },
                    label = {
                        Text(language.tr("ឬបិទភ្ជាប់តំណរូបភាព (Image URL)", "Or paste Image URL (https://...)"))
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            if (customUrlInput.trim().startsWith("http")) {
                Button(
                    onClick = {
                        onSelectCustomCover(playlist.coverResName, customUrlInput.trim())
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text(
                        text = language.tr("ប្រើតំណរូបភាព", "Apply URL"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = language.tr("បិទ", "Close"),
                    color = palette.secondaryText
                )
            }
        }
    )
}

