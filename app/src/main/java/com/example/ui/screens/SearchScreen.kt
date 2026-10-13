package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.DownloadFormatOption
import com.example.data.KhmerArtistDirectory
import com.example.data.KhmerArtistProfile
import com.example.data.MediaItemEntity
import com.example.ui.SearchCategory
import com.example.ui.components.LumoArtworkImage
import com.example.ui.theme.LocalLumoLanguage
import com.example.ui.theme.LocalLumoPalette
import com.example.ui.theme.LumoIcons

private data class SampleYouTubeLink(
    val label: String,
    val url: String
)

private val SAMPLE_YOUTUBE_LINKS = listOf(
    SampleYouTubeLink("VannDa - Time To Rise (វណ្ណដា)", "https://www.youtube.com/watch?v=rvje5oblrLw"),
    SampleYouTubeLink("The Weeknd - Blinding Lights", "https://www.youtube.com/watch?v=4NRXx6U8ABQ"),
    SampleYouTubeLink("Daft Punk - One More Time", "https://www.youtube.com/watch?v=FGBhQbmPwH8"),
    SampleYouTubeLink("Coldplay - Viva La Vida", "https://www.youtube.com/watch?v=dvgZkm1xWPE")
)

private val KHMER_QUICK_GENRE_CHIPS = listOf(
    "បទថ្មី 2026" to "បទថ្មី 2026 · New Khmer",
    "បទសេដ" to "បទសេដ · Sad Song",
    "បទញាក់" to "បទញាក់ · Remix & DJ",
    "TikTok Viral Sound 2026" to "🔥 TikTok Viral Sound",
    "Sound Effect Audio" to "🔊 Sound Effects / SFX",
    "ស្នេហា" to "ស្នេហា · Love",
    "រាំវង់" to "រាំវង់ · Romvong",
    "Instrumental Beat" to "🎹 Instrumental & Beats"
)

@Composable
fun SearchScreen(
    allMedia: List<MediaItemEntity>,
    worldTrendingMedia: List<MediaItemEntity> = emptyList(),
    onlineSearchResults: List<MediaItemEntity> = emptyList(),
    currentPlayingMediaId: String?,
    searchQuery: String,
    isSearchingOnline: Boolean,
    isLoadingMoreSearch: Boolean = false,
    selectedCategory: SearchCategory,
    isLinkDownloaderMode: Boolean,
    youtubeLinkInput: String,
    isResolvingYouTubeLink: Boolean,
    resolvedYouTubeMedia: MediaItemEntity?,
    onSetLinkDownloaderMode: (Boolean) -> Unit,
    onYouTubeLinkInputChange: (String) -> Unit,
    onResolveYouTubeLink: (String, DownloadFormatOption?) -> Unit,
    onQueryChange: (String) -> Unit,
    onLoadMoreOnlineSearch: () -> Unit = {},
    onSelectCategory: (SearchCategory) -> Unit,
    onPlayMedia: (MediaItemEntity, Boolean) -> Unit,
    onQuickDownloadFormat: (MediaItemEntity, DownloadFormatOption) -> Unit,
    onOpenDownloadSheet: (MediaItemEntity) -> Unit,
    onOpenTrackOptions: (MediaItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalLumoPalette.current
    val language = LocalLumoLanguage.current
    val context = LocalContext.current
    val searchAccent = palette.searchAccent

    var localSearchText by remember { mutableStateOf(searchQuery) }
    var isSoundFinderExpanded by remember { mutableStateOf(false) }
    var soundFinderStatusHint by remember { mutableStateOf("") }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = matches?.firstOrNull()?.trim().orEmpty()
            if (recognizedText.isNotEmpty()) {
                localSearchText = recognizedText
                onSetLinkDownloaderMode(false)
                onQueryChange(recognizedText)
                soundFinderStatusHint = "Found sound match for \"$recognizedText\""
            }
        }
    }

    val launchVoiceSoundFinder: () -> Unit = {
        runCatching {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak song title, lyrics, artist, or sound name…")
            }
            speechRecognizerLauncher.launch(intent)
        }.onFailure {
            isSoundFinderExpanded = true
            soundFinderStatusHint = "Type or paste any sound name, lyric, or link below"
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchVoiceSoundFinder()
        } else {
            isSoundFinderExpanded = true
            soundFinderStatusHint = "Use Universal Sound Finder below to search any sound, lyric, or link"
        }
    }

    val triggerMicSoundFinder: () -> Unit = {
        val hasPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            launchVoiceSoundFinder()
        } else {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(searchQuery) {
        if (localSearchText != searchQuery) {
            localSearchText = searchQuery
        }
    }

    val q = searchQuery.trim()
    val prioritizeSongs = selectedCategory != SearchCategory.VIDEOS

    // Base catalog only recomputes when onlineSearchResults, worldTrendingMedia, or allMedia changes, NEVER on every keystroke
    val baseCatalog = remember(onlineSearchResults, worldTrendingMedia, allMedia) {
        (onlineSearchResults + worldTrendingMedia + allMedia).distinctBy { it.id }
    }

    val filteredResults = remember(
        allMedia,
        worldTrendingMedia,
        onlineSearchResults,
        resolvedYouTubeMedia,
        isLinkDownloaderMode,
        selectedCategory,
        q,
        prioritizeSongs,
        baseCatalog
    ) {
        val onlineIds = onlineSearchResults.mapTo(HashSet()) { it.id }
        val globalTrendingSource = worldTrendingMedia.ifEmpty {
            allMedia.filter { it.popularityScore >= 1_000L }
        }
        val baseSource = when {
            isLinkDownloaderMode && resolvedYouTubeMedia != null -> {
                (listOf(resolvedYouTubeMedia) + baseCatalog).distinctBy { it.id }
            }
            q.isNotEmpty() && onlineSearchResults.isNotEmpty() -> {
                (onlineSearchResults + baseCatalog.filter { KhmerArtistDirectory.matchesMediaItem(it, q) }).distinctBy { it.id }
            }
            selectedCategory == SearchCategory.TRENDING && q.isEmpty() -> {
                globalTrendingSource
            }
            else -> (worldTrendingMedia + allMedia).distinctBy { it.id }
        }

        val rawFilteredResults = baseSource.filter { item ->
            val matchesCategory = when (selectedCategory) {
                SearchCategory.ALL -> true
                SearchCategory.TRENDING -> true
                SearchCategory.SONGS -> !item.isVideo
                SearchCategory.VIDEOS -> item.isVideo
                SearchCategory.ARTISTS -> true
            }
            val matchesQuery = q.isEmpty() ||
                (q.isNotEmpty() && item.id in onlineIds) ||
                q.contains("youtube.com", true) ||
                q.contains("youtu.be", true) ||
                KhmerArtistDirectory.matchesMediaItem(item, q)
            matchesCategory && matchesQuery
        }

        when {
            isLinkDownloaderMode && resolvedYouTubeMedia != null -> {
                val rest = KhmerArtistDirectory.sortPopularSongsFirst(
                    items = rawFilteredResults.filterNot { it.id == resolvedYouTubeMedia.id },
                    queryHint = q,
                    prioritizeSongsBeforeVideos = prioritizeSongs
                )
                listOf(resolvedYouTubeMedia) + rest
            }
            selectedCategory == SearchCategory.TRENDING -> {
                KhmerArtistDirectory.sortTrendingNewFirst(rawFilteredResults)
            }
            else -> {
                KhmerArtistDirectory.sortPopularSongsFirst(
                    items = rawFilteredResults,
                    queryHint = q,
                    prioritizeSongsBeforeVideos = prioritizeSongs
                )
            }
        }
    }

    val matchingKhmerArtists = remember(q) {
        KhmerArtistDirectory.findMatchingArtists(q)
    }

    // Only recompute artist track buckets when baseCatalog changes, using fast substring checks
    val artistTracksByProfileId = remember(baseCatalog) {
        KhmerArtistDirectory.featuredArtists.associate { profile ->
            profile.id to baseCatalog.filter { item ->
                item.artist.contains(profile.englishName, ignoreCase = true) ||
                    item.artist.contains(profile.khmerName) ||
                    item.title.contains(profile.englishName, ignoreCase = true) ||
                    item.title.contains(profile.khmerName)
            }
        }
    }

    val distinctOtherArtists = remember(filteredResults, matchingKhmerArtists) {
        filteredResults
            .groupBy { it.artist }
            .filterKeys { artistName ->
                matchingKhmerArtists.none { profile ->
                    artistName.contains(profile.englishName, ignoreCase = true) ||
                        artistName.contains(profile.khmerName)
                }
            }
            .entries
            .map { (artistName, tracks) ->
                artistName to KhmerArtistDirectory.sortPopularSongsFirst(tracks)
            }
    }

    val solidPillBg = if (palette.option.isDark) Color.White else Color(0xFF0F172A)
    val solidPillText = if (palette.option.isDark) Color(0xFF0B0F17) else Color.White

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("search_screen_list"),
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
                            text = if (isLinkDownloaderMode) {
                                language.tr("ទាញយកគ្រប់សំឡេង & តំណ", "All Sound & Link Downloader", "Sound & Link Downloader")
                            } else {
                                language.tr("ស្វែងរកគ្រប់សំឡេង · All Sound Finder", "All Sound Finder & Search", "ស្វែងរកគ្រប់សំឡេង · Sound Finder")
                            },
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = palette.primaryText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = language.tr(
                                "គាំទ្រគ្រប់សំឡេង៖ បទចម្រៀង · TikTok Sound · DJ Remix · SFX (YouTube · Apple Music · Deezer · SoundCloud)",
                                "Supports all sounds: Songs, TikTok Sounds, Remixes, SFX & Links (YouTube · iTunes · Deezer · SoundCloud)"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.secondaryText
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSoundFinderExpanded) searchAccent
                                else searchAccent.copy(alpha = 0.16f)
                            )
                            .border(
                                width = 1.dp,
                                color = searchAccent.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { isSoundFinderExpanded = !isSoundFinderExpanded }
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                            .testTag("all_sound_finder_toggle_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = LumoIcons.GraphicEq,
                                contentDescription = "All Sound Finder",
                                tint = if (isSoundFinderExpanded) Color(0xFF090D16) else searchAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = language.tr("រកសំឡេង", "Sound Finder"),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSoundFinderExpanded) Color(0xFF090D16) else searchAccent
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = isSoundFinderExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(palette.elevatedSurface)
                            .border(1.dp, searchAccent.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                            .testTag("all_sound_finder_panel")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = language.tr(
                                        "Universal All-Sound Finder · ស្វែងរកគ្រប់សំឡេងទាំងអស់",
                                        "Universal All-Sound Finder · Every Sound Supported"
                                    ),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = palette.primaryText
                                )
                                Text(
                                    text = if (soundFinderStatusHint.isNotBlank()) {
                                        soundFinderStatusHint
                                    } else {
                                        language.tr(
                                            "ស្វែងរកតាមសំឡេងនិយាយ ប៉ារ៉ូលចម្រៀង ឈ្មោះបទ ឬបិទភ្ជាប់តំណ (YouTube, TikTok, SoundCloud, Spotify, MP3)",
                                            "Find any song, TikTok audio, remix, SFX, lyric, or paste any link"
                                        )
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.secondaryText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(searchAccent)
                                    .clickable { triggerMicSoundFinder() }
                                    .testTag("sound_finder_voice_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = LumoIcons.GraphicEq,
                                        contentDescription = "Voice & Sound Mic",
                                        tint = Color(0xFF090D16),
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = language.tr("មេក្រូរកសំឡេង · Mic", "Voice / Sound Mic"),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF090D16)
                                    )
                                }
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(palette.surface)
                                    .border(1.dp, palette.divider, RoundedCornerShape(12.dp))
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        val pasted = clipboard?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()?.trim().orEmpty()
                                        if (pasted.isNotEmpty()) {
                                            localSearchText = pasted
                                            onQueryChange(pasted)
                                            soundFinderStatusHint = "Searching sound: \"$pasted\""
                                        } else {
                                            onSetLinkDownloaderMode(true)
                                        }
                                    }
                                    .testTag("sound_finder_paste_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = LumoIcons.ContentPaste,
                                        contentDescription = "Paste Sound Link or Name",
                                        tint = palette.primaryText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = language.tr("បិទភ្ជាប់សំឡេង/តំណ", "Paste Sound / Link"),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = palette.primaryText
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Signature 2-Pill Mode Switcher (Matching Image 3 right: White Pill vs Dark Elevated Pill)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val searchSelected = !isLinkDownloaderMode
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (searchSelected) solidPillBg else palette.elevatedSurface)
                            .border(
                                width = 1.dp,
                                color = if (searchSelected) solidPillBg else palette.divider,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onSetLinkDownloaderMode(false) }
                            .testTag("mode_search_songs_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = if (searchSelected) solidPillText else palette.primaryText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = language.tr("ស្វែងរកមិនកំណត់", "Search & Download", "ស្វែងរក · Search"),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (searchSelected) solidPillText else palette.primaryText
                            )
                        }
                    }

                    val linkSelected = isLinkDownloaderMode
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (linkSelected) solidPillBg else palette.elevatedSurface)
                            .border(
                                width = 1.dp,
                                color = if (linkSelected) solidPillBg else palette.divider,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onSetLinkDownloaderMode(true) }
                            .testTag("mode_youtube_link_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = LumoIcons.YouTubePlay,
                                contentDescription = null,
                                tint = if (linkSelected) Color(0xFFEF4444) else palette.downloadsAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = language.tr("តំណ YouTube", "YouTube Link", "YouTube Link"),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (linkSelected) solidPillText else palette.primaryText
                            )
                        }
                    }
                }
            }
        }

        // YouTube Link Downloader Card (Visible when in YouTube Link Mode)
        item {
            AnimatedVisibility(visible = isLinkDownloaderMode) {
                val effectiveResolvedMedia = resolvedYouTubeMedia
                    ?: if (youtubeLinkInput.isBlank()) allMedia.firstOrNull() else null
                YouTubeLinkDownloaderSection(
                    youtubeLinkInput = youtubeLinkInput,
                    isResolving = isResolvingYouTubeLink,
                    resolvedMedia = effectiveResolvedMedia,
                    onInputChange = onYouTubeLinkInputChange,
                    onPasteFromClipboard = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val pasted = clipboard?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()?.trim().orEmpty()
                        val effectiveUrl = pasted.ifEmpty { SAMPLE_YOUTUBE_LINKS.first().url }
                        onYouTubeLinkInputChange(effectiveUrl)
                        onResolveYouTubeLink(effectiveUrl, null)
                    },
                    onFetchLink = { url -> onResolveYouTubeLink(url, null) },
                    onDownloadMp4 = { media ->
                        if (youtubeLinkInput.isNotBlank() && resolvedYouTubeMedia == null) {
                            onResolveYouTubeLink(youtubeLinkInput, DownloadFormatOption.MP4_1080P)
                        } else {
                            onQuickDownloadFormat(media, DownloadFormatOption.MP4_1080P)
                        }
                    },
                    onDownloadMp3 = { media ->
                        if (youtubeLinkInput.isNotBlank() && resolvedYouTubeMedia == null) {
                            onResolveYouTubeLink(youtubeLinkInput, DownloadFormatOption.MP3_320)
                        } else {
                            onQuickDownloadFormat(media, DownloadFormatOption.MP3_320)
                        }
                    },
                    onPlayMedia = { media ->
                        onPlayMedia(media, true)
                    }
                )
            }
        }

        // Search Input + Categories + Khmer Artist Strip + Featured Header Card (when in Search & Download Mode)
        if (!isLinkDownloaderMode) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    OutlinedTextField(
                        value = localSearchText,
                        onValueChange = { newText ->
                            localSearchText = newText
                            onQueryChange(newText)
                        },
                        placeholder = {
                            Text(
                                text = language.tr(
                                    "ស្វែងរកគ្រប់បទចម្រៀង មិនកំណត់ (វណ្ណដា, បទសេដ, APT, YouTube…)",
                                    "Search any song without limits (VannDa, វណ្ណដា, APT, links…)"
                                ),
                                color = palette.secondaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = palette.secondaryText
                            )
                        },
                        trailingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                if (isSearchingOnline) {
                                    CircularProgressIndicator(
                                        color = searchAccent,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .padding(end = 4.dp)
                                    )
                                } else if (localSearchText.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            localSearchText = ""
                                            onQueryChange("")
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Clear,
                                            contentDescription = "Clear search",
                                            tint = palette.secondaryText
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { triggerMicSoundFinder() },
                                    modifier = Modifier.testTag("search_bar_sound_finder_mic_button")
                                ) {
                                    Icon(
                                        imageVector = LumoIcons.GraphicEq,
                                        contentDescription = "All Sound Finder Mic",
                                        tint = searchAccent
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = palette.primaryText.copy(alpha = 0.35f),
                            unfocusedBorderColor = palette.divider,
                            focusedContainerColor = palette.surface,
                            unfocusedContainerColor = palette.surface,
                            focusedTextColor = palette.primaryText,
                            unfocusedTextColor = palette.primaryText
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_input_field")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Khmer Mood & Genre Quick Chips (បទថ្មី, បទសេដ, បទញាក់, ស្នេហា, មរតកដើម, រាំវង់)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.testTag("khmer_genre_quick_row")
                    ) {
                        items(KHMER_QUICK_GENRE_CHIPS, key = { "genre_chip_${it.first}" }) { (khmerTerm, displayChip) ->
                            val isActiveChip = q.equals(khmerTerm, ignoreCase = true)
                            Surface(
                                color = if (isActiveChip) searchAccent.copy(alpha = 0.2f) else palette.elevatedSurface,
                                shape = CircleShape,
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = if (isActiveChip) searchAccent else palette.divider,
                                        shape = CircleShape
                                    )
                                    .clickable { onQueryChange(khmerTerm) }
                            ) {
                                Text(
                                    text = displayChip,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isActiveChip) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isActiveChip) searchAccent else palette.primaryText,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.testTag("search_category_row")
                    ) {
                        items(SearchCategory.entries) { category ->
                            val isSelected = category == selectedCategory
                            val bg by animateColorAsState(
                                targetValue = if (isSelected) solidPillBg else palette.surface,
                                label = "searchCatBg"
                            )
                            val textColor by animateColorAsState(
                                targetValue = if (isSelected) solidPillText else palette.secondaryText,
                                label = "searchCatText"
                            )

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(bg)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) solidPillBg else palette.divider,
                                        shape = CircleShape
                                    )
                                    .clickable { onSelectCategory(category) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .testTag("search_category_${category.label.lowercase()}")
                            ) {
                                Text(
                                    text = category.localizedLabel(language),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = textColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Popular Khmer Artists Quick-Select Row (Bilingual English + Khmer Script)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = language.tr("តារាចម្រៀងខ្មែរ · Khmer Artists", "Khmer Artists · តារាចម្រៀងខ្មែរ"),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.primaryText
                        )
                        Text(
                            text = if (selectedCategory == SearchCategory.ARTISTS) {
                                language.tr("កំពុងបង្ហាញតារា", "Showing Artists")
                            } else {
                                language.tr("មើលទាំងអស់ (${KhmerArtistDirectory.featuredArtists.size})", "View All (${KhmerArtistDirectory.featuredArtists.size})")
                            },
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = searchAccent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onSelectCategory(SearchCategory.ARTISTS)
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .testTag("view_all_khmer_artists_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.testTag("khmer_artists_quick_row")
                    ) {
                        items(
                            items = KhmerArtistDirectory.featuredArtists,
                            key = { "quick_khmer_${it.id}" }
                        ) { artistProfile ->
                            val isActiveArtist = q.equals(artistProfile.englishName, ignoreCase = true) ||
                                q == artistProfile.khmerName ||
                                q.equals(artistProfile.displayName, ignoreCase = true)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isActiveArtist) searchAccent.copy(alpha = 0.18f)
                                        else palette.elevatedSurface
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isActiveArtist) searchAccent else palette.divider,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onQueryChange(artistProfile.englishName)
                                        if (selectedCategory == SearchCategory.ARTISTS) {
                                            onSelectCategory(SearchCategory.ALL)
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                                    .testTag("khmer_artist_chip_${artistProfile.id}")
                            ) {
                                Text(
                                    text = "${artistProfile.englishName} · ${artistProfile.khmerName}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isActiveArtist) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isActiveArtist) searchAccent else palette.primaryText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // When user searches a Khmer artist name in ALL/SONGS/VIDEOS mode, show matching Artist Profile Card(s)
                    if (selectedCategory != SearchCategory.ARTISTS && q.isNotEmpty() && matchingKhmerArtists.isNotEmpty()) {
                        val topArtists = matchingKhmerArtists.take(3)
                        topArtists.forEach { profile ->
                            val artistTracks = artistTracksByProfileId[profile.id].orEmpty()
                            val repMedia = artistTracks.firstOrNull()
                            KhmerArtistSearchResultCard(
                                profile = profile,
                                representativeMedia = repMedia,
                                trackCount = artistTracks.size,
                                onSelectArtist = {
                                    onQueryChange(profile.englishName)
                                },
                                onPlayTopTrack = {
                                    if (repMedia != null) {
                                        onPlayMedia(repMedia, true)
                                    } else {
                                        onQueryChange(profile.englishName)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    // Featured Album/Track Hero Card (Exact Image 3 right layout: 104dp artwork + Metadata + Play & Download pills)
                    val spotlightMedia = resolvedYouTubeMedia ?: filteredResults.firstOrNull()
                    if (spotlightMedia != null && selectedCategory != SearchCategory.ARTISTS) {
                        FeaturedReferenceSpotlightCard(
                            media = spotlightMedia,
                            totalTrackCount = filteredResults.size,
                            onPlay = { onPlayMedia(spotlightMedia, true) },
                            onDownloadMp3 = { onQuickDownloadFormat(spotlightMedia, DownloadFormatOption.MP3_320) },
                            onDownloadMp4 = { onQuickDownloadFormat(spotlightMedia, DownloadFormatOption.MP4_1080P) },
                            onOpenOptions = { onOpenTrackOptions(spotlightMedia) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        if (selectedCategory == SearchCategory.ARTISTS && !isLinkDownloaderMode) {
            // 1. Show matching Khmer artists from KhmerArtistDirectory
            items(
                items = matchingKhmerArtists,
                key = { "khmer_dir_artist_${it.id}" }
            ) { profile ->
                val artistTracks = artistTracksByProfileId[profile.id].orEmpty()
                val repMedia = artistTracks.firstOrNull()
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)) {
                    KhmerArtistSearchResultCard(
                        profile = profile,
                        representativeMedia = repMedia,
                        trackCount = artistTracks.size,
                        onSelectArtist = {
                            onQueryChange(profile.englishName)
                            onSelectCategory(SearchCategory.ALL)
                        },
                        onPlayTopTrack = {
                            if (repMedia != null) {
                                onPlayMedia(repMedia, true)
                            } else {
                                onQueryChange(profile.englishName)
                                onSelectCategory(SearchCategory.ALL)
                            }
                        }
                    )
                }
            }

            // 2. Also show any additional distinct artists from loaded tracks that aren't already in matchingKhmerArtists
            items(distinctOtherArtists, key = { "artist_${it.first}" }) { (artistName, sortedArtistTracks) ->
                val rep = sortedArtistTracks.first()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.surface)
                        .border(1.dp, palette.divider, RoundedCornerShape(16.dp))
                        .clickable {
                            onQueryChange(artistName)
                            onSelectCategory(SearchCategory.ALL)
                        }
                        .padding(12.dp)
                ) {
                    LumoArtworkImage(
                        artworkUrl = rep.artworkUrl,
                        fallbackResId = rep.artworkResId,
                        contentDescription = artistName,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = artistName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = palette.primaryText
                        )
                        Text(
                            text = "${sortedArtistTracks.size} tracks · Top: ${rep.title}",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.secondaryText
                        )
                    }
                    IconButton(onClick = { onPlayMedia(rep, true) }) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play $artistName",
                            tint = searchAccent
                        )
                    }
                }
            }
        } else {
            if (q.isNotEmpty() && !isLinkDownloaderMode) {
                item {
                    Surface(
                        color = palette.elevatedSurface,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .border(1.dp, searchAccent.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                            .clickable { onResolveYouTubeLink(q, null) }
                            .testTag("deep_resolve_song_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = language.tr(
                                        "រកមិនឃើញបទ \"$q\" មែនទេ? (ស្វែងរកមិនកំណត់)",
                                        "Don't see the exact song \"$q\"?"
                                    ),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = palette.primaryText
                                )
                                Text(
                                    text = language.tr(
                                        "ចុចទីនេះដើម្បីទាញយកបទចម្រៀងផ្ទាល់ពី YouTube + ប្រភពពិភពលោក",
                                        "Tap to Deep-Fetch & Resolve exact track from YouTube + Multi-Source"
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.secondaryText
                                )
                            }
                            Surface(
                                color = solidPillBg,
                                shape = CircleShape
                            ) {
                                Text(
                                    text = if (isResolvingYouTubeLink) {
                                        language.tr("កំពុងស្វែងរក…", "Fetching…")
                                    } else {
                                        language.tr("ស្វែងរកផ្ទាល់", "Find Song")
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = solidPillText,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (filteredResults.isNotEmpty() && !isLinkDownloaderMode) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (selectedCategory) {
                                SearchCategory.VIDEOS -> language.tr("វីដេអូពេញនិយមមុនគេ", "Popular Videos First")
                                SearchCategory.TRENDING -> language.tr("បទថ្មីកំពុងពេញនិយម · Trending", "Trending New · បទថ្មីកំពុងពេញនិយម")
                                else -> language.tr("បទល្បីៗមុនគេ · មិនកំណត់", "Popular Songs First · បទល្បីៗ")
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = palette.primaryText
                        )
                        Text(
                            text = language.tr("${filteredResults.size} បទ · មិនកំណត់", "${filteredResults.size} tracks"),
                            style = MaterialTheme.typography.labelMedium,
                            color = searchAccent
                        )
                    }
                }
            }

            // Clean Numbered Track List (01, 02, 03... with Equalizer highlight when playing + 1-Tap Download)
            itemsIndexed(
                items = filteredResults,
                key = { _, item -> "search_res_${item.id}" }
            ) { index, media ->
                CleanNumberedSearchRow(
                    trackNumber = index + 1,
                    media = media,
                    isCurrentlyPlaying = currentPlayingMediaId == media.id,
                    onPlay = { onPlayMedia(media, true) },
                    onQuickDownloadMp3 = { onQuickDownloadFormat(media, DownloadFormatOption.MP3_320) },
                    onOpenDownloadSheet = { onOpenDownloadSheet(media) },
                    onOpenOptions = { onOpenTrackOptions(media) }
                )
            }

            // Unlimited Search Load More Button (មិនកំណត់)
            if (q.isNotEmpty() && !isLinkDownloaderMode) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Surface(
                            color = palette.elevatedSurface,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, searchAccent.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                                .clickable(enabled = !isLoadingMoreSearch) { onLoadMoreOnlineSearch() }
                                .testTag("load_more_unlimited_songs_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
                            ) {
                                if (isLoadingMoreSearch) {
                                    CircularProgressIndicator(
                                        color = searchAccent,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = language.tr(
                                            "កំពុងស្វែងរកបទចម្រៀងបន្ថែម មិនកំណត់…",
                                            "Loading more songs from global catalog…"
                                        ),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = palette.primaryText
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = null,
                                        tint = searchAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = language.tr(
                                            "បង្ហាញបទចម្រៀងបន្ថែមទៀត · មិនកំណត់ (+65 បទ)",
                                            "Load More Songs · Unlimited Search (+65 Tracks)"
                                        ),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = searchAccent
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KhmerArtistSearchResultCard(
    profile: KhmerArtistProfile,
    representativeMedia: MediaItemEntity?,
    trackCount: Int,
    onSelectArtist: () -> Unit,
    onPlayTopTrack: () -> Unit
) {
    val palette = LocalLumoPalette.current
    val searchAccent = palette.searchAccent

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(palette.surface)
            .border(1.dp, palette.divider, RoundedCornerShape(16.dp))
            .clickable { onSelectArtist() }
            .padding(12.dp)
            .testTag("khmer_artist_card_${profile.id}")
    ) {
        if (representativeMedia != null && representativeMedia.artworkUrl.isNotBlank()) {
            LumoArtworkImage(
                artworkUrl = representativeMedia.artworkUrl,
                fallbackResId = representativeMedia.artworkResId,
                contentDescription = profile.displayName,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(searchAccent.copy(alpha = 0.16f))
                    .border(1.dp, searchAccent.copy(alpha = 0.45f), CircleShape)
            ) {
                Text(
                    text = profile.englishName.take(2).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = searchAccent
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = profile.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            val popularHitsLabel = if (profile.popularSongs.isNotEmpty()) {
                "Popular: ${profile.popularSongs.take(3).joinToString(", ")}"
            } else {
                profile.genreBadge
            }
            Text(
                text = if (trackCount > 0) {
                    "$popularHitsLabel · $trackCount songs loaded"
                } else {
                    "$popularHitsLabel · Tap to load popular songs"
                },
                style = MaterialTheme.typography.bodySmall,
                color = palette.secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = onPlayTopTrack,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(searchAccent.copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Play ${profile.englishName}",
                tint = searchAccent
            )
        }
    }
}

@Composable
private fun YouTubeLinkDownloaderSection(
    youtubeLinkInput: String,
    isResolving: Boolean,
    resolvedMedia: MediaItemEntity?,
    onInputChange: (String) -> Unit,
    onPasteFromClipboard: () -> Unit,
    onFetchLink: (String) -> Unit,
    onDownloadMp4: (MediaItemEntity) -> Unit,
    onDownloadMp3: (MediaItemEntity) -> Unit,
    onPlayMedia: (MediaItemEntity) -> Unit
) {
    val palette = LocalLumoPalette.current
    val solidPillBg = if (palette.option.isDark) Color.White else Color(0xFF0F172A)
    val solidPillText = if (palette.option.isDark) Color(0xFF0B0F17) else Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(palette.surface)
            .border(1.dp, palette.divider, RoundedCornerShape(24.dp))
            .padding(18.dp)
            .testTag("youtube_link_downloader_card")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = LumoIcons.YouTubePlay,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "YouTube Video & MP3 Link Downloader",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.primaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = youtubeLinkInput,
            onValueChange = onInputChange,
            placeholder = {
                Text(
                    text = "Paste YouTube link (youtu.be/... or youtube.com/watch?v=...)",
                    color = palette.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = LumoIcons.Link,
                    contentDescription = "YouTube URL",
                    tint = palette.downloadsAccent
                )
            },
            trailingIcon = {
                if (youtubeLinkInput.isNotEmpty()) {
                    IconButton(onClick = { onInputChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Clear link",
                            tint = palette.secondaryText
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = palette.primaryText.copy(alpha = 0.45f),
                unfocusedBorderColor = palette.divider,
                focusedContainerColor = palette.elevatedSurface,
                unfocusedContainerColor = palette.elevatedSurface,
                focusedTextColor = palette.primaryText,
                unfocusedTextColor = palette.primaryText
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("youtube_url_input_field")
        )

        Spacer(modifier = Modifier.height(12.dp))

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
                    .background(palette.elevatedSurface)
                    .border(1.dp, palette.divider, RoundedCornerShape(13.dp))
                    .clickable(onClick = onPasteFromClipboard)
                    .testTag("paste_youtube_link_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = LumoIcons.ContentPaste,
                        contentDescription = "Paste link",
                        tint = palette.primaryText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Paste Link",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.primaryText
                    )
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1.2f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(solidPillBg)
                    .clickable {
                        val target = youtubeLinkInput.ifBlank { SAMPLE_YOUTUBE_LINKS.first().url }
                        onInputChange(target)
                        onFetchLink(target)
                    }
                    .testTag("fetch_youtube_link_button")
            ) {
                if (isResolving) {
                    CircularProgressIndicator(
                        color = solidPillText,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = LumoIcons.Download,
                            contentDescription = "Analyze Link",
                            tint = solidPillText,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Analyze Link",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = solidPillText
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Quick Try YouTube Links:",
            style = MaterialTheme.typography.labelSmall,
            color = palette.secondaryText
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(SAMPLE_YOUTUBE_LINKS) { sample ->
                Surface(
                    color = palette.elevatedSurface,
                    shape = CircleShape,
                    modifier = Modifier
                        .border(1.dp, palette.divider, CircleShape)
                        .clickable {
                            onInputChange(sample.url)
                            onFetchLink(sample.url)
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = LumoIcons.YouTubePlay,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = sample.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.primaryText
                        )
                    }
                }
            }
        }

        if (resolvedMedia != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(palette.divider)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Resolved YouTube Preview Card with 1-Tap Play button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlayMedia(resolvedMedia) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    LumoArtworkImage(
                        artworkUrl = resolvedMedia.artworkUrl,
                        fallbackResId = resolvedMedia.artworkResId,
                        contentDescription = resolvedMedia.title,
                        modifier = Modifier
                            .size(86.dp)
                            .shadow(14.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .border(1.dp, Color.White.copy(alpha = 0.65f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play Exact YouTube Stream",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${resolvedMedia.category} · ${resolvedMedia.formattedDuration}",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.secondaryText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = resolvedMedia.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = palette.primaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = resolvedMedia.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Direct 1-Tap Download MP4 Video & MP3 Audio Pills
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
                        .clickable { onDownloadMp4(resolvedMedia) }
                        .testTag("youtube_download_mp4_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = LumoIcons.Videocam,
                            contentDescription = "Download MP4 Video",
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
                        .clickable { onDownloadMp3(resolvedMedia) }
                        .testTag("youtube_download_mp3_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = LumoIcons.MusicNote,
                            contentDescription = "Download MP3 Audio",
                            tint = palette.primaryText,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save Audio (MP3)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.primaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Note: Saving/importing media is only available for content you are authorized or permitted to download.",
                style = MaterialTheme.typography.labelSmall,
                color = palette.secondaryText
            )
        }
    }
}

@Composable
private fun FeaturedReferenceSpotlightCard(
    media: MediaItemEntity,
    totalTrackCount: Int,
    onPlay: () -> Unit,
    onDownloadMp3: () -> Unit,
    onDownloadMp4: () -> Unit,
    onOpenOptions: () -> Unit
) {
    val palette = LocalLumoPalette.current
    val solidPillBg = if (palette.option.isDark) Color.White else Color(0xFF0F172A)
    val solidPillText = if (palette.option.isDark) Color(0xFF0B0F17) else Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Exact Image 3 (right) Header Row: 100dp Rounded Square Art + Metadata + 3 mini icons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            LumoArtworkImage(
                artworkUrl = media.artworkUrl,
                fallbackResId = media.artworkResId,
                contentDescription = media.title,
                modifier = Modifier
                    .size(100.dp)
                    .shadow(18.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .clickable(onClick = onPlay)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "#1 Popular • ${media.formattedPopularity} • $totalTrackCount songs",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.secondaryText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = media.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = palette.primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = media.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .border(1.dp, palette.divider, CircleShape)
                            .clickable(onClick = onDownloadMp3)
                    ) {
                        Icon(
                            imageVector = LumoIcons.MusicNote,
                            contentDescription = "Quick Download MP3",
                            tint = palette.primaryText,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .border(1.dp, palette.divider, CircleShape)
                            .clickable(onClick = onDownloadMp4)
                    ) {
                        Icon(
                            imageVector = LumoIcons.Download,
                            contentDescription = "Quick Download MP4",
                            tint = palette.primaryText,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .border(1.dp, palette.divider, CircleShape)
                            .clickable(onClick = onOpenOptions)
                    ) {
                        Icon(
                            imageVector = LumoIcons.MoreHoriz,
                            contentDescription = "More options",
                            tint = palette.primaryText,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Exact Image 3 (right) Dual Pill Row: [ ▶ Play ] [ ⬇ Download ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(solidPillBg)
                    .clickable(onClick = onPlay)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = solidPillText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Play",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = solidPillText
                    )
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.elevatedSurface)
                    .border(1.dp, palette.divider, RoundedCornerShape(14.dp))
                    .clickable(onClick = onDownloadMp3)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = LumoIcons.Download,
                        contentDescription = null,
                        tint = palette.primaryText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Download Song",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = palette.primaryText
                    )
                }
            }
        }
    }
}

@Composable
private fun CleanNumberedSearchRow(
    trackNumber: Int,
    media: MediaItemEntity,
    isCurrentlyPlaying: Boolean,
    onPlay: () -> Unit,
    onQuickDownloadMp3: () -> Unit,
    onOpenDownloadSheet: () -> Unit,
    onOpenOptions: () -> Unit
) {
    val palette = LocalLumoPalette.current
    val rowBg = if (isCurrentlyPlaying) palette.elevatedSurface else Color.Transparent

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(rowBg)
            .clickable(onClick = onPlay)
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .testTag("search_result_${media.id}")
    ) {
        // Track number (01, 02...) or Equalizer icon when playing (Exact Image 3 right style!)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.width(28.dp)
        ) {
            if (isCurrentlyPlaying) {
                Icon(
                    imageVector = LumoIcons.Equalizer,
                    contentDescription = "Playing",
                    tint = palette.primaryText,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = "%02d".format(trackNumber),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.secondaryText
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        LumoArtworkImage(
            artworkUrl = media.artworkUrl,
            fallbackResId = media.artworkResId,
            contentDescription = media.title,
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = media.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = palette.primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${media.artist}  ·  ${media.formattedPopularity}  ·  ${media.formattedDuration}",
                style = MaterialTheme.typography.bodySmall,
                color = palette.secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (media.isAuthorizedDownload) {
            // 1-Tap Quick MP3/MP4 Download pill + Format Picker Icon
            Surface(
                color = palette.elevatedSurface,
                shape = CircleShape,
                modifier = Modifier
                    .border(1.dp, palette.divider, CircleShape)
                    .clickable(onClick = onQuickDownloadMp3)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = LumoIcons.Download,
                        contentDescription = null,
                        tint = palette.primaryText,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (media.isVideo) "MP4" else "MP3",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = palette.primaryText
                    )
                }
            }

            IconButton(
                onClick = onOpenDownloadSheet,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("search_download_${media.id}")
            ) {
                Icon(
                    imageVector = LumoIcons.Download,
                    contentDescription = "Choose download format for ${media.title}",
                    tint = palette.downloadsAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        IconButton(
            onClick = onOpenOptions,
            modifier = Modifier
                .size(36.dp)
                .testTag("search_more_${media.id}")
        ) {
            Icon(
                imageVector = LumoIcons.MoreHoriz,
                contentDescription = "More options",
                tint = palette.secondaryText,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
