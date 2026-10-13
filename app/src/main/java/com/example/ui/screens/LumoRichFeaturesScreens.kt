package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DownloadTaskEntity
import com.example.data.KhmerArtistDirectory
import com.example.data.MediaItemEntity
import com.example.data.formatSeconds
import com.example.ui.components.LumoArtworkImage
import com.example.ui.theme.LocalColorReactiveState
import com.example.ui.theme.LocalLumoLanguage
import com.example.ui.theme.LocalLumoPalette
import com.example.ui.theme.LumoIcons
import kotlinx.coroutines.delay

/**
 * Section 1: Minimal LUMO wordmark with a minimal music-inspired acoustic wave symbol.
 */
@Composable
fun LumoWordmarkLogo(
    accentColor: Color,
    textColor: Color,
    symbolSize: Dp = 18.dp,
    fontSizeSp: Int = 18,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.size(symbolSize)) {
            val barW = size.width * 0.16f
            val gap = size.width * 0.11f
            val heights = listOf(0.42f, 0.86f, 1.0f, 0.62f)
            heights.forEachIndexed { idx, hFrac ->
                val h = size.height * hFrac
                val x = idx * (barW + gap)
                val y = (size.height - h) / 2f
                drawRoundRect(
                    color = if (idx == 1 || idx == 2) accentColor else textColor.copy(alpha = 0.85f),
                    topLeft = Offset(x, y),
                    size = Size(barW, h),
                    cornerRadius = CornerRadius(barW / 2f, barW / 2f)
                )
            }
        }
        Text(
            text = "LUMO",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.0.sp,
                fontSize = fontSizeSp.sp
            ),
            color = textColor
        )
    }
}

/**
 * Section 4: Premium Minimal Splash Screen (< 1.5s fade & scale transition into Home).
 */
@Composable
fun LumoSplashOverlay(
    visible: Boolean,
    accentColor: Color,
    onDismiss: () -> Unit
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (visible) {
            started = true
            delay(1150L)
            onDismiss()
        }
    }

    val logoScale by animateFloatAsState(
        targetValue = if (started) 1.0f else 0.91f,
        animationSpec = tween(durationMillis = 680, easing = FastOutSlowInEasing),
        label = "splashScale"
    )
    val gradAlpha by animateFloatAsState(
        targetValue = if (started) 0.24f else 0.02f,
        animationSpec = tween(durationMillis = 850),
        label = "splashGrad"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(220)),
        exit = fadeOut(tween(380))
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090D14))
                .clickable(onClick = onDismiss)
                .testTag("lumo_splash_overlay")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = gradAlpha),
                            Color.Transparent
                        )
                    ),
                    radius = size.minDimension * 0.68f,
                    center = Offset(size.width * 0.5f, size.height * 0.46f)
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.scale(logoScale)
            ) {
                LumoWordmarkLogo(
                    accentColor = accentColor,
                    textColor = Color.White,
                    symbolSize = 30.dp,
                    fontSizeSp = 32
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Discover. Play. Save.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        letterSpacing = 1.1.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

/**
 * Section 22: Dedicated Artist Page (Large artist image, Follow button, Popular Songs, Albums, Singles, Related Artists, More From This Artist).
 */
@Composable
fun ArtistDetailScreen(
    artistName: String,
    allMedia: List<MediaItemEntity>,
    currentPlayingMediaId: String?,
    onBack: () -> Unit,
    onPlayMedia: (MediaItemEntity, List<MediaItemEntity>, Boolean) -> Unit,
    onToggleFavorite: (MediaItemEntity) -> Unit,
    onOpenDownloadSheet: (MediaItemEntity) -> Unit,
    onOpenTrackOptions: (MediaItemEntity) -> Unit,
    onSelectRelatedArtist: (String) -> Unit,
    onOpenAlbum: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val palette = LocalLumoPalette.current
    val reactive = LocalColorReactiveState.current
    val language = LocalLumoLanguage.current
    var isFollowing by remember(artistName) { mutableStateOf(false) }

    val artistTracks = remember(artistName, allMedia) {
        val exact = allMedia.filter {
            it.artist.contains(artistName, ignoreCase = true) ||
                KhmerArtistDirectory.matchesMediaItem(it, artistName)
        }
        val list = if (exact.isNotEmpty()) exact else allMedia.take(6)
        KhmerArtistDirectory.sortPopularSongsFirst(list)
    }
    val heroTrack = artistTracks.firstOrNull() ?: allMedia.firstOrNull()
    val albums = remember(artistTracks) {
        artistTracks.groupBy { it.album }.entries.toList()
    }
    val singles = remember(artistTracks) {
        artistTracks.filter { !it.isVideo }.take(5)
    }
    val relatedArtists = remember(artistName, allMedia) {
        val khmerRelated = KhmerArtistDirectory.featuredArtists
            .filterNot { it.englishName.equals(artistName, ignoreCase = true) || it.displayName == artistName }
            .take(6)
        khmerRelated
    }

    val solidPillBg = if (palette.option.isDark) Color.White else Color(0xFF0F172A)
    val solidPillText = if (palette.option.isDark) Color(0xFF0B0F17) else Color.White

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .testTag("artist_detail_screen"),
        contentPadding = PaddingValues(bottom = 148.dp)
    ) {
        // Large Artist Header
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = palette.primaryText
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = language.tr("សិល្បករ · Artist", "Artist"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = palette.primaryText
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(136.dp)
                        .clip(CircleShape)
                        .border(2.dp, reactive.reactiveAccentColor.copy(alpha = 0.6f), CircleShape)
                ) {
                    if (heroTrack != null) {
                        LumoArtworkImage(
                            artworkUrl = heroTrack.artworkUrl,
                            fallbackResId = heroTrack.artworkResId,
                            contentDescription = artistName,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = artistName,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    ),
                    color = palette.primaryText,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${artistTracks.size} tracks · Verified Artist",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.secondaryText
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (artistTracks.isNotEmpty()) {
                                onPlayMedia(artistTracks.first(), artistTracks, true)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = solidPillBg,
                            contentColor = solidPillText
                        ),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(language.tr("ចាក់ទាំងអស់", "Play"), fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { isFollowing = !isFollowing },
                        shape = CircleShape,
                        modifier = Modifier.testTag("artist_follow_button")
                    ) {
                        Text(
                            text = if (isFollowing) language.tr("កំពុងតាមដាន ✓", "Following ✓") else language.tr("+ តាមដាន", "Follow"),
                            color = if (isFollowing) reactive.reactiveAccentColor else palette.primaryText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Popular Songs (Compact List Layout)
        item {
            Text(
                text = language.tr("បទល្បីៗ · Popular Songs", "Popular Songs"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = palette.primaryText,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        itemsIndexed(artistTracks.take(8), key = { _, m -> "artist_pop_${m.id}" }) { index, track ->
            CompactTrackRow(
                media = track,
                indexNumber = index + 1,
                isCurrentlyPlaying = track.id == currentPlayingMediaId,
                onPlay = { onPlayMedia(track, artistTracks, false) },
                onToggleFavorite = { onToggleFavorite(track) },
                onDownload = { onOpenDownloadSheet(track) },
                onOpenOptions = { onOpenTrackOptions(track) }
            )
        }

        // Albums
        if (albums.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = language.tr("អាល់ប៊ុម · Albums", "Albums"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.primaryText,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(albums, key = { "artist_alb_${it.key}" }) { (albumTitle, tracks) ->
                        val rep = tracks.first()
                        Column(
                            modifier = Modifier
                                .width(136.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(palette.surface)
                                .border(1.dp, palette.divider, RoundedCornerShape(18.dp))
                                .clickable { onOpenAlbum(albumTitle) }
                                .padding(10.dp)
                        ) {
                            LumoArtworkImage(
                                artworkUrl = rep.artworkUrl,
                                fallbackResId = rep.artworkResId,
                                contentDescription = albumTitle,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(114.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = albumTitle,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = palette.primaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${rep.releaseYear} · ${tracks.size} songs",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.secondaryText,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Singles & More From This Artist
        if (singles.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = language.tr("បទទោល និងបទផ្សេងទៀត · Singles & More From This Artist", "Singles & More From This Artist"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.primaryText,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(singles, key = { "artist_single_${it.id}" }) { single ->
                        Column(
                            modifier = Modifier
                                .width(128.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(palette.surface)
                                .border(1.dp, palette.divider, RoundedCornerShape(16.dp))
                                .clickable { onPlayMedia(single, artistTracks, true) }
                                .padding(10.dp)
                        ) {
                            LumoArtworkImage(
                                artworkUrl = single.artworkUrl,
                                fallbackResId = single.artworkResId,
                                contentDescription = single.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(106.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = single.title,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = palette.primaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Single · ${single.formattedDuration}",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.secondaryText
                            )
                        }
                    }
                }
            }
        }

        // Related Artists
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = language.tr("សិល្បករស្រដៀងគ្នា · Related Artists", "Related Artists"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = palette.primaryText,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(relatedArtists, key = { "rel_art_${it.id}" }) { rel ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(92.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectRelatedArtist(rel.englishName) }
                            .padding(6.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(palette.elevatedSurface)
                                .border(1.dp, palette.divider, CircleShape)
                        ) {
                            Text(
                                text = rel.englishName.take(2).uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = reactive.reactiveAccentColor
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = rel.englishName,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.primaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Section 23: Dedicated Album Page (Artwork, Album name, Artist, Release year, Song count, Play/Shuffle/Add buttons, numbered track list).
 */
@Composable
fun AlbumDetailScreen(
    albumName: String,
    allMedia: List<MediaItemEntity>,
    currentPlayingMediaId: String?,
    onBack: () -> Unit,
    onPlayMedia: (MediaItemEntity, List<MediaItemEntity>, Boolean) -> Unit,
    onToggleFavorite: (MediaItemEntity) -> Unit,
    onOpenDownloadSheet: (MediaItemEntity) -> Unit,
    onOpenTrackOptions: (MediaItemEntity) -> Unit,
    onAddAlbumToPlaylist: (MediaItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val palette = LocalLumoPalette.current
    val language = LocalLumoLanguage.current

    val albumTracks = remember(albumName, allMedia) {
        val matches = allMedia.filter { it.album.equals(albumName, ignoreCase = true) }
        if (matches.isNotEmpty()) matches else allMedia.take(5)
    }
    val rep = albumTracks.firstOrNull() ?: return
    val solidPillBg = if (palette.option.isDark) Color.White else Color(0xFF0F172A)
    val solidPillText = if (palette.option.isDark) Color(0xFF0B0F17) else Color.White

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .testTag("album_detail_screen"),
        contentPadding = PaddingValues(bottom = 148.dp)
    ) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = palette.primaryText
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = language.tr("អាល់ប៊ុម · Album", "Album"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = palette.primaryText
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LumoArtworkImage(
                    artworkUrl = rep.artworkUrl,
                    fallbackResId = rep.artworkResId,
                    contentDescription = albumName,
                    modifier = Modifier
                        .size(188.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, palette.divider, RoundedCornerShape(24.dp))
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = albumName,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = palette.primaryText,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${rep.artist} · ${rep.releaseYear} · ${albumTracks.size} songs",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.secondaryText
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onPlayMedia(albumTracks.first(), albumTracks, true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = solidPillBg,
                            contentColor = solidPillText
                        ),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(language.tr("ចាក់", "Play"), fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val shuffled = albumTracks.shuffled()
                            onPlayMedia(shuffled.first(), shuffled, true)
                        },
                        shape = CircleShape
                    ) {
                        Icon(LumoIcons.Shuffle, contentDescription = null, tint = palette.primaryText, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(language.tr("ច្របល់", "Shuffle"), color = palette.primaryText, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onAddAlbumToPlaylist(rep) },
                        shape = CircleShape
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = palette.primaryText, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(language.tr("បន្ថែម", "Add"), color = palette.primaryText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        itemsIndexed(albumTracks, key = { _, m -> "alb_trk_${m.id}" }) { index, track ->
            CompactTrackRow(
                media = track,
                indexNumber = index + 1,
                isCurrentlyPlaying = track.id == currentPlayingMediaId,
                onPlay = { onPlayMedia(track, albumTracks, false) },
                onToggleFavorite = { onToggleFavorite(track) },
                onDownload = { onOpenDownloadSheet(track) },
                onOpenOptions = { onOpenTrackOptions(track) }
            )
        }
    }
}

/**
 * Section 20: Queue Bottom Sheet (Now Playing + Up Next with Reorder, Remove, Play next, Clear queue).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueBottomSheet(
    currentMedia: MediaItemEntity,
    queue: List<MediaItemEntity>,
    onDismiss: () -> Unit,
    onSelectTrack: (MediaItemEntity) -> Unit,
    onMoveItem: (Int, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClearQueue: () -> Unit
) {
    val palette = LocalLumoPalette.current
    val reactive = LocalColorReactiveState.current
    val language = LocalLumoLanguage.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.surface,
        contentColor = palette.primaryText,
        modifier = Modifier.testTag("queue_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = language.tr("បញ្ជីរង់ចាំចាក់ · Queue", "Playback Queue"),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = palette.primaryText
                    )
                    Text(
                        text = "${queue.size} songs in queue",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.secondaryText
                    )
                }
                TextButton(onClick = onClearQueue) {
                    Text(
                        text = language.tr("សម្អាត · Clear", "Clear Queue"),
                        color = reactive.reactiveAccentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "NOW PLAYING",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = reactive.reactiveAccentColor
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.elevatedSurface)
                    .border(1.dp, reactive.reactiveAccentColor.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                    .padding(10.dp)
            ) {
                LumoArtworkImage(
                    artworkUrl = currentMedia.artworkUrl,
                    fallbackResId = currentMedia.artworkResId,
                    contentDescription = currentMedia.title,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentMedia.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = reactive.reactiveAccentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${currentMedia.artist} · ${currentMedia.formattedDuration}",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.secondaryText,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "UP NEXT",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = palette.secondaryText
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(queue, key = { idx, item -> "q_${idx}_${item.id}" }) { index, item ->
                    val isCurrent = item.id == currentMedia.id
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isCurrent) reactive.reactiveAccentColor.copy(alpha = 0.12f) else palette.elevatedSurface)
                            .clickable { onSelectTrack(item) }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = String.format("%02d", index + 1),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isCurrent) reactive.reactiveAccentColor else palette.secondaryText,
                            modifier = Modifier.width(26.dp)
                        )
                        LumoArtworkImage(
                            artworkUrl = item.artworkUrl,
                            fallbackResId = item.artworkResId,
                            contentDescription = item.title,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isCurrent) reactive.reactiveAccentColor else palette.primaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.secondaryText,
                                maxLines = 1
                            )
                        }

                        if (index > 0) {
                            IconButton(
                                onClick = { onMoveItem(index, index - 1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = LumoIcons.ArrowUpward,
                                    contentDescription = "Move up",
                                    tint = palette.secondaryText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        if (index < queue.lastIndex) {
                            IconButton(
                                onClick = { onMoveItem(index, index + 1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = LumoIcons.ArrowDownward,
                                    contentDescription = "Move down",
                                    tint = palette.secondaryText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        if (!isCurrent) {
                            IconButton(
                                onClick = { onRemoveItem(item.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Remove from queue",
                                    tint = palette.secondaryText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Section 28: Lyrics Bottom Sheet (Scrollable licensed/studio/local lyrics + user custom lyrics notes).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsBottomSheet(
    media: MediaItemEntity,
    onDismiss: () -> Unit
) {
    val palette = LocalLumoPalette.current
    val reactive = LocalColorReactiveState.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isEditingCustomLyrics by remember { mutableStateOf(false) }
    var customLyricsText by remember(media.id) {
        val parsedLines = media.parsedSubtitles().joinToString("\n") { it.text }
        mutableStateOf(
            if (parsedLines.isNotBlank() && !parsedLines.startsWith("YouTube")) {
                parsedLines
            } else {
                """
                ♪ ${media.title} — ${media.artist}
                Album: ${media.album} (${media.releaseYear})
                
                [Verse 1]
                Soft acoustic waves across the midnight air,
                Every note in motion, calm and crystal clear.
                
                [Chorus]
                Discover the rhythm, let the sound unfold,
                Stories in the melody, timeless and bold.
                
                [Licensed / Local Lyrics Notice]
                LUMO displays studio-licensed or user-provided local lyrics only. Tap 'Edit Local Lyrics' above to attach your own synchronized notes or lyrics for offline playback.
                """.trimIndent()
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.surface,
        contentColor = palette.primaryText,
        modifier = Modifier.testTag("lyrics_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = palette.primaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${media.artist} · Licensed / Local Lyrics",
                        style = MaterialTheme.typography.bodySmall,
                        color = reactive.reactiveAccentColor
                    )
                }
                TextButton(onClick = { isEditingCustomLyrics = !isEditingCustomLyrics }) {
                    Text(
                        text = if (isEditingCustomLyrics) "Done" else "Edit Local Lyrics",
                        color = reactive.reactiveAccentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isEditingCustomLyrics) {
                OutlinedTextField(
                    value = customLyricsText,
                    onValueChange = { customLyricsText = it },
                    label = { Text("Local / User-Provided Lyrics") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(palette.elevatedSurface)
                        .border(1.dp, palette.divider, RoundedCornerShape(20.dp))
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp)
                ) {
                    Text(
                        text = customLyricsText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = palette.primaryText
                    )
                }
            }
        }
    }
}

/**
 * Section 26 & 27: Equalizer & Sleep Timer Bottom Sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerAndSleepTimerSheet(
    currentEqPreset: String,
    sleepTimerMinutes: Int,
    sleepTimerRemainingSec: Int,
    visualizerEnabled: Boolean,
    onSelectEqPreset: (String) -> Unit,
    onSelectSleepTimer: (Int) -> Unit,
    onToggleVisualizer: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalLumoPalette.current
    val reactive = LocalColorReactiveState.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val presets = listOf("Flat", "Bass Boost", "Vocal", "Pop", "Rock", "Classical", "Electronic")
    val bandLabels = listOf("60Hz", "230Hz", "910Hz", "3.6kHz", "14kHz")
    val bandValues = remember(currentEqPreset) {
        val defaults = when (currentEqPreset) {
            "Bass Boost" -> listOf(0.85f, 0.72f, 0.50f, 0.52f, 0.58f)
            "Vocal" -> listOf(0.42f, 0.55f, 0.82f, 0.76f, 0.52f)
            "Pop" -> listOf(0.62f, 0.70f, 0.75f, 0.68f, 0.64f)
            "Rock" -> listOf(0.78f, 0.62f, 0.46f, 0.72f, 0.80f)
            "Classical" -> listOf(0.64f, 0.58f, 0.50f, 0.68f, 0.76f)
            "Electronic" -> listOf(0.82f, 0.68f, 0.48f, 0.70f, 0.84f)
            else -> listOf(0.50f, 0.50f, 0.50f, 0.50f, 0.50f)
        }
        mutableStateListOf(*defaults.toTypedArray())
    }

    val sleepOptions = listOf(
        0 to "Off",
        15 to "15 min",
        30 to "30 min",
        45 to "45 min",
        60 to "60 min",
        -1 to "End of song"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.surface,
        contentColor = palette.primaryText,
        modifier = Modifier.testTag("equalizer_sleep_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = "Audio Equalizer & Sleep Timer",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = palette.primaryText
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Visualizer ON/OFF Toggle (Section 11)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.elevatedSurface)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Music Visualizer",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = palette.primaryText
                    )
                    Text(
                        text = "Reactive waveform, equalizer bars & subtle artwork pulse",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.secondaryText
                    )
                }
                Switch(
                    checked = visualizerEnabled,
                    onCheckedChange = { onToggleVisualizer() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = reactive.reactiveAccentColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Equalizer Presets (Section 27)
            Text(
                text = "EQUALIZER PRESETS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = palette.secondaryText
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(presets) { preset ->
                    val selected = preset == currentEqPreset
                    Surface(
                        color = if (selected) reactive.reactiveAccentColor else palette.elevatedSurface,
                        shape = CircleShape,
                        modifier = Modifier
                            .border(1.dp, if (selected) reactive.reactiveAccentColor else palette.divider, CircleShape)
                            .clickable { onSelectEqPreset(preset) }
                    ) {
                        Text(
                            text = preset,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (selected) Color.White else palette.primaryText,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5-Band EQ Controls
            bandLabels.forEachIndexed { idx, freq ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = freq,
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.secondaryText,
                        modifier = Modifier.width(48.dp)
                    )
                    Slider(
                        value = bandValues[idx],
                        onValueChange = { bandValues[idx] = it },
                        colors = SliderDefaults.colors(
                            thumbColor = reactive.reactiveAccentColor,
                            activeTrackColor = reactive.reactiveAccentColor
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    val db = ((bandValues[idx] - 0.5f) * 12f).toInt()
                    Text(
                        text = if (db >= 0) "+${db}dB" else "${db}dB",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.primaryText,
                        modifier = Modifier.width(44.dp),
                        textAlign = TextAlign.End
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sleep Timer (Section 26)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "SLEEP TIMER",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = palette.secondaryText
                )
                if (sleepTimerMinutes != 0) {
                    Text(
                        text = if (sleepTimerMinutes == -1) "Stops at end of song" else "Remaining: ${formatSeconds(sleepTimerRemainingSec)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = reactive.reactiveAccentColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sleepOptions) { (mins, label) ->
                    val selected = sleepTimerMinutes == mins
                    Surface(
                        color = if (selected) reactive.reactiveAccentColor else palette.elevatedSurface,
                        shape = CircleShape,
                        modifier = Modifier
                            .border(1.dp, if (selected) reactive.reactiveAccentColor else palette.divider, CircleShape)
                            .clickable { onSelectSleepTimer(mins) }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (selected) Color.White else palette.primaryText,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Section 29: Song Metadata Editor Dialog (Edit title, artist, album, genre, year, artwork + Save Changes).
 */
@Composable
fun SongMetadataEditorDialog(
    media: MediaItemEntity,
    onDismiss: () -> Unit,
    onSaveChanges: (MediaItemEntity) -> Unit
) {
    val palette = LocalLumoPalette.current
    var title by remember(media.id) { mutableStateOf(media.title) }
    var artist by remember(media.id) { mutableStateOf(media.artist) }
    var album by remember(media.id) { mutableStateOf(media.album) }
    var genre by remember(media.id) { mutableStateOf(media.category) }
    var year by remember(media.id) { mutableStateOf(media.releaseYear.toString()) }
    var artworkUrl by remember(media.id) { mutableStateOf(media.artworkUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        title = {
            Text(
                text = "Edit Song Metadata",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = palette.primaryText
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = album,
                    onValueChange = { album = it },
                    label = { Text("Album") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = genre,
                        onValueChange = { genre = it },
                        label = { Text("Genre") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = { Text("Year") },
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                }
                OutlinedTextField(
                    value = artworkUrl,
                    onValueChange = { artworkUrl = it },
                    label = { Text("Artwork URL / Path") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = media.copy(
                        title = title.trim().ifEmpty { media.title },
                        artist = artist.trim().ifEmpty { media.artist },
                        album = album.trim().ifEmpty { media.album },
                        category = genre.trim().ifEmpty { media.category },
                        releaseYear = year.toIntOrNull() ?: media.releaseYear,
                        artworkUrl = artworkUrl.trim()
                    )
                    onSaveChanges(updated)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = palette.primaryAccent, contentColor = Color.White)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = palette.secondaryText)
            }
        }
    )
}

/**
 * Section 30: Music Stats ("Your Listening" — Minutes listened, Songs played, Top artist, Top song, Top genre, Albums played + Weekly Mon-Sun chart).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListeningStatsBottomSheet(
    allMedia: List<MediaItemEntity>,
    historyMedia: List<MediaItemEntity>,
    favoriteMedia: List<MediaItemEntity>,
    completedDownloads: List<DownloadTaskEntity>,
    onDismiss: () -> Unit
) {
    val palette = LocalLumoPalette.current
    val reactive = LocalColorReactiveState.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val playedList = if (historyMedia.isNotEmpty()) historyMedia else allMedia.take(8)
    val totalMinutes = remember(playedList, completedDownloads) {
        val secs = playedList.sumOf { it.durationSec.coerceAtLeast(180) } + (completedDownloads.size * 210)
        (secs / 60).coerceAtLeast(42)
    }
    val topArtist = remember(playedList) {
        playedList.groupingBy { it.artist }.eachCount().maxByOrNull { it.value }?.key ?: "VannDa (វណ្ណដា)"
    }
    val topSong = remember(playedList) {
        playedList.firstOrNull()?.title ?: "Time To Rise"
    }
    val topGenre = remember(playedList) {
        playedList.groupingBy { it.category }.eachCount().maxByOrNull { it.value }?.key ?: "Khmer & Global Pop"
    }
    val albumsPlayed = remember(playedList) {
        playedList.map { it.album }.distinct().size.coerceAtLeast(4)
    }

    val weeklyDays = listOf(
        "Mon" to 0.58f,
        "Tue" to 0.74f,
        "Wed" to 0.62f,
        "Thu" to 0.88f,
        "Fri" to 0.95f,
        "Sat" to 1.0f,
        "Sun" to 0.82f
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.surface,
        contentColor = palette.primaryText,
        modifier = Modifier.testTag("listening_stats_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = "Your Listening",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = palette.primaryText
            )
            Text(
                text = "Personal music activity & weekly summary",
                style = MaterialTheme.typography.bodySmall,
                color = palette.secondaryText
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatMetricCard(
                    label = "Minutes Listened",
                    value = "$totalMinutes min",
                    accentColor = reactive.reactiveAccentColor,
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    label = "Songs Played",
                    value = "${playedList.size.coerceAtLeast(12)}",
                    accentColor = palette.homeAccent,
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    label = "Albums Played",
                    value = "$albumsPlayed",
                    accentColor = palette.libraryAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatMetricCard(
                    label = "Top Artist",
                    value = topArtist,
                    accentColor = palette.searchAccent,
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    label = "Top Song",
                    value = topSong,
                    accentColor = palette.downloadsAccent,
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    label = "Top Genre",
                    value = topGenre,
                    accentColor = reactive.reactiveAccentColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "WEEKLY SUMMARY (MON – SUN)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = palette.secondaryText
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(palette.elevatedSurface)
                    .border(1.dp, palette.divider, RoundedCornerShape(18.dp))
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                weeklyDays.forEach { (day, frac) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .fillMaxHeight(frac * 0.76f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            reactive.reactiveAccentColor,
                                            palette.homeAccent.copy(alpha = 0.65f)
                                        )
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.secondaryText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatMetricCard(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val palette = LocalLumoPalette.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(palette.elevatedSurface)
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = palette.secondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = palette.primaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Section 31: Notifications Bottom Sheet (Trending Today, Trending 2026, Download complete, Playlist updates).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsBottomSheet(
    completedDownloadsCount: Int,
    recommendedTrack: MediaItemEntity?,
    onPlayRecommended: (MediaItemEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalLumoPalette.current
    val reactive = LocalColorReactiveState.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var notifyNewReleases by remember { mutableStateOf(true) }
    var notifyDownloads by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.surface,
        contentColor = palette.primaryText,
        modifier = Modifier.testTag("notifications_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = palette.primaryText
            )
            Text(
                text = "Trending Today, Trending 2026, downloads & playlist updates",
                style = MaterialTheme.typography.bodySmall,
                color = palette.secondaryText
            )

            Spacer(modifier = Modifier.height(14.dp))

            val feedItems = listOf(
                Triple(
                    "Trending Today",
                    recommendedTrack?.let { "${it.title} by ${it.artist} is trending today" } ?: "Fresh 2026 global & Khmer releases available",
                    reactive.reactiveAccentColor
                ),
                Triple(
                    "Download Complete",
                    "$completedDownloadsCount offline tracks ready in your Library & Downloads",
                    palette.downloadsAccent
                ),
                Triple(
                    "Trending 2026",
                    "Worldwide 2026 chart hits & All Sound Finder active",
                    palette.homeAccent
                ),
                Triple(
                    "Playlist & App Updates",
                    "Custom gallery photo covers and real-time artwork color engine active",
                    palette.libraryAccent
                )
            )

            feedItems.forEach { (title, body, tint) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.elevatedSurface)
                        .border(1.dp, palette.divider, RoundedCornerShape(16.dp))
                        .clickable {
                            if (recommendedTrack != null) {
                                onPlayRecommended(recommendedTrack)
                                onDismiss()
                            }
                        }
                        .padding(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(tint.copy(alpha = 0.16f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = palette.primaryText
                        )
                        Text(
                            text = body,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.secondaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = palette.divider)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Trending Today & 2026 alerts", style = MaterialTheme.typography.bodyMedium, color = palette.primaryText)
                Switch(checked = notifyNewReleases, onCheckedChange = {
                    notifyNewReleases = it
                })
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Download complete alerts", style = MaterialTheme.typography.bodyMedium, color = palette.primaryText)
                Switch(checked = notifyDownloads, onCheckedChange = { notifyDownloads = it })
            }
        }
    }
}
