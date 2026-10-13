package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.MediaItemEntity
import com.example.ui.theme.LocalColorReactiveState
import com.example.ui.theme.LocalLumoPalette
import com.example.ui.theme.LumoIcons
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun LumoMiniPlayer(
    media: MediaItemEntity?,
    isVisible: Boolean,
    isPlaying: Boolean,
    positionSec: Int,
    durationSec: Int,
    accentColor: Color,
    onTapExpand: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit = {},
    onSkipNext: () -> Unit = {},
    onToggleFavorite: (MediaItemEntity) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalLumoPalette.current
    val reactive = LocalColorReactiveState.current

    AnimatedVisibility(
        visible = isVisible && media != null,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
        modifier = modifier
    ) {
        if (media != null) {
            var dragOffsetX by remember(media.id) { mutableFloatStateOf(0f) }
            val heartScale by animateFloatAsState(
                targetValue = if (media.isFavorite) 1.16f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "miniHeartScale"
            )

            val mult = reactive.intensityMode.reactionMultiplier
            val reactiveAccent = lerp(accentColor, reactive.reactiveAccentColor, 0.72f)
            val playPulseScale = if (isPlaying) {
                1.0f + (reactive.beatPulse * 0.08f * mult)
            } else 1.0f

            // High-contrast floating card with subtle Color Reactive Engine atmosphere
            val dockContainerColor = if (palette.option.isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
            val dockTitleColor = if (palette.option.isDark) Color(0xFF0B0F17) else Color(0xFFF8FAFC)
            val dockSubColor = if (palette.option.isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
            val playCircleBg = if (palette.option.isDark) Color(0xFF121824) else Color.White
            val playIconTint = if (palette.option.isDark) Color.White else Color(0xFF0B0F17)

            val borderGlowAlpha = (0.22f + reactive.beatPulse * 0.34f * mult).coerceIn(0.16f, 0.62f)

            Surface(
                color = dockContainerColor,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 18.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .offset { IntOffset(dragOffsetX.roundToInt(), 0) }
                    .drawBehind {
                        // Subtle reactive ambient halo underneath the mini-player dock
                        if (isPlaying) {
                            drawRoundRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        reactive.dominantColor.copy(alpha = reactive.ambientGlowAlpha * 0.9f),
                                        reactiveAccent.copy(alpha = reactive.ambientGlowAlpha * 1.1f),
                                        reactive.secondaryColor.copy(alpha = reactive.ambientGlowAlpha * 0.9f)
                                    )
                                ),
                                cornerRadius = CornerRadius(26.dp.toPx(), 26.dp.toPx()),
                                size = Size(size.width, size.height)
                            )
                        }
                    }
                    .border(
                        width = 1.2.dp,
                        color = reactiveAccent.copy(alpha = borderGlowAlpha),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .pointerInput(media.id) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (abs(dragOffsetX) > 220f) {
                                    dragOffsetX = 0f
                                    onDismiss()
                                } else {
                                    dragOffsetX = 0f
                                }
                            },
                            onDragCancel = { dragOffsetX = 0f },
                            onHorizontalDrag = { _, dragAmount ->
                                dragOffsetX += dragAmount
                            }
                        )
                    }
                    .clickable(onClick = onTapExpand)
                    .testTag("mini_player_bar")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Smooth animated artwork crossfade when song changes
                        AnimatedContent(
                            targetState = media,
                            transitionSpec = {
                                (fadeIn(tween(520)) + scaleIn(initialScale = 0.90f, animationSpec = tween(520)))
                                    .togetherWith(fadeOut(tween(420)) + scaleOut(targetScale = 0.92f, animationSpec = tween(420)))
                            },
                            label = "miniPlayerArtworkTransition"
                        ) { targetMedia ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .scale(if (isPlaying) 1f + reactive.beatPulse * 0.045f * mult else 1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(
                                        width = 1.dp,
                                        color = reactiveAccent.copy(alpha = 0.45f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                            ) {
                                LumoArtworkImage(
                                    artworkUrl = targetMedia.artworkUrl,
                                    fallbackResId = targetMedia.artworkResId,
                                    contentDescription = "${targetMedia.title} artwork",
                                    modifier = Modifier.matchParentSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isPlaying) {
                                    MiniReactiveEqualizerBars(
                                        color = reactiveAccent,
                                        bassLevel = reactive.bassLevel,
                                        beatPulse = reactive.beatPulse
                                    )
                                    Spacer(modifier = Modifier.width(7.dp))
                                }
                                Text(
                                    text = media.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = dockTitleColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = media.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = dockSubColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { onToggleFavorite(media) },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("mini_player_favorite_button")
                        ) {
                            Icon(
                                imageVector = if (media.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (media.isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (media.isFavorite) Color(0xFFEF4444) else dockSubColor,
                                modifier = Modifier
                                    .size(20.dp)
                                    .scale(heartScale)
                            )
                        }

                        IconButton(
                            onClick = onSkipPrevious,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("mini_player_previous_button")
                        ) {
                            Icon(
                                imageVector = LumoIcons.SkipPrevious,
                                contentDescription = "Previous",
                                tint = dockTitleColor,
                                modifier = Modifier.size(21.dp)
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(42.dp)
                                .scale(playPulseScale)
                                .drawBehind {
                                    if (isPlaying) {
                                        drawCircle(
                                            color = reactiveAccent.copy(alpha = (0.20f + reactive.beatPulse * 0.25f) * mult),
                                            radius = size.minDimension * (0.54f + reactive.beatPulse * 0.12f * mult)
                                        )
                                    }
                                }
                                .clip(CircleShape)
                                .background(playCircleBg)
                                .clickable(onClick = onTogglePlayPause)
                                .testTag("mini_player_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) LumoIcons.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = playIconTint,
                                modifier = Modifier.size(21.dp)
                            )
                        }

                        IconButton(
                            onClick = onSkipNext,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("mini_player_next_button")
                        ) {
                            Icon(
                                imageVector = LumoIcons.SkipNext,
                                contentDescription = "Next",
                                tint = dockTitleColor,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }

                    val progress = if (durationSec > 0) {
                        (positionSec.toFloat() / durationSec.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    // Music-reactive gradient progress bar at bottom of mini-player
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.2.dp)
                    ) {
                        drawRect(
                            color = dockSubColor.copy(alpha = 0.18f),
                            size = size
                        )
                        val activeW = (size.width * progress).coerceIn(0f, size.width)
                        if (activeW > 0f) {
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        reactive.dominantColor,
                                        reactiveAccent
                                    ),
                                    startX = 0f,
                                    endX = activeW.coerceAtLeast(1f)
                                ),
                                size = Size(activeW, size.height)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniReactiveEqualizerBars(
    color: Color,
    bassLevel: Float,
    beatPulse: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "miniEq")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 680, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "miniEqPhase"
    )

    Canvas(modifier = Modifier.size(width = 14.dp, height = 13.dp)) {
        val barCount = 3
        val barW = 3.dp.toPx()
        val gap = ((size.width - barW * barCount) / (barCount - 1)).coerceAtLeast(1f)
        for (i in 0 until barCount) {
            val wave = ((sin(phase + i * 1.4f) + 1f) * 0.5f)
            val frac = (0.28f + wave * 0.45f + bassLevel * 0.15f + beatPulse * 0.12f).coerceIn(0.22f, 1.0f)
            val h = size.height * frac
            val left = i * (barW + gap)
            val top = size.height - h
            drawRoundRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(barW, h),
                cornerRadius = CornerRadius(barW / 2f, barW / 2f)
            )
        }
    }
}
