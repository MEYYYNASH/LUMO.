package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.data.*

class LumoViewModel : ViewModel() {
    var mediaItems by mutableStateOf(KhmerArtistDirectory.initialCatalog)
        private set

    var playlists by mutableStateOf(
        listOf(
            Playlist(
                id = "pl_1",
                name = "Trending Today & 2026",
                description = "Ranked by real worldwide plays with Jamie Miller & top hits.",
                coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?q=80&w=600&auto=format&fit=crop",
                trackIds = listOf("track_babydoll", "track_1", "track_2", "track_3")
            )
        )
    )
        private set

    var downloads by mutableStateOf(listOf<DownloadTask>())
        private set

    var settings by mutableStateOf(LumoSettings())
        private set

    var currentTab by mutableStateOf("home")
    var selectedArtist by mutableStateOf<KhmerArtistProfile?>(null)

    var currentTrack by mutableStateOf<MediaItem?>(null)
    var isPlaying by mutableStateOf(false)
    var currentSec by mutableStateOf(0)
    var isPlayerModalOpen by mutableStateOf(false)

    fun playTrack(track: MediaItem) {
        currentTrack = track
        currentSec = 0
        isPlaying = true
    }

    fun togglePlay() {
        isPlaying = !isPlaying
    }

    fun toggleFavorite(trackId: String) {
        mediaItems = mediaItems.map {
            if (it.id == trackId) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    fun updateSettings(newSettings: LumoSettings) {
        settings = newSettings
    }
}
