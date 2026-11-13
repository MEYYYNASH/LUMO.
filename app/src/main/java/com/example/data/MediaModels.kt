package com.example.data

data class MediaItem(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSec: Int,
    val isVideo: Boolean,
    val artworkUrl: String,
    val accentHex: Long,
    val streamUrl: String,
    val licenseBadge: String,
    val category: String,
    val releaseYear: Int,
    val bitrateKbps: Int,
    var isFavorite: Boolean = false,
    var inLibrary: Boolean = false,
    val popularityScore: Long = 0L,
    val waveformSeed: Int = 42
)

data class Playlist(
    val id: String,
    val name: String,
    val description: String,
    val coverUrl: String,
    val trackIds: List<String>
)

data class DownloadTask(
    val id: String,
    val title: String,
    val artist: String,
    val format: String,
    val status: String
)

data class LumoSettings(
    val themeId: String = "OBSIDIAN",
    val appLanguage: String = "BILINGUAL",
    val defaultQuality: String = "High 320 kbps AAC"
)

data class KhmerArtistProfile(
    val id: String,
    val englishName: String,
    val khmerName: String,
    val genreBadge: String,
    val imageUrl: String,
    val popularSongs: List<String>
)
