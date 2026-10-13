package com.example.ui.screens

import android.app.Activity
import android.app.PictureInPictureParams
import android.net.Uri
import android.os.Build
import android.util.Rational
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.LumoYouTubeStreamAuth
import com.example.data.MediaItemEntity
import com.example.media.PlayerPlaybackState
import com.example.ui.components.LumoArtworkImage
import com.example.ui.components.WaveformScrubber
import com.example.ui.theme.LocalColorReactiveState
import com.example.ui.theme.LocalLumoPalette
import com.example.ui.theme.LumoIcons
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun VideoPlayerScreen(
    playerState: PlayerPlaybackState,
    onClose: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onSeekToSec: (Int) -> Unit,
    onToggleSubtitles: () -> Unit,
    onChangeSpeed: (Float) -> Unit,
    onChangeBrightness: (Float) -> Unit,
    onChangeVolume: (Float) -> Unit,
    onToggleLandscapeLayout: () -> Unit,
    onSwitchToAudioPlayer: () -> Unit,
    onOpenDownloadSheet: (MediaItemEntity) -> Unit,
    onOpenTrackOptions: (MediaItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val palette = LocalLumoPalette.current
    val reactive = LocalColorReactiveState.current
    val context = LocalContext.current
    val media = playerState.currentMedia ?: return
    val accent = reactive.reactiveAccentColor

    var gestureHudLabel by remember { mutableStateOf<String?>(null) }

    val subtitles = remember(media.id) { media.parsedSubtitles() }
    val activeSubtitle = subtitles.find {
        playerState.positionSec in it.startSec..it.endSec
    }?.text ?: if (playerState.subtitlesEnabled) {
        "♪ ${media.title} — ${media.artist} ♪"
    } else null

    val infiniteTransition = rememberInfiniteTransition(label = "cinemaVisualizer")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cinemaWavePhase"
    )

    val waveformBars = remember(media.id) { media.generateWaveform(50) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .drawBehind {
                val glowAlpha = reactive.ambientGlowAlpha
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accent.copy(alpha = (glowAlpha * 0.90f).coerceIn(0.06f, 0.34f)),
                            reactive.dominantColor.copy(alpha = (glowAlpha * 1.1f).coerceIn(0.08f, 0.40f)),
                            palette.background
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.32f),
                        radius = size.maxDimension * (0.60f * reactive.beatScaleExpansion)
                    )
                )
            }
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .testTag("full_video_player_screen"),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Minimal Top Header (Matching Image 3 left)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("video_back_button")
            ) {
                Icon(
                    imageVector = LumoIcons.ChevronDown,
                    contentDescription = "Collapse video player",
                    tint = palette.primaryText,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Now Playing",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = palette.primaryText
                )
                Text(
                    text = "${media.category} · Cinema Mode",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.secondaryText
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onSwitchToAudioPlayer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = LumoIcons.MusicNote,
                        contentDescription = "Switch to Audio Player",
                        tint = palette.primaryText,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(
                    onClick = { onOpenTrackOptions(media) },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("video_more_button")
                ) {
                    Icon(
                        imageVector = LumoIcons.MoreHoriz,
                        contentDescription = "Video options",
                        tint = palette.primaryText,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Rich Widescreen / Square Visual Canvas (Always displays real high-res artwork + dynamic visualizer; never a blank black box!)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (playerState.isLandscapeVideoRequested) {
                        Modifier.weight(1f).padding(vertical = 8.dp)
                    } else {
                        Modifier.aspectRatio(16f / 10f)
                    }
                )
                .shadow(24.dp, RoundedCornerShape(28.dp), ambientColor = Color.Black, spotColor = accent)
                .clip(RoundedCornerShape(28.dp))
                .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(28.dp))
                .pointerInput(playerState.videoBrightness, playerState.videoVolume) {
                    detectVerticalDragGestures(
                        onDragEnd = { gestureHudLabel = null },
                        onDragCancel = { gestureHudLabel = null },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            val delta = -dragAmount / size.height.coerceAtLeast(1).toFloat()
                            if (change.position.x < size.width / 2f) {
                                val updated = (playerState.videoBrightness + delta).coerceIn(0.25f, 1.0f)
                                onChangeBrightness(updated)
                                gestureHudLabel = "Brightness ${(updated * 100).toInt()}%"
                            } else {
                                val updated = (playerState.videoVolume + delta).coerceIn(0.0f, 1.0f)
                                onChangeVolume(updated)
                                gestureHudLabel = "Volume ${(updated * 100).toInt()}%"
                            }
                        }
                    )
                }
                .testTag("video_viewport_box")
        ) {
            var isVideoSurfaceReady by remember(media.id) { mutableStateOf(false) }
            val videoStreamSource = remember(media.id, media.localFilePath, media.streamUrl) {
                media.effectiveVideoStreamUrlOrPath
            }
            val isActualVideoStream = remember(videoStreamSource) {
                val basePath = videoStreamSource.substringBefore("?").lowercase()
                basePath.endsWith(".mp4") || basePath.endsWith(".webm") || basePath.endsWith(".mkv")
            }

            LumoArtworkImage(
                artworkUrl = media.artworkUrl,
                fallbackResId = media.artworkResId,
                contentDescription = media.title,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(playerState.videoBrightness.coerceIn(0.35f, 1.0f))
            )

            if (isActualVideoStream) {
                var activeVideoView by remember(media.id) { mutableStateOf<VideoView?>(null) }

                DisposableEffect(media.id) {
                    onDispose {
                        runCatching { activeVideoView?.stopPlayback() }
                        activeVideoView = null
                    }
                }

                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            activeVideoView = this
                            setOnPreparedListener { mp ->
                                runCatching {
                                    mp.setVolume(0f, 0f)
                                    mp.isLooping = playerState.repeatMode == "ONE"
                                    val targetMs = playerState.positionSec * 1000
                                    if (targetMs > 1000) {
                                        mp.seekTo(targetMs)
                                    }
                                    if (playerState.isPlaying) {
                                        start()
                                    }
                                    isVideoSurfaceReady = true
                                }
                            }
                            setOnErrorListener { _, _, _ ->
                                isVideoSurfaceReady = false
                                true
                            }
                            runCatching {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                                    (videoStreamSource.startsWith("http://") || videoStreamSource.startsWith("https://"))
                                ) {
                                    setVideoURI(Uri.parse(videoStreamSource), LumoYouTubeStreamAuth.getHeadersForUrl())
                                } else {
                                    setVideoURI(Uri.parse(videoStreamSource))
                                }
                            }
                        }
                    },
                    update = { view ->
                        if (isVideoSurfaceReady) {
                            runCatching {
                                if (playerState.isPlaying && !view.isPlaying) {
                                    view.start()
                                } else if (!playerState.isPlaying && view.isPlaying) {
                                    view.pause()
                                }
                                val targetMs = playerState.positionSec * 1000
                                if (abs(view.currentPosition - targetMs) > 2500) {
                                    view.seekTo(targetMs)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(
                            if (isVideoSurfaceReady) {
                                playerState.videoBrightness.coerceIn(0.35f, 1.0f)
                            } else 0f
                        )
                )
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.18f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.72f)
                        )
                    )
                )
                if (playerState.isPlaying) {
                    val count = 32
                    val slot = size.width / count
                    val barW = (slot * 0.48f).coerceAtLeast(3f)
                    for (i in 0 until count) {
                        val amp = (0.16f + 0.52f * abs(sin(wavePhase + i * 0.42f)))
                        val h = size.height * 0.22f * amp
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.55f),
                            topLeft = Offset(i * slot + (slot - barW) / 2f, size.height - h - 10f),
                            size = Size(barW, h),
                            cornerRadius = CornerRadius(barW / 2f, barW / 2f)
                        )
                    }
                }
            }

            // Top-left HD Video badge
            Surface(
                color = Color.Black.copy(alpha = 0.62f),
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = LumoIcons.YouTubePlay,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (media.id.startsWith("yt_")) "YOUTUBE HD" else "HD VIDEO",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            if (playerState.subtitlesEnabled && activeSubtitle != null) {
                Surface(
                    color = Color.Black.copy(alpha = 0.68f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                        .testTag("video_subtitle_box")
                ) {
                    Text(
                        text = activeSubtitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            if (gestureHudLabel != null) {
                Surface(
                    color = Color.Black.copy(alpha = 0.82f),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 14.dp)
                ) {
                    Text(
                        text = gestureHudLabel!!,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Clean Metadata + Controls Column (Matching Image 3 left)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = media.title,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 23.sp
                ),
                color = palette.primaryText,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = media.artist,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                color = palette.secondaryText,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Clean Waveform Scrubber instead of broken Slider
            WaveformScrubber(
                waveformAmplitudes = waveformBars,
                positionSec = playerState.positionSec,
                durationSec = playerState.durationSec,
                isPlaying = playerState.isPlaying,
                accentColor = accent,
                onSeekToSec = onSeekToSec,
                modifier = Modifier.testTag("video_seek_slider")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Clean Transport Row with Glowing White Circular Play Button
            val playBtnBg = if (palette.option.isDark) Color.White else palette.primaryText
            val playBtnIconTint = if (palette.option.isDark) Color(0xFF0B0F17) else Color.White

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleSubtitles,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("video_cc_button")
                ) {
                    Icon(
                        imageVector = LumoIcons.ClosedCaption,
                        contentDescription = "Toggle Subtitles",
                        tint = if (playerState.subtitlesEnabled) palette.primaryText else palette.secondaryText.copy(alpha = 0.55f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = onSkipPrevious,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = LumoIcons.SkipPrevious,
                        contentDescription = "Previous",
                        tint = palette.primaryText,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(82.dp)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        playBtnBg.copy(alpha = 0.36f),
                                        playBtnBg.copy(alpha = 0.10f),
                                        Color.Transparent
                                    )
                                ),
                                radius = size.minDimension * 0.62f
                            )
                        }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(66.dp)
                            .shadow(16.dp, CircleShape, ambientColor = playBtnBg, spotColor = playBtnBg)
                            .clip(CircleShape)
                            .background(playBtnBg)
                            .clickable(onClick = onTogglePlayPause)
                            .testTag("video_play_pause_button")
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
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = LumoIcons.SkipNext,
                        contentDescription = "Next",
                        tint = palette.primaryText,
                        modifier = Modifier.size(30.dp)
                    )
                }

                IconButton(
                    onClick = { onOpenDownloadSheet(media) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = LumoIcons.Download,
                        contentDescription = "Download MP4 / MP3",
                        tint = palette.downloadsAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Clean, well-spaced utility pills row (no overlap!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = palette.elevatedSurface,
                    shape = CircleShape,
                    modifier = Modifier
                        .border(1.dp, palette.divider, CircleShape)
                        .clickable {
                            val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                            val idx = speeds.indexOf(playerState.playbackSpeed)
                            onChangeSpeed(speeds[(idx + 1) % speeds.size])
                        }
                        .testTag("video_speed_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Icon(
                            imageVector = LumoIcons.Speed,
                            contentDescription = "Speed",
                            tint = palette.secondaryText,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${playerState.playbackSpeed}x",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.primaryText
                        )
                    }
                }

                Surface(
                    color = palette.elevatedSurface,
                    shape = CircleShape,
                    modifier = Modifier
                        .border(1.dp, palette.divider, CircleShape)
                        .clickable(onClick = onToggleLandscapeLayout)
                        .testTag("video_fullscreen_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Icon(
                            imageVector = if (playerState.isLandscapeVideoRequested) LumoIcons.ScreenRotation else LumoIcons.Fullscreen,
                            contentDescription = "Expand Video",
                            tint = palette.secondaryText,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (playerState.isLandscapeVideoRequested) "Fit" else "Expand",
                            style = MaterialTheme.typography.labelMedium,
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
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                runCatching {
                                    val activity = context as? Activity
                                    val params = PictureInPictureParams.Builder()
                                        .setAspectRatio(Rational(16, 9))
                                        .build()
                                    activity?.enterPictureInPictureMode(params)
                                }
                            }
                        }
                        .testTag("video_pip_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Icon(
                            imageVector = LumoIcons.PictureInPictureAlt,
                            contentDescription = "PiP",
                            tint = palette.secondaryText,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "PiP",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.primaryText
                        )
                    }
                }

                Surface(
                    color = palette.downloadsAccent.copy(alpha = 0.16f),
                    shape = CircleShape,
                    modifier = Modifier
                        .border(1.dp, palette.downloadsAccent.copy(alpha = 0.45f), CircleShape)
                        .clickable { onOpenDownloadSheet(media) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Icon(
                            imageVector = LumoIcons.Download,
                            contentDescription = "Download MP4",
                            tint = palette.downloadsAccent,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "MP4 / MP3",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = palette.downloadsAccent
                        )
                    }
                }
            }
        }
    }
}
