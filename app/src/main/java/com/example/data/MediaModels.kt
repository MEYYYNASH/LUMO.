package com.example.data

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.R
import com.example.ui.theme.LumoThemeOption
import kotlin.math.abs
import kotlin.math.sin

@Entity(tableName = "media_items")
data class MediaItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSec: Int,
    val isVideo: Boolean,
    val artworkUrl: String = "",
    val artworkResName: String = "img_lumo_icon_fg",
    val accentHex: Long = 0xFF3B82F6,
    val streamUrl: String,
    val localFilePath: String = "",
    val licenseBadge: String = "Authorized Media Stream",
    val isAuthorizedDownload: Boolean = true,
    val category: String = "Music",
    val releaseYear: Int = 2026,
    val bitrateKbps: Int = 256,
    val isFavorite: Boolean = false,
    val inLibrary: Boolean = false,
    val lastPlayedTimestamp: Long = 0L,
    val lastPositionSec: Int = 0,
    val playCount: Int = 0,
    val popularityScore: Long = 0L,
    val waveformSeed: Int = 42,
    val subtitleLinesPipeSeparated: String = ""
) {
    @get:DrawableRes
    val artworkResId: Int
        get() = resolveArtworkRes(artworkResName)

    val formattedDuration: String
        get() = formatSeconds(durationSec)

    val formattedPopularity: String
        get() {
            val globalPlays = popularityScore
            return when {
                globalPlays >= 1_000_000_000L -> String.format(java.util.Locale.US, "%.2fB global plays", globalPlays / 1_000_000_000.0)
                globalPlays >= 1_000_000L -> String.format(java.util.Locale.US, "%.1fM global plays", globalPlays / 1_000_000.0)
                globalPlays >= 1_000L -> String.format(java.util.Locale.US, "%dK global plays", globalPlays / 1_000L)
                globalPlays > 0L -> "$globalPlays global plays"
                else -> "Global Chart"
            }
        }

    val progressFraction: Float
        get() = if (durationSec > 0) (lastPositionSec.toFloat() / durationSec.toFloat()).coerceIn(0f, 1f) else 0f

    val effectivePlayableUrlOrPath: String
        get() {
            val localFile = if (localFilePath.isNotBlank()) java.io.File(localFilePath) else null
            if (localFile != null && localFile.exists() && localFile.length() > 1024L) {
                return localFile.absolutePath
            }
            if (streamUrl.contains("invidious.f5.si", ignoreCase = true) ||
                streamUrl.contains("api-v2.soundcloud.com", ignoreCase = true)
            ) {
                return ""
            }
            return streamUrl
        }

    val effectiveVideoStreamUrlOrPath: String
        get() {
            val localFile = if (localFilePath.isNotBlank()) java.io.File(localFilePath) else null
            if (localFile != null && localFile.exists() && localFile.length() > 1024L) {
                return localFile.absolutePath
            }
            if (streamUrl.contains("invidious.f5.si", ignoreCase = true) ||
                streamUrl.contains("api-v2.soundcloud.com", ignoreCase = true)
            ) {
                return ""
            }
            return streamUrl
        }

    fun generateWaveform(barsCount: Int = 44): List<Float> {
        val seed = if (waveformSeed != 0) waveformSeed else abs(id.hashCode() % 997) + 11
        return List(barsCount) { index ->
            val phase = (index + 1) * 0.43f + seed * 0.71f
            val primary = abs(sin(phase)) * 0.62f
            val harmonic = abs(sin(phase * 2.3f + seed)) * 0.28f
            (0.16f + primary + harmonic).coerceIn(0.16f, 1.0f)
        }
    }

    fun parsedSubtitles(): List<SubtitleCue> {
        if (subtitleLinesPipeSeparated.isBlank()) return emptyList()
        return subtitleLinesPipeSeparated.split("|").mapNotNull { entry ->
            val parts = entry.split("::", limit = 3)
            if (parts.size == 3) {
                val start = parts[0].toIntOrNull() ?: 0
                val end = parts[1].toIntOrNull() ?: (start + 15)
                SubtitleCue(startSec = start, endSec = end, text = parts[2])
            } else null
        }
    }
}

data class SubtitleCue(
    val startSec: Int,
    val endSec: Int,
    val text: String
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val description: String,
    val coverUrl: String = "",
    val coverResName: String = "img_lumo_icon_fg",
    val accentHex: Long = 0xFF3B82F6,
    val trackIdsCsv: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val trackIds: List<String>
        get() = if (trackIdsCsv.isBlank()) emptyList()
        else trackIdsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    @get:DrawableRes
    val coverResId: Int
        get() = resolveArtworkRes(coverResName)
}

@Entity(tableName = "downloads")
data class DownloadTaskEntity(
    @PrimaryKey val id: String,
    val mediaId: String,
    val title: String,
    val artist: String,
    val artworkUrl: String = "",
    val artworkResName: String = "img_lumo_icon_fg",
    val format: String,
    val isVideo: Boolean,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val progressPercent: Int,
    val status: String, // "DOWNLOADING", "PAUSED", "COMPLETED", "FAILED"
    val remoteUrl: String = "",
    val localFilePath: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    @get:DrawableRes
    val artworkResId: Int
        get() = resolveArtworkRes(artworkResName)

    val formattedSizeMb: String
        get() {
            val bytesToShow = if (totalBytes > 0L) totalBytes else downloadedBytes
            if (bytesToShow <= 0L) return "Stream"
            val mb = bytesToShow.toDouble() / (1024.0 * 1024.0)
            return String.format(java.util.Locale.US, "%.1f MB", mb)
        }
}

@Entity(tableName = "lumo_settings")
data class LumoSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val themeId: String = LumoThemeOption.OBSIDIAN.id,
    val appLanguage: String = "KM", // "KM", "BILINGUAL", "EN"
    val colorReactiveIntensity: String = "MEDIUM", // "LOW", "MEDIUM", "HIGH"
    val backgroundPlayback: Boolean = true,
    val autoplay: Boolean = true,
    val defaultQuality: String = "High 320 kbps AAC",
    val playbackSpeed: Float = 1.0f,
    val repeatMode: String = "ALL", // "OFF", "ALL", "ONE"
    val gaplessPlayback: Boolean = true,
    val defaultDownloadFormat: String = "MP3",
    val defaultDownloadQuality: String = "High (320 kbps / 1080p)",
    val wifiOnlyDownloads: Boolean = true,
    val downloadLocation: String = "/Internal Storage/LUMO/Media",
    val simultaneousDownloads: Int = 3
) {
    val themeOption: LumoThemeOption
        get() = LumoThemeOption.fromId(themeId)

    val languageOption: com.example.ui.theme.LumoLanguage
        get() = com.example.ui.theme.LumoLanguage.fromId(appLanguage)

    val reactiveIntensity: com.example.ui.theme.ColorReactiveIntensity
        get() = com.example.ui.theme.ColorReactiveIntensity.fromId(colorReactiveIntensity)
}

enum class DownloadFormatOption(
    val id: String,
    val label: String,
    val subLabel: String,
    val isVideo: Boolean,
    val fileExtension: String
) {
    MP3("MP3", "MP3", "Audio Stream Container (.mp3)", false, "mp3"),
    MP3_320("MP3", "MP3", "320 kbps High-Quality Audio (.mp3)", false, "mp3"),
    M4A("M4A", "M4A", "256 kbps AAC Audio (.m4a)", false, "m4a"),
    MP4_360P("MP4 360p", "MP4 360p", "Compact Video (.mp4)", true, "mp4"),
    MP4_720P("MP4 720p", "MP4 720p", "HD Video Stream (.mp4)", true, "mp4"),
    MP4_1080P("MP4 1080p", "MP4 1080p", "Full HD Video Stream (.mp4)", true, "mp4")
}

@DrawableRes
fun resolveArtworkRes(name: String): Int = when (name) {
    "img_album_neon_echoes" -> R.drawable.img_album_neon_echoes_1791694225919
    "img_album_velvet_drift" -> R.drawable.img_album_velvet_drift_1791694241269
    "img_album_solar_pulse" -> R.drawable.img_album_solar_pulse_1791694252661
    "img_video_aurora_session" -> R.drawable.img_video_aurora_session_1791694262520
    else -> R.drawable.img_album_neon_echoes_1791694225919
}

fun formatSeconds(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    val mins = safe / 60
    val secs = safe % 60
    return String.format(java.util.Locale.US, "%d:%02d", mins, secs)
}
