package com.example

import com.example.data.KhmerArtistDirectory
import com.example.data.MediaItemEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun khmerArtistSearch_matchesEnglishAndKhmerScriptAndExpandsQueries() {
        val vanndaByEnglish = KhmerArtistDirectory.findMatchingArtists("vannda")
        assertTrue(vanndaByEnglish.any { it.englishName == "VannDa" })

        val vanndaByKhmer = KhmerArtistDirectory.findMatchingArtists("វណ្ណដា")
        assertTrue(vanndaByKhmer.any { it.englishName == "VannDa" })

        val genericKhmer = KhmerArtistDirectory.findMatchingArtists("khmer artist")
        assertTrue(genericKhmer.size >= 10)

        val expandedQueries = KhmerArtistDirectory.expandSearchQueries("វណ្ណដា")
        assertTrue(expandedQueries.contains("VannDa"))
        assertTrue(expandedQueries.contains("វណ្ណដា"))
    }

    @Test
    fun khmerArtistAttribution_normalizesInvertedAndUploaderTitles() {
        val (artist1, title1) = KhmerArtistDirectory.normalizeTrackArtistAndTitle(
            rawTitle = "Time To Rise - VannDa feat. Master Kong Nay",
            uploaderUsername = "Baramey Official",
            queryHint = "VannDa"
        )
        assertEquals("VannDa (វណ្ណដា)", artist1)
        assertEquals("Time To Rise", title1)

        val sampleTrack = MediaItemEntity(
            id = "sc_test_1",
            title = "Time To Rise",
            artist = "VannDa (វណ្ណដា)",
            album = "Skull 2",
            durationSec = 240,
            streamUrl = "https://example.com/audio.mp3",
            category = "Khmer Music",
            isVideo = false,
            releaseYear = 2022
        )
        assertTrue(KhmerArtistDirectory.matchesMediaItem(sampleTrack, "វណ្ណដា"))
        assertTrue(KhmerArtistDirectory.matchesMediaItem(sampleTrack, "vannda"))
        assertTrue(KhmerArtistDirectory.matchesMediaItem(sampleTrack, "khmer artist"))
    }
}

