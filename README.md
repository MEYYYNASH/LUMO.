# LUMO — Music & Video Player (Android)

LUMO is a modern, high-performance Android music and video streaming application built with **Jetpack Compose**, **Kotlin Coroutines**, **Room Database**, and **Media3 / ExoPlayer**.

---

## 📥 Direct APK Download

You can download and install the ready-to-run APK directly from this repository:
- **Direct file:** [`LUMO.apk`](./LUMO.apk) (tap to download and install on Android)

---

## 🚀 Key Features

- **Trending Today & Trending 2026:**
  - Real-time global listening charts powered by Piped Open YouTube, Apple Music / iTunes Charts, and SoundCloud.
  - Strict filtering displaying only **Trending Today** and **Trending 2026** hits.
- **Universal Sound & Link Finder:**
  - Search any track, lyric, SFX, viral audio, or remix.
  - Built-in Voice / Audio Recognition (Microphone button in search).
  - Multi-platform link resolver: paste links from YouTube, TikTok, SoundCloud, Spotify, Apple Music, Deezer, or direct audio streams (`.mp3`, `.wav`, `.m4a`).
- **Resilient Multi-Source Streaming:**
  - Automatic stream fallback resolution so playback never stutters or drops.
  - Background audio playback with Android Media notification and lock screen controls via MediaSession.
- **Video & Music Player:**
  - Full-screen video mode, floating mini-player, background playback, and waveform scrubber.
- **Offline Downloads & Library:**
  - Download tracks for offline playback with instant resume and local storage management.
- **Modern Jetpack Compose UI:**
  - Material 3 dynamic color theming, smooth animations, bilingual support (Khmer & English).

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Audio/Video Engine:** AndroidX Media3 (ExoPlayer) & MediaSessionService
- **Local Persistence:** Room Database
- **Networking:** Ktor / Retrofit & Coroutines Flow
- **Image Loading:** Coil Compose
- **Build System:** Gradle (Kotlin DSL) with Version Catalog

---

## 📦 Building from Source

1. Clone this repository:
   ```bash
   git clone https://github.com/MEYYYNASH/LUMO.git
   ```
2. Open the project in **Android Studio** (Ladybug or newer).
3. Let Gradle sync dependencies.
4. Run on a connected device or emulator with `assembleDebug`.
