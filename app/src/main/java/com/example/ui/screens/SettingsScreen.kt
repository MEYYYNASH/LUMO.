package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.DownloadTaskEntity
import com.example.data.LumoSettingsEntity
import com.example.ui.theme.ColorReactiveIntensity
import com.example.ui.theme.LocalColorReactiveState
import com.example.ui.theme.LocalLumoLanguage
import com.example.ui.theme.LocalLumoPalette
import com.example.ui.theme.LumoIcons
import com.example.ui.theme.LumoLanguage
import com.example.ui.theme.LumoThemeOption
import com.example.ui.theme.paletteForTheme

@Composable
fun SettingsScreen(
    settings: LumoSettingsEntity,
    completedDownloads: List<DownloadTaskEntity>,
    onBack: () -> Unit,
    onSelectTheme: (LumoThemeOption) -> Unit,
    onUpdateSettings: ((LumoSettingsEntity) -> LumoSettingsEntity) -> Unit,
    onManageClearStorage: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val palette = LocalLumoPalette.current
    val reactive = LocalColorReactiveState.current
    val language = LocalLumoLanguage.current

    val audioDownloadedBytes = completedDownloads.filter { !it.isVideo }.sumOf { it.totalBytes }
    val videoDownloadedBytes = completedDownloads.filter { it.isVideo }.sumOf { it.totalBytes }
    val musicMb = audioDownloadedBytes.toDouble() / (1024.0 * 1024.0)
    val videosMb = videoDownloadedBytes.toDouble() / (1024.0 * 1024.0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .testTag("settings_screen_list"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_button")
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
                        text = language.tr("ការកំណត់", "Settings", "ការកំណត់ · Settings"),
                        style = MaterialTheme.typography.headlineLarge,
                        color = palette.primaryText
                    )
                    Text(
                        text = language.tr(
                            "ភាសាខ្មែរ រូបរាង ការចាក់ភ្លេង និងការទាញយក",
                            "Language, Appearance, Color Reactive Engine, playback & storage"
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.secondaryText
                    )
                }
            }
        }

        item {
            SettingsSectionCard(
                title = language.tr(
                    "ភាសាកម្មវិធី · App Language (ខ្មែរ / EN)",
                    "App Language · ភាសាខ្មែរ & English"
                )
            ) {
                Text(
                    text = language.tr(
                        "ជ្រើសរើសភាសាខ្មែរពេញលេញ ពីរភាសា ឬអង់គ្លេស សម្រាប់កម្មវិធី LUMO",
                        "Choose Full Khmer, Bilingual (Khmer + English), or English interface"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.secondaryText
                )
                Spacer(modifier = Modifier.height(10.dp))

                LumoLanguage.entries.forEach { langOpt ->
                    val isSelected = settings.languageOption == langOpt
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) palette.primaryAccent.copy(alpha = 0.16f)
                                else palette.elevatedSurface
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) palette.primaryAccent else palette.divider,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                onUpdateSettings { it.copy(appLanguage = langOpt.id) }
                            }
                            .padding(horizontal = 14.dp, vertical = 11.dp)
                            .testTag("language_option_${langOpt.id}")
                    ) {
                        Icon(
                            imageVector = if (isSelected) LumoIcons.RadioButtonChecked else LumoIcons.RadioButtonUnchecked,
                            contentDescription = langOpt.displayName,
                            tint = if (isSelected) palette.primaryAccent else palette.secondaryText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = langOpt.displayName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = palette.primaryText
                            )
                            Text(
                                text = langOpt.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.secondaryText
                            )
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(palette.primaryAccent.copy(alpha = 0.16f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = langOpt.badgeLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = palette.primaryAccent
                            )
                        }
                    }
                }
            }
        }

        item {
            SettingsSectionCard(title = "Color Reactive Engine — LUMO") {
                Text(
                    text = "Real-time artwork palette extraction & audio-reactive atmosphere (Bass, Beat & Energy)",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.secondaryText
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Live extracted artwork palette preview row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(palette.elevatedSurface)
                        .border(1.dp, reactive.reactiveAccentColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Live Artwork Identity",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = palette.primaryText
                        )
                        Text(
                            text = "Dominant · Secondary · Reactive Accent",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.secondaryText
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(reactive.dominantColor)
                                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(reactive.secondaryColor)
                                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(reactive.reactiveAccentColor)
                                .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ColorReactiveIntensity.entries.forEach { mode ->
                    val isSelected = settings.reactiveIntensity == mode
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) reactive.reactiveAccentColor.copy(alpha = 0.16f)
                                else palette.elevatedSurface
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) reactive.reactiveAccentColor else palette.divider,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                onUpdateSettings { it.copy(colorReactiveIntensity = mode.id) }
                            }
                            .padding(horizontal = 14.dp, vertical = 11.dp)
                            .testTag("reactive_intensity_${mode.id}")
                    ) {
                        Icon(
                            imageVector = if (isSelected) LumoIcons.RadioButtonChecked else LumoIcons.RadioButtonUnchecked,
                            contentDescription = mode.label,
                            tint = if (isSelected) reactive.reactiveAccentColor else palette.secondaryText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${mode.label} Intensity",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = palette.primaryText
                            )
                            Text(
                                text = mode.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.secondaryText
                            )
                        }
                    }
                }
            }
        }

        item {
            SettingsSectionCard(title = "Appearance · Five Themes") {
                LumoThemeOption.entries.forEach { themeOpt ->
                    val isSelected = settings.themeOption == themeOpt
                    val previewPalette = paletteForTheme(themeOpt)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) palette.primaryAccent.copy(alpha = 0.14f)
                                else palette.elevatedSurface
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) palette.primaryAccent else palette.divider,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelectTheme(themeOpt) }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("theme_option_${themeOpt.id}")
                    ) {
                        Icon(
                            imageVector = if (isSelected) LumoIcons.RadioButtonChecked else LumoIcons.RadioButtonUnchecked,
                            contentDescription = themeOpt.displayName,
                            tint = if (isSelected) palette.primaryAccent else palette.secondaryText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${themeOpt.numberLabel} — ${themeOpt.displayName}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = palette.primaryText
                            )
                            Text(
                                text = themeOpt.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.secondaryText
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(previewPalette.background)
                                    .border(1.dp, palette.divider, CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(previewPalette.primaryAccent)
                            )
                        }
                    }
                }
            }
        }

        item {
            SettingsSectionCard(title = "Playback") {
                SettingsToggleRow(
                    title = "Background playback",
                    subtitle = "Keep audio playing when screen is off or switching apps",
                    checked = settings.backgroundPlayback,
                    onCheckedChange = { v -> onUpdateSettings { it.copy(backgroundPlayback = v) } }
                )
                SettingsToggleRow(
                    title = "Autoplay",
                    subtitle = "Automatically play similar tracks when queue ends",
                    checked = settings.autoplay,
                    onCheckedChange = { v -> onUpdateSettings { it.copy(autoplay = v) } }
                )
                SettingsCycleRow(
                    title = "Default quality",
                    value = settings.defaultQuality,
                    onClick = {
                        val list = listOf("High 256 kbps AAC", "Lossless 320 kbps", "Balanced 160 kbps")
                        val next = list[(list.indexOf(settings.defaultQuality) + 1) % list.size]
                        onUpdateSettings { it.copy(defaultQuality = next) }
                    }
                )
                SettingsCycleRow(
                    title = "Playback speed",
                    value = "${settings.playbackSpeed}x",
                    onClick = {
                        val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                        val next = speeds[(speeds.indexOf(settings.playbackSpeed) + 1) % speeds.size]
                        onUpdateSettings { it.copy(playbackSpeed = next) }
                    }
                )
                SettingsCycleRow(
                    title = "Repeat behavior",
                    value = settings.repeatMode,
                    onClick = {
                        val modes = listOf("ALL", "ONE", "OFF")
                        val next = modes[(modes.indexOf(settings.repeatMode) + 1) % modes.size]
                        onUpdateSettings { it.copy(repeatMode = next) }
                    }
                )
                SettingsToggleRow(
                    title = "Gapless playback",
                    subtitle = "Seamless transitions between album tracks",
                    checked = settings.gaplessPlayback,
                    onCheckedChange = { v -> onUpdateSettings { it.copy(gaplessPlayback = v) } }
                )
            }
        }

        item {
            SettingsSectionCard(title = "Downloads") {
                SettingsCycleRow(
                    title = "Default format",
                    value = settings.defaultDownloadFormat,
                    onClick = {
                        val formats = listOf("M4A", "MP3", "MP4 720p", "MP4 1080p")
                        val next = formats[(formats.indexOf(settings.defaultDownloadFormat) + 1) % formats.size]
                        onUpdateSettings { it.copy(defaultDownloadFormat = next) }
                    }
                )
                SettingsCycleRow(
                    title = "Default quality",
                    value = settings.defaultDownloadQuality,
                    onClick = {
                        val quals = listOf("High (256 kbps / 720p)", "Studio (320 kbps / 1080p)", "Data Saver (360p)")
                        val next = quals[(quals.indexOf(settings.defaultDownloadQuality) + 1) % quals.size]
                        onUpdateSettings { it.copy(defaultDownloadQuality = next) }
                    }
                )
                SettingsToggleRow(
                    title = "Wi-Fi only",
                    subtitle = "Pause large media downloads on cellular networks",
                    checked = settings.wifiOnlyDownloads,
                    onCheckedChange = { v -> onUpdateSettings { it.copy(wifiOnlyDownloads = v) } }
                )
                SettingsCycleRow(
                    title = "Download location",
                    value = settings.downloadLocation,
                    onClick = {
                        val locs = listOf("/Internal Storage/LUMO/Media", "/SD Card/LUMO/Offline")
                        val next = locs[(locs.indexOf(settings.downloadLocation) + 1) % locs.size]
                        onUpdateSettings { it.copy(downloadLocation = next) }
                    }
                )
                SettingsCycleRow(
                    title = "Simultaneous downloads",
                    value = "${settings.simultaneousDownloads} active tasks",
                    onClick = {
                        val next = if (settings.simultaneousDownloads >= 5) 1 else settings.simultaneousDownloads + 1
                        onUpdateSettings { it.copy(simultaneousDownloads = next) }
                    }
                )
            }
        }

        item {
            SettingsSectionCard(title = "Storage") {
                StorageStatRow(
                    label = "Music",
                    valueText = String.format(java.util.Locale.US, "%.1f MB", musicMb),
                    dotColor = palette.homeAccent
                )
                StorageStatRow(
                    label = "Videos",
                    valueText = String.format(java.util.Locale.US, "%.1f MB", videosMb),
                    dotColor = palette.searchAccent
                )
                StorageStatRow(
                    label = "Other (Artwork Cache)",
                    valueText = "12.4 MB",
                    dotColor = palette.downloadsAccent
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(CircleShape)
                        .background(palette.divider)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(0.45f)
                            .fillMaxHeight()
                            .background(palette.homeAccent)
                    )
                    Box(
                        modifier = Modifier
                            .weight(0.25f)
                            .fillMaxHeight()
                            .background(palette.searchAccent)
                    )
                    Box(
                        modifier = Modifier
                            .weight(0.10f)
                            .fillMaxHeight()
                            .background(palette.downloadsAccent)
                    )
                    Spacer(modifier = Modifier.weight(0.20f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onManageClearStorage,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manage_storage_button")
                ) {
                    Text(
                        text = "Manage Storage (${completedDownloads.size} offline files)",
                        color = palette.primaryText,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            val newReleasesNotif = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }
            val downloadCompleteNotif = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }
            val recommendationNotif = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

            SettingsSectionCard(title = "Notifications & Privacy") {
                SettingsToggleRow(
                    title = "Download Completion Alerts",
                    subtitle = "Notify when authorized MP3 or MP4 saves finish",
                    checked = downloadCompleteNotif.value,
                    onCheckedChange = { downloadCompleteNotif.value = it }
                )
                SettingsToggleRow(
                    title = "New Artist Releases",
                    subtitle = "Alerts for followed artists and albums",
                    checked = newReleasesNotif.value,
                    onCheckedChange = { newReleasesNotif.value = it }
                )
                SettingsToggleRow(
                    title = "Personalized Mix Updates",
                    subtitle = "Weekly LUMO Mix and Daily Mix refresh notifications",
                    checked = recommendationNotif.value,
                    onCheckedChange = { recommendationNotif.value = it }
                )
                HorizontalDivider(color = palette.divider.copy(alpha = 0.5f))
                SettingsCycleRow(
                    title = "Listening History Privacy",
                    value = "On-Device Private",
                    onClick = {}
                )
            }
        }

        item {
            SettingsSectionCard(title = "About LUMO") {
                LumoWordmarkLogo(
                    accentColor = reactive.reactiveAccentColor,
                    textColor = palette.primaryText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Discover. Play. Save.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.secondaryText
                )
                Spacer(modifier = Modifier.height(12.dp))
                SettingsCycleRow(
                    title = "Version",
                    value = "LUMO 2.5.0 Premium",
                    onClick = {}
                )
                SettingsCycleRow(
                    title = "Privacy Policy",
                    value = "Zero Tracking · Local Storage",
                    onClick = {}
                )
                SettingsCycleRow(
                    title = "Terms & Media Policy",
                    value = "Authorized Media Only",
                    onClick = {}
                )
                SettingsCycleRow(
                    title = "Open Source Licenses",
                    value = "Media3 · Room · Compose",
                    onClick = {}
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    val palette = LocalLumoPalette.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(palette.surface)
            .border(1.dp, palette.divider, RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = palette.primaryText
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val palette = LocalLumoPalette.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = palette.primaryText
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = palette.secondaryText
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = palette.primaryAccent
            )
        )
    }
}

@Composable
private fun SettingsCycleRow(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    val palette = LocalLumoPalette.current
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = palette.primaryText
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = palette.primaryAccent
            )
        }
        HorizontalDivider(color = palette.divider.copy(alpha = 0.5f))
    }
}

@Composable
private fun StorageStatRow(
    label: String,
    valueText: String,
    dotColor: Color
) {
    val palette = LocalLumoPalette.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = palette.primaryText
            )
        }
        Text(
            text = valueText,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = palette.primaryText
        )
    }
}
