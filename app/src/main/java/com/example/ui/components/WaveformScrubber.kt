package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.formatSeconds
import com.example.ui.theme.LocalColorReactiveState
import com.example.ui.theme.LocalLumoPalette
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun WaveformScrubber(
    waveformAmplitudes: List<Float>,
    positionSec: Int,
    durationSec: Int,
    isPlaying: Boolean,
    accentColor: Color,
    onSeekToSec: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalLumoPalette.current
    val reactive = LocalColorReactiveState.current
    val safeDuration = durationSec.coerceAtLeast(1)
    val progress = (positionSec.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)

    val infiniteTransition = rememberInfiniteTransition(label = "waveformBreath")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveformPhase"
    )

    val bars = remember(waveformAmplitudes) {
        if (waveformAmplitudes.isEmpty()) List(52) { 0.4f } else waveformAmplitudes
    }

    val reactiveAccent = reactive.reactiveAccentColor
    val bassLevel = reactive.bassLevel
    val beatPulse = reactive.beatPulse
    val energyLevel = reactive.energyLevel
    val mult = reactive.intensityMode.reactionMultiplier

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .pointerInput(safeDuration) {
                    detectTapGestures { offset ->
                        val fraction = (offset.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                        onSeekToSec((fraction * safeDuration).toInt())
                    }
                }
                .pointerInput(safeDuration) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                        val fraction = (change.position.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                        onSeekToSec((fraction * safeDuration).toInt())
                    }
                }
                .testTag("waveform_scrubber_canvas")
        ) {
            val count = bars.size
            val totalWidth = size.width
            val waveformAreaHeight = size.height - 10.dp.toPx()
            val centerY = waveformAreaHeight / 2f
            val slotWidth = totalWidth / count.toFloat()
            val barWidth = (slotWidth * 0.48f).coerceIn(2.2f, 5.5f)
            val playheadIndex = (progress * count).toInt()

            bars.forEachIndexed { index, baseAmp ->
                val isPlayed = index <= playheadIndex
                val distanceToPlayhead = abs(index - playheadIndex)

                // Taper edges like the symmetric waveform (••••|l|l||l|••••)
                val edgeDistance = minOf(index, count - 1 - index).toFloat()
                val edgeEnvelope = (edgeDistance / 7f).coerceIn(0.14f, 1f)

                // Real-time music-reactive wave + beat expansion near the playhead and across played bars
                val waveRipple = if (isPlaying) {
                    val proximityFactor = (1f - (distanceToPlayhead / 10f).coerceIn(0f, 1f))
                    val harmonicWave = sin(phase + index * 0.55f) * (0.10f + energyLevel * 0.12f)
                    val beatLift = beatPulse * 0.22f * (0.35f + proximityFactor * 0.65f)
                    val bassLift = bassLevel * 0.12f * edgeEnvelope
                    (harmonicWave * proximityFactor + beatLift + bassLift) * mult
                } else 0f

                val effectiveAmp = ((baseAmp * edgeEnvelope) + waveRipple).coerceIn(0.10f, 0.98f)
                val barHeight = (waveformAreaHeight * 0.88f * effectiveAmp).coerceAtLeast(4f)
                val left = index * slotWidth + (slotWidth - barWidth) / 2f
                val top = centerY - barHeight / 2f

                val basePlayedColor = if (palette.option.isDark) Color.White else accentColor
                val colorBlend = ((beatPulse * 0.45f + energyLevel * 0.30f) * mult).coerceIn(0.12f, 0.72f)
                val reactivePlayedColor = lerp(basePlayedColor, reactiveAccent, colorBlend)

                val barColor = when {
                    index == playheadIndex -> lerp(Color.White, reactiveAccent, 0.35f)
                    isPlayed -> {
                        val nearPlayhead = (1f - (distanceToPlayhead / 8f).coerceIn(0f, 1f))
                        lerp(basePlayedColor.copy(alpha = 0.90f), reactivePlayedColor, nearPlayhead * 0.75f + 0.25f)
                    }
                    else -> lerp(
                        palette.secondaryText.copy(alpha = 0.26f),
                        reactive.dominantColor.copy(alpha = 0.48f),
                        0.35f * mult
                    )
                }

                // Subtle glow behind playhead bar on beat
                if (index == playheadIndex && isPlaying) {
                    drawRoundRect(
                        color = reactiveAccent.copy(alpha = (0.22f + beatPulse * 0.28f) * mult),
                        topLeft = Offset(left - 2.dp.toPx(), top - 3.dp.toPx()),
                        size = Size(barWidth + 4.dp.toPx(), barHeight + 6.dp.toPx()),
                        cornerRadius = CornerRadius(barWidth, barWidth)
                    )
                }

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }

            // Sleek reactive progress bar track underneath the waveform
            val trackY = size.height - 3.dp.toPx()
            val trackHeight = (2.2f + beatPulse * 1.1f * mult).dp.toPx()
            drawRoundRect(
                color = palette.secondaryText.copy(alpha = 0.18f),
                topLeft = Offset(0f, trackY - trackHeight / 2f),
                size = Size(totalWidth, trackHeight),
                cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)
            )

            val playedWidth = (totalWidth * progress).coerceIn(0f, totalWidth)
            if (playedWidth > 1f) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            reactive.dominantColor,
                            reactiveAccent,
                            Color.White
                        ),
                        startX = 0f,
                        endX = playedWidth
                    ),
                    topLeft = Offset(0f, trackY - trackHeight / 2f),
                    size = Size(playedWidth, trackHeight),
                    cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)
                )
                // Glowing playhead bead on the progress line
                val dotRadius = (3.5f + beatPulse * 2.2f * mult).dp.toPx()
                drawCircle(
                    color = reactiveAccent.copy(alpha = 0.38f),
                    radius = dotRadius * 1.8f,
                    center = Offset(playedWidth, trackY)
                )
                drawCircle(
                    color = Color.White,
                    radius = dotRadius * 0.85f,
                    center = Offset(playedWidth, trackY)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatSeconds(positionSec),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = palette.primaryText.copy(alpha = 0.88f)
            )
            Text(
                text = formatSeconds(safeDuration),
                style = MaterialTheme.typography.labelMedium,
                color = palette.secondaryText.copy(alpha = 0.68f)
            )
        }
    }
}
