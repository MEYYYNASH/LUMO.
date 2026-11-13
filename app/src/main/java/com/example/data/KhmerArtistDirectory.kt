package com.example.data

object KhmerArtistDirectory {
    val featuredArtists = listOf(
        KhmerArtistProfile(
            id = "khmer_vannda",
            englishName = "VannDa",
            khmerName = "វណ្ណដា",
            genreBadge = "Khmer Hip-Hop & Rap",
            imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=600&auto=format&fit=crop",
            popularSongs = listOf("Time To Rise", "J+O", "Solo", "Mama")
        ),
        KhmerArtistProfile(
            id = "khmer_tena",
            englishName = "Tena",
            khmerName = "ថេណា",
            genreBadge = "Khmer R&B & Indie Pop",
            imageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?q=80&w=600&auto=format&fit=crop",
            popularSongs = listOf("Khmeanalan", "Love Is Singular", "First Lady")
        ),
        KhmerArtistProfile(
            id = "khmer_suly_pheng",
            englishName = "Suly Pheng",
            khmerName = "ស៊ូលីផេង",
            genreBadge = "Khmer Acoustic & Soul",
            imageUrl = "https://images.unsplash.com/photo-1516280440614-37939bbacd81?q=80&w=600&auto=format&fit=crop",
            popularSongs = listOf("Phka Proheus", "10K", "Yesterday")
        ),
        KhmerArtistProfile(
            id = "khmer_g_devith",
            englishName = "G-Devith",
            khmerName = "ជី ដេវីត",
            genreBadge = "Khmer Hip-Hop & Rap",
            imageUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?q=80&w=600&auto=format&fit=crop",
            popularSongs = listOf("Body", "Bodyguard", "No Smoke")
        )
    )

    val initialCatalog = listOf(
        MediaItem(
            id = "track_babydoll",
            title = "Babydoll",
            artist = "Jamie Miller",
            album = "Trending Today 2026",
            durationSec = 198,
            isVideo = false,
            artworkUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?q=80&w=800&auto=format&fit=crop",
            accentHex = 0xFF007AFF,
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            licenseBadge = "Trending Today · 320 kbps AAC",
            category = "Trending Today",
            releaseYear = 2026,
            bitrateKbps = 320,
            isFavorite = true,
            inLibrary = true,
            popularityScore = 285000000
        ),
        MediaItem(
            id = "track_1",
            title = "Time To Rise (feat. Master Kong Nay)",
            artist = "VannDa (វណ្ណដា)",
            album = "SKULL 2 (2026 Edition)",
            durationSec = 248,
            isVideo = false,
            artworkUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=800&auto=format&fit=crop",
            accentHex = 0xFF007AFF,
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            licenseBadge = "Trending Today · 320 kbps AAC",
            category = "Trending Today",
            releaseYear = 2026,
            bitrateKbps = 320,
            isFavorite = true,
            inLibrary = true,
            popularityScore = 185000000
        ),
        MediaItem(
            id = "track_2",
            title = "Phka Proheus (ផ្កាព្រហ្មសិលា)",
            artist = "Suly Pheng (ស៊ូលីផេង)",
            album = "Acoustic Sessions 2026",
            durationSec = 212,
            isVideo = false,
            artworkUrl = "https://images.unsplash.com/photo-1516280440614-37939bbacd81?q=80&w=800&auto=format&fit=crop",
            accentHex = 0xFFF59E0B,
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            licenseBadge = "Trending Today · FLAC",
            category = "Trending Today",
            releaseYear = 2026,
            bitrateKbps = 320,
            isFavorite = true,
            inLibrary = true,
            popularityScore = 45000000
        ),
        MediaItem(
            id = "track_3",
            title = "Khmeanalan (គ្មានអាល័យ)",
            artist = "Tena (ថេណា)",
            album = "Indie R&B Club",
            durationSec = 195,
            isVideo = false,
            artworkUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?q=80&w=800&auto=format&fit=crop",
            accentHex = 0xFFFF3B78,
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            licenseBadge = "Trending Today · M4A",
            category = "Trending Today",
            releaseYear = 2026,
            bitrateKbps = 256,
            isFavorite = false,
            inLibrary = true,
            popularityScore = 32000000
        )
    )
}
