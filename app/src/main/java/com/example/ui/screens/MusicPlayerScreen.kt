package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import android.content.Intent
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MediaItemEntity
import com.example.media.PlayerPlaybackState
import com.example.ui.components.LumoArtworkImage
import com.example.ui.components.WaveformScrubber
import com.example.ui.theme.LocalColorReactiveState
import com.example.ui.theme.LocalLumoPalette
import com.example.ui.theme.LumoIcons
import kotlin.math.sin

@Composable
fun MusicPlayerScreen(
    playerState: PlayerPlaybackState,
    onClose: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onSeekToSec: (Int) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: (MediaItemEntity) -> Unit,
    onOpenDownloadSheet: (MediaItemEntity) -> Unit,
    onOpenTrackOptions: (MediaItemEntity) -> Unit,
    onOpenAddToPlaylist: (MediaItemEntity) -> Unit = {},
    onSelectQueueTrack: (MediaItemEntity) -> Unit = {},
    onMoveQueueItem: (Int, Int) -> Unit = { _, _ -> },
    onRemoveQueueItem: (String) -> Unit = {},
    onClearQueue: () -> Unit = {},
    onToggleVisualizer: () -> Unit = {},
    onSelectEqPreset: (String) -> Unit = {},
    onSelectSleepTimer: (Int) -> Unit = {},
    onSwitchToVideoPlayer: () -> Unit,
    onChangeSpeed: (Float) -> Unit,
    onCycleReactiveIntensity: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val palette = LocalLumoPalette.current
    val reactive = LocalColorReactiveState.current
    val media = playerState.currentMedia ?: return
    val trackAccent = reactive.reactiveAccentColor
    var showLockScreenPreview by remember { mutableStateOf(false) }
    var showLyricsSheet by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showEqAndSleepSheet by remember { mutableStateOf(false) }

    val basePlayScale by animateFloatAsState(
        targetValue = if (playerState.isPlaying) 1.0f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "heroArtworkScale"
    )
    // Beat reaction: subtle scale expansion on beat when visualizer is ON
    val vizMult = if (playerState.visualizerEnabled) 1f else 0f
    val artworkScale = basePlayScale * (1.0f + reactive.beatPulse * 0.032f * reactive.intensityMode.reactionMultiplier * vizMult)

    val heartScale by animateFloatAsState(
        targetValue = if (media.isFavorite) 1.18f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "playerHeartScale"
    )

    val waveformBars = remember(media.id) { media.generateWaveform(54) }

    val infiniteTransition = rememberInfiniteTransition(label = "ambientAuraDrift")
    val driftPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambientDriftPhase"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .drawBehind {
                // Real-Time Color Reactive Ambient Gradient:
                // - Dominant & Secondary colors extracted from artwork
                // - Bass controls glow intensity (ambientGlowAlpha)
                // - Beat expands radius gently (beatScaleExpansion)
                val glowAlpha = reactive.ambientGlowAlpha
                val driftX = sin(driftPhase) * size.width * 0.04f
                val primaryCenter = Offset(size.width * 0.5f + driftX, size.height * 0.29f)
                val primaryRadius = size.maxDimension * (0.58f * reactive.beatScaleExpansion)

                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            reactive.reactiveAccentColor.copy(alpha = (glowAlpha * 0.85f).coerceIn(0.05f, 0.36f)),
                            reactive.dominantColor.copy(alpha = (glowAlpha * 1.15f).coerceIn(0.08f, 0.44f)),
                            reactive.secondaryColor.copy(alpha = (glowAlpha * 0.65f).coerceIn(0.04f, 0.28f)),
                            palette.background
                        ),
                        center = primaryCenter,
                        radius = primaryRadius
                    )
                )

                // Subtle bottom secondary atmosphere
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            reactive.secondaryColor.copy(alpha = (glowAlpha * 0.55f).coerceIn(0.03f, 0.22f)),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f - driftX, size.height * 0.78f),
                        radius = size.maxDimension * 0.50f
                    )
                )
            }
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("full_music_player_screen")
    ) {
        val containerMaxWidth = this.maxWidth
        val containerMaxHeight = this.maxHeight
        val isLandscapeTwoColumn = containerMaxWidth > 620.dp && containerMaxWidth > containerMaxHeight

        if (isLandscapeTwoColumn) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(0.45f)
                        .fillMaxHeight()
                ) {
                    ReactiveArtworkStage(
                        media = media,
                        artDimension = 260.dp,
                        artworkScale = artworkScale,
                        driftPhase = driftPhase
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(0.55f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center
                ) {
                    PlayerTopBar(
                        media = media,
                        onClose = onClose,
                        onSwitchToVideoPlayer = onSwitchToVideoPlayer,
                        onToggleLockScreenPreview = { showLockScreenPreview = !showLockScreenPreview }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PlayerControlsAndMetadataColumn(
                        media = media,
                        playerState = playerState,
                        waveformBars = waveformBars,
                        trackAccent = trackAccent,
                        heartScale = heartScale,
                        showLockScreenPreview = showLockScreenPreview,
                        onToggleFavorite = { onToggleFavorite(media) },
                        onOpenDownloadSheet = { onOpenDownloadSheet(media) },
                        onOpenTrackOptions = { onOpenTrackOptions(media) },
                        onSwitchToVideoPlayer = onSwitchToVideoPlayer,
                        onSeekToSec = onSeekToSec,
                        onTogglePlayPause = onTogglePlayPause,
                        onSkipPrevious = onSkipPrevious,
                        onSkipNext = onSkipNext,
                        onToggleShuffle = onToggleShuffle,
                        onCycleRepeat = onCycleRepeat,
                        onChangeSpeed = onChangeSpeed,
                        onCycleReactiveIntensity = onCycleReactiveIntensity,
                        onOpenLyrics = { showLyricsSheet = true },
                        onOpenQueue = { showQueueSheet = true },
                        onOpenAddToPlaylist = { onOpenAddToPlaylist(media) },
                        onOpenEqAndSleep = { showEqAndSleepSheet = true },
                        onToggleVisualizer = onToggleVisualizer
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                PlayerTopBar(
                    media = media,
                    onClose = onClose,
                    onSwitchToVideoPlayer = onSwitchToVideoPlayer,
                    onToggleLockScreenPreview = { showLockScreenPreview = !showLockScreenPreview }
                )

                Spacer(modifier = Modifier.height(14.dp))

                val artDimension = (containerMaxWidth - 68.dp).coerceIn(232.dp, 304.dp)
                ReactiveArtworkStage(
                    media = media,
                    artDimension = artDimension,
                    artworkScale = artworkScale,
                    driftPhase = driftPhase
                )

                Spacer(modifier = Modifier.height(20.dp))

                PlayerControlsAndMetadataColumn(
                    media = media,
                    playerState = playerState,
                    waveformBars = waveformBars,
                    trackAccent = trackAccent,
                    heartScale = heartScale,
                    showLockScreenPreview = showLockScreenPreview,
                    onToggleFavorite = { onToggleFavorite(media) },
                    onOpenDownloadSheet = { onOpenDownloadSheet(media) },
                    onOpenTrackOptions = { onOpenTrackOptions(media) },
                    onSwitchToVideoPlayer = onSwitchToVideoPlayer,
                    onSeekToSec = onSeekToSec,
                    onTogglePlayPause = onTogglePlayPause,
                    onSkipPrevious = onSkipPrevious,
                    onSkipNext = onSkipNext,
                    onToggleShuffle = onToggleShuffle,
                    onCycleRepeat = onCycleRepeat,
                    onChangeSpeed = onChangeSpeed,
                    onCycleReactiveIntensity = onCycleReactiveIntensity,
                    onOpenLyrics = { showLyricsSheet = true },
                    onOpenQueue = { showQueueSheet = true },
                    onOpenAddToPlaylist = { onOpenAddToPlaylist(media) },
                    onOpenEqAndSleep = { showEqAndSleepSheet = true },
                    onToggleVisualizer = onToggleVisualizer
                )
            }
        }

        if (showLyricsSheet) {
            LyricsBottomSheet(
                media = media,
                onDismiss = { showLyricsSheet = false }
            )
        }

        if (showQueueSheet) {
            QueueBottomSheet(
                currentMedia = media,
                queue = playerState.queue.ifEmpty { listOf(media) },
                onDismiss = { showQueueSheet = false },
                onSelectTrack = { selected ->
                    onSelectQueueTrack(selected)
                    showQueueSheet = false
                },
                onMoveItem = onMoveQueueItem,
                onRemoveItem = onRemoveQueueItem,
                onClearQueue = onClearQueue
            )
        }

        if (showEqAndSleepSheet) {
            EqualizerAndSleepTimerSheet(
                currentEqPreset = playerState.eqPreset,
                sleepTimerMinutes = playerState.sleepTimerMinutes,
                sleepTimerRemainingSec = playerState.sleepTimerRemainingSec,
                visualizerEnabled = playerState.visualizerEnabled,
                onSelectEqPreset = onSelectEqPreset,
                onSelectSleepTimer = onSelectSleepTimer,
                onToggleVisualizer = onToggleVisualizer,
                onDismiss = { showEqAndSleepSheet = false }
            )
        }
    }
}

@Composable
private fun ReactiveArtworkStage(
    media: MediaItemEntity,
    artDimension: Dp,
    artworkScale: Float,
    driftPhase: Float
) {
    val palette = LocalLumoPalette.current
    val reactive = LocalColorReactiveState.current
    val mult = reactive.intensityMode.reactionMultiplier

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(artDimension)
    ) {
        // Acoustic breathing rings & artwork glow that respond to bass and beat detection
        Canvas(modifier = Modifier.fillMaxSize()) {
            val baseRadius = size.minDimension * 0.50f
            val glowRadius = baseRadius * (1.28f * reactive.beatScaleExpansion)
            val glowAlpha = (reactive.ambientGlowAlpha * 1.15f).coerceIn(0.08f, 0.42f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (palette.option.isDark) 0.14f + reactive.beatPulse * 0.08f * mult else 0.04f),
                        reactive.reactiveAccentColor.copy(alpha = glowAlpha),
                        reactive.dominantColor.copy(alpha = glowAlpha * 0.7f),
                        Color.Transparent
                    )
                ),
                radius = glowRadius
            )

            // Concentric breathing soundwave halo rings when playing
            if (reactive.isPlaying) {
                val ring1Radius = baseRadius * (0.98f + reactive.bassLevel * 0.14f * mult + reactive.beatPulse * 0.08f * mult)
                val ring2Radius = baseRadius * (1.06f + ((sin(driftPhase * 2f) + 1f) * 0.04f) + reactive.beatPulse * 0.12f * mult)
                drawCircle(
                    color = reactive.reactiveAccentColor.copy(alpha = (0.18f + reactive.beatPulse * 0.22f) * mult),
                    radius = ring1Radius,
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawCircle(
                    color = reactive.dominantColor.copy(alpha = (0.14f + reactive.bassLevel * 0.18f) * mult),
                    radius = ring2Radius,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }

        // Smooth 700ms crossfade + scale transition when song changes
        AnimatedContent(
            targetState = media,
            transitionSpec = {
                (fadeIn(animationSpec = tween(680)) + scaleIn(initialScale = 0.92f, animationSpec = tween(680)))
                    .togetherWith(fadeOut(animationSpec = tween(520)) + scaleOut(targetScale = 0.95f, animationSpec = tween(520)))
            },
            label = "heroArtworkAnimatedContent"
        ) { targetMedia ->
            val borderAlpha = (0.14f + reactive.beatPulse * 0.28f * mult).coerceIn(0.12f, 0.48f)
            val borderColor = lerp(Color.White.copy(alpha = 0.14f), reactive.reactiveAccentColor.copy(alpha = borderAlpha), 0.65f)

            LumoArtworkImage(
                artworkUrl = targetMedia.artworkUrl,
                fallbackResId = targetMedia.artworkResId,
                contentDescription = "${targetMedia.title} album art",
                modifier = Modifier
                    .fillMaxSize()
                    .scale(artworkScale)
                    .shadow(
                        elevation = 28.dp,
                        shape = RoundedCornerShape(32.dp),
                        ambientColor = reactive.dominantColor,
                        spotColor = reactive.reactiveAccentColor
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .border(
                        width = 1.2.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(32.dp)
                    )
                    .testTag("hero_player_artwork")
            )
        }
    }
}

@Composable
private fun PlayerTopBar(
    media: MediaItemEntity,
    onClose: () -> Unit,
    onSwitchToVideoPlayer: () -> Unit,
    onToggleLockScreenPreview: () -> Unit
) {
    val palette = LocalLumoPalette.current
    val reactive = LocalColorReactiveState.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(48.dp)
                .testTag("close_full_player_button")
        ) {
            Icon(
                imageVector = LumoIcons.ChevronDown,
                contentDescription = "Collapse player",
                tint = palette.primaryText,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .scale(if (reactive.isPlaying) 1f + reactive.beatPulse * 0.35f else 1f)
                        .clip(CircleShape)
                        .background(reactive.reactiveAccentColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Now Playing",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = palette.primaryText
                )
            }
            Text(
                text = "${media.album} · ${media.category}",
                style = MaterialTheme.typography.labelSmall,
                color = palette.secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onToggleLockScreenPreview,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("toggle_lockscreen_preview_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Lock-screen MediaSession preview",
                    tint = palette.secondaryText,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(
                onClick = onSwitchToVideoPlayer,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("switch_to_video_mode_button")
            ) {
                Icon(
                    imageVector = LumoIcons.QueueMusic,
                    contentDescription = "Video & Queue Mode",
                    tint = palette.primaryText,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun PlayerControlsAndMetadataColumn(
    media: MediaItemEntity,
    playerState: PlayerPlaybackState,
    waveformBars: List<Float>,
    trackAccent: Color,
    heartScale: Float,
    showLockScreenPreview: Boolean,
    onToggleFavorite: () -> Unit,
    onOpenDownloadSheet: () -> Unit,
    onOpenTrackOptions: () -> Unit,
    onSwitchToVideoPlayer: () -> Unit,
    onSeekToSec: (Int) -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onChangeSpeed: (Float) -> Unit,
    onCycleReactiveIntensity: () -> Unit,
    onOpenLyrics: () -> Unit = {},
    onOpenQueue: () -> Unit = {},
    onOpenAddToPlaylist: () -> Unit = {},
    onOpenEqAndSleep: () -> Unit = {},
    onToggleVisualizer: () -> Unit = {}
) {
    val palette = LocalLumoPalette.current
    val reactive = LocalColorReactiveState.current
    val context = LocalContext.current
    val mult = reactive.intensityMode.reactionMultiplier * (if (playerState.visualizerEnabled) 1f else 0f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Heart on Left | Centered Bold Title & Artist | More ••• on Right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("hero_favorite_button")
            ) {
                Icon(
                    imageVector = if (media.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (media.isFavorite) Color(0xFFEF4444) else palette.secondaryText,
                    modifier = Modifier
                        .size(24.dp)
                        .scale(heartScale)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = media.title,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = palette.primaryText,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = media.artist,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp
                    ),
                    color = palette.secondaryText,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onOpenTrackOptions,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("hero_more_options_button")
            ) {
                Icon(
                    imageVector = LumoIcons.MoreHoriz,
                    contentDescription = "More options",
                    tint = palette.secondaryText,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Symmetric Waveform Scrubber + Progress Bar (Music-reactive)
        WaveformScrubber(
            waveformAmplitudes = waveformBars,
            positionSec = playerState.positionSec,
            durationSec = playerState.durationSec,
            isPlaying = playerState.isPlaying,
            accentColor = trackAccent,
            onSeekToSec = onSeekToSec
        )

        Spacer(modifier = Modifier.height(22.dp))

        // Main Transport Row (Shuffle | Prev | Reactive Glowing White Circle Play | Next | Repeat)
        val playBtnBg = if (palette.option.isDark) Color.White else palette.primaryText
        val playBtnIconTint = if (palette.option.isDark) Color(0xFF0B0F17) else Color.White
        val haloScale = reactive.playButtonHaloScale

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selected control: Shuffle highlights with reactive accent when active
            IconButton(
                onClick = onToggleShuffle,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (playerState.isShuffleEnabled) reactive.reactiveAccentColor.copy(alpha = 0.16f)
                        else Color.Transparent
                    )
                    .testTag("hero_shuffle_button")
            ) {
                Icon(
                    imageVector = LumoIcons.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (playerState.isShuffleEnabled) reactive.reactiveAccentColor else palette.secondaryText.copy(alpha = 0.65f),
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(
                onClick = onSkipPrevious,
                modifier = Modifier
                    .size(52.dp)
                    .testTag("hero_previous_button")
            ) {
                Icon(
                    imageVector = LumoIcons.SkipPrevious,
                    contentDescription = "Previous track",
                    tint = palette.primaryText,
                    modifier = Modifier.size(30.dp)
                )
            }

            // Glowing White Circular Play/Pause Button with Beat-Reactive Aura
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(88.dp)
                    .drawBehind {
                        val auraColor = lerp(playBtnBg, reactive.reactiveAccentColor, 0.55f)
                        val outerAlpha = (0.28f + reactive.beatPulse * 0.24f * mult).coerceIn(0.18f, 0.54f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    auraColor.copy(alpha = outerAlpha),
                                    reactive.dominantColor.copy(alpha = outerAlpha * 0.45f),
                                    Color.Transparent
                                )
                            ),
                            radius = size.minDimension * 0.54f * haloScale
                        )
                    }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(68.dp)
                        .scale(1.0f + reactive.beatPulse * 0.055f * mult)
                        .shadow(
                            elevation = 18.dp,
                            shape = CircleShape,
                            ambientColor = reactive.reactiveAccentColor,
                            spotColor = playBtnBg
                        )
                        .clip(CircleShape)
                        .background(playBtnBg)
                        .clickable(onClick = onTogglePlayPause)
                        .testTag("hero_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (playerState.isPlaying) LumoIcons.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                        tint = playBtnIconTint,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            IconButton(
                onClick = onSkipNext,
                modifier = Modifier
                    .size(52.dp)
                    .testTag("hero_next_button")
            ) {
                Icon(
                    imageVector = LumoIcons.SkipNext,
                    contentDescription = "Next track",
                    tint = palette.primaryText,
                    modifier = Modifier.size(30.dp)
                )
            }

            // Selected control: Repeat highlights with reactive accent when active
            IconButton(
                onClick = onCycleRepeat,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (playerState.repeatMode != "OFF") reactive.reactiveAccentColor.copy(alpha = 0.16f)
                        else Color.Transparent
                    )
                    .testTag("hero_repeat_button")
            ) {
                Icon(
                    imageVector = if (playerState.repeatMode == "ONE") LumoIcons.RepeatOne else LumoIcons.Repeat,
                    contentDescription = "Repeat mode ${playerState.repeatMode}",
                    tint = if (playerState.repeatMode != "OFF") reactive.reactiveAccentColor else palette.secondaryText.copy(alpha = 0.65f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 10 Additional Actions Row: Lyrics | Queue | Add to Playlist | Share
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerActionIconPill(
                label = "Lyrics",
                onClick = onOpenLyrics,
                testTag = "hero_lyrics_button"
            )
            PlayerActionIconPill(
                label = "Queue (${playerState.queue.size.coerceAtLeast(1)})",
                onClick = onOpenQueue,
                testTag = "hero_queue_button"
            )
            PlayerActionIconPill(
                label = "+ Playlist",
                onClick = onOpenAddToPlaylist,
                testTag = "hero_add_playlist_button"
            )
            PlayerActionIconPill(
                label = "Share",
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "${media.title} — ${media.artist}")
                        putExtra(Intent.EXTRA_TEXT, "Listening to ${media.title} by ${media.artist} on LUMO — Discover. Play. Save.")
                    }
                    runCatching {
                        context.startActivity(Intent.createChooser(shareIntent, "Share Song"))
                    }
                },
                testTag = "hero_share_button"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Secondary utility bar (Download, Visualizer ON/OFF, EQ/Timer, Aura, Video, Speed)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (media.isAuthorizedDownload) {
                Surface(
                    color = palette.elevatedSurface,
                    shape = CircleShape,
                    modifier = Modifier
                        .border(1.dp, palette.divider, CircleShape)
                        .clickable(onClick = onOpenDownloadSheet)
                        .testTag("hero_download_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Icon(
                            imageVector = LumoIcons.Download,
                            contentDescription = "Download",
                            tint = palette.primaryText,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.primaryText
                        )
                    }
                }
            }

            // Visualizer ON/OFF toggle pill (Section 11)
            Surface(
                color = if (playerState.visualizerEnabled) reactive.reactiveAccentColor.copy(alpha = 0.22f) else palette.elevatedSurface,
                shape = CircleShape,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = if (playerState.visualizerEnabled) reactive.reactiveAccentColor else palette.divider,
                        shape = CircleShape
                    )
                    .clickable(onClick = onToggleVisualizer)
                    .testTag("hero_visualizer_toggle_pill")
            ) {
                Text(
                    text = if (playerState.visualizerEnabled) "Visualizer ON" else "Visualizer OFF",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (playerState.visualizerEnabled) reactive.reactiveAccentColor else palette.secondaryText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                )
            }

            // EQ & Sleep Timer pill (Section 26 & 27)
            Surface(
                color = palette.elevatedSurface,
                shape = CircleShape,
                modifier = Modifier
                    .border(1.dp, palette.divider, CircleShape)
                    .clickable(onClick = onOpenEqAndSleep)
                    .testTag("hero_eq_timer_pill")
            ) {
                val timerBadge = if (playerState.sleepTimerMinutes != 0) " · ⏱" else ""
                Text(
                    text = "EQ: ${playerState.eqPreset}$timerBadge",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.primaryText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                )
            }

            // Color Reactive Intensity Mode Selector Pill (LOW / MEDIUM / HIGH)
            Surface(
                color = reactive.dominantColor.copy(alpha = 0.32f),
                shape = CircleShape,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = reactive.reactiveAccentColor.copy(alpha = 0.48f),
                        shape = CircleShape
                    )
                    .clickable(onClick = onCycleReactiveIntensity)
                    .testTag("hero_reactive_intensity_pill")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .scale(1f + reactive.beatPulse * 0.35f)
                            .clip(CircleShape)
                            .background(reactive.reactiveAccentColor)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = reactive.intensityMode.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.primaryText
                    )
                }
            }

            Surface(
                color = palette.elevatedSurface,
                shape = CircleShape,
                modifier = Modifier
                    .border(1.dp, palette.divider, CircleShape)
                    .clickable {
                        val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                        val idx = speeds.indexOf(playerState.playbackSpeed)
                        val nextSpeed = speeds[(idx + 1) % speeds.size]
                        onChangeSpeed(nextSpeed)
                    }
                    .testTag("hero_speed_pill")
            ) {
                Text(
                    text = "${playerState.playbackSpeed}x",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (playerState.playbackSpeed != 1.0f) reactive.reactiveAccentColor else palette.secondaryText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                )
            }
        }

        if (showLockScreenPreview) {
            Spacer(modifier = Modifier.height(14.dp))
            LockScreenMediaSessionCard(
                media = media,
                isPlaying = playerState.isPlaying,
                onSkipPrevious = onSkipPrevious,
                onTogglePlayPause = onTogglePlayPause,
                onSkipNext = onSkipNext
            )
        }
    }
}

@Composable
private fun PlayerActionIconPill(
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    val palette = LocalLumoPalette.current
    Surface(
        color = palette.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .border(1.dp, palette.divider, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = palette.primaryText,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun LockScreenMediaSessionCard(
    media: MediaItemEntity,
    isPlaying: Boolean,
    onSkipPrevious: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit
) {
    val palette = LocalLumoPalette.current
    val reactive = LocalColorReactiveState.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(palette.surface)
            .border(1.dp, reactive.reactiveAccentColor.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
            .padding(16.dp)
            .testTag("lock_screen_media_session_card")
    ) {
        Text(
            text = "ANDROID LOCK-SCREEN MEDIA SESSION",
            style = MaterialTheme.typography.labelSmall,
            color = reactive.reactiveAccentColor
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            LumoArtworkImage(
                artworkUrl = media.artworkUrl,
                fallbackResId = media.artworkResId,
                contentDescription = media.title,
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = media.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.primaryText,
                    maxLines = 1
                )
                Text(
                    text = media.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.secondaryText
                )
            }
            IconButton(onClick = onSkipPrevious) {
                Icon(LumoIcons.SkipPrevious, contentDescription = "Previous", tint = palette.primaryText)
            }
            IconButton(onClick = onTogglePlayPause) {
                Icon(
                    imageVector = if (isPlaying) LumoIcons.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = reactive.reactiveAccentColor
                )
            }
            IconButton(onClick = onSkipNext) {
                Icon(LumoIcons.SkipNext, contentDescription = "Next", tint = palette.primaryText)
            }
        }
    }
}
