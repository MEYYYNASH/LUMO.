package com.example.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.ColorUtils
import com.example.data.MediaItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

enum class ColorReactiveIntensity(
    val id: String,
    val label: String,
    val description: String,
    val reactionMultiplier: Float
) {
    LOW(
        id = "LOW",
        label = "Low",
        description = "Very subtle ambient glow & calm beat response",
        reactionMultiplier = 0.36f
    ),
    MEDIUM(
        id = "MEDIUM",
        label = "Medium",
        description = "Balanced waveform, glow, and beat response (Default)",
        reactionMultiplier = 0.72f
    ),
    HIGH(
        id = "HIGH",
        label = "High",
        description = "More noticeable music-reactive atmosphere while staying elegant",
        reactionMultiplier = 1.0f
    );

    fun next(): ColorReactiveIntensity = when (this) {
        LOW -> MEDIUM
        MEDIUM -> HIGH
        HIGH -> LOW
    }

    companion object {
        fun fromId(id: String?): ColorReactiveIntensity =
            entries.find { it.id.equals(id, ignoreCase = true) } ?: MEDIUM
    }
}

data class ArtworkExtractedPalette(
    val dominantColor: Color,
    val secondaryColor: Color,
    val accentColor: Color
)

@Stable
data class LumoColorReactiveState(
    val dominantColor: Color = Color(0xFF1E293B),
    val secondaryColor: Color = Color(0xFF172033),
    val accentColor: Color = Color(0xFF60A5FA),
    val reactiveAccentColor: Color = Color(0xFF60A5FA),
    val bassLevel: Float = 0.18f,
    val beatPulse: Float = 0.0f,
    val energyLevel: Float = 0.25f,
    val isBeatActive: Boolean = false,
    val intensityMode: ColorReactiveIntensity = ColorReactiveIntensity.MEDIUM,
    val isPlaying: Boolean = false
) {
    /**
     * Ambient glow alpha controlled by bass + beatPulse and scaled by intensity mode.
     * Quiet sections reduce visual intensity; beats gently expand the aura.
     */
    val ambientGlowAlpha: Float
        get() {
            val mult = intensityMode.reactionMultiplier
            val base = if (isPlaying) 0.14f else 0.08f
            val bassBoost = bassLevel * 0.16f
            val beatBoost = beatPulse * 0.12f
            return (base + (bassBoost + beatBoost) * mult.coerceIn(0.3f, 1f)).coerceIn(0.06f, 0.42f)
        }

    /**
     * Subtle artwork / halo expansion factor on beats (Normal = 1.0f, Beat = up to 1.075f).
     */
    val beatScaleExpansion: Float
        get() = 1.0f + (beatPulse * 0.07f)

    /**
     * Play button halo scale (expands gently on beat, smoothly settles back).
     */
    val playButtonHaloScale: Float
        get() = 1.0f + (beatPulse * 0.16f) + (bassLevel * 0.05f)
}

val LocalColorReactiveState = compositionLocalOf { LumoColorReactiveState() }

private object ArtworkPaletteCache {
    private val cache = ConcurrentHashMap<String, ArtworkExtractedPalette>()
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    suspend fun getOrExtractPalette(
        context: Context,
        media: MediaItemEntity
    ): ArtworkExtractedPalette = withContext(Dispatchers.IO) {
        cache[media.id]?.let { return@withContext it }

        val seedPalette = buildSeedPaletteFromHex(media.accentHex, media.waveformSeed)
        val bitmap: Bitmap? = runCatching {
            if (media.artworkUrl.isNotBlank()) {
                val req = Request.Builder()
                    .url(media.artworkUrl)
                    .header("User-Agent", "Mozilla/5.0")
                    .get()
                    .build()
                httpClient.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val bytes = resp.body?.bytes()
                        if (bytes != null && bytes.isNotEmpty()) {
                            val opts = BitmapFactory.Options().apply { inSampleSize = 4 }
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
                        } else null
                    } else null
                }
            } else {
                val opts = BitmapFactory.Options().apply { inSampleSize = 4 }
                BitmapFactory.decodeResource(context.resources, media.artworkResId, opts)
            }
        }.getOrNull()

        val extracted = if (bitmap != null) {
            val res = extractReadableDarkPaletteFromBitmap(bitmap, seedPalette)
            runCatching { bitmap.recycle() }
            res
        } else {
            seedPalette
        }

        cache[media.id] = extracted
        extracted
    }

    private fun extractReadableDarkPaletteFromBitmap(
        bitmap: Bitmap,
        fallback: ArtworkExtractedPalette
    ): ArtworkExtractedPalette = runCatching {
        val scaled = Bitmap.createScaledBitmap(bitmap, 32, 32, true)
        val width = scaled.width
        val height = scaled.height
        val pixels = IntArray(width * height)
        scaled.getPixels(pixels, 0, width, 0, 0, width, height)
        if (scaled !== bitmap) {
            runCatching { scaled.recycle() }
        }

        // 12 hue buckets (30 degrees each)
        val bucketWeights = FloatArray(12)
        val bucketHueSum = FloatArray(12)
        val bucketSatSum = FloatArray(12)
        val bucketLumSum = FloatArray(12)
        val hsl = FloatArray(3)

        for (argb in pixels) {
            val r = (argb shr 16) and 0xFF
            val g = (argb shr 8) and 0xFF
            val b = argb and 0xFF
            ColorUtils.RGBToHSL(r, g, b, hsl)
            val hue = hsl[0]
            val sat = hsl[1]
            val lum = hsl[2]

            if (lum < 0.08f || lum > 0.92f || sat < 0.12f) continue

            val bucket = ((hue / 30f).toInt()).coerceIn(0, 11)
            val weight = sat * (1f - abs(lum - 0.5f))
            bucketWeights[bucket] += weight
            bucketHueSum[bucket] += hue * weight
            bucketSatSum[bucket] += sat * weight
            bucketLumSum[bucket] += lum * weight
        }

        val sortedBuckets = (0 until 12)
            .filter { bucketWeights[it] > 0.05f }
            .sortedByDescending { bucketWeights[it] }

        if (sortedBuckets.isEmpty()) return@runCatching fallback

        val domBucket = sortedBuckets[0]
        val domW = bucketWeights[domBucket]
        val domHue = (bucketHueSum[domBucket] / domW).coerceIn(0f, 360f)
        val domSat = (bucketSatSum[domBucket] / domW).coerceIn(0.36f, 0.72f)
        val domLum = (bucketLumSum[domBucket] / domW).coerceIn(0.22f, 0.36f)

        val secBucket = sortedBuckets.firstOrNull { b ->
            val h = bucketHueSum[b] / bucketWeights[b]
            val diff = abs(h - domHue)
            minOf(diff, 360f - diff) >= 28f
        } ?: domBucket

        val secW = bucketWeights[secBucket]
        val secHue = if (secBucket != domBucket) {
            (bucketHueSum[secBucket] / secW).coerceIn(0f, 360f)
        } else {
            (domHue + 38f) % 360f
        }
        val secSat = (bucketSatSum[secBucket] / secW).coerceIn(0.30f, 0.64f)
        val secLum = 0.24f

        val accentHue = domHue
        val accentSat = (domSat * 1.12f).coerceIn(0.45f, 0.78f)
        val accentLum = 0.64f

        ArtworkExtractedPalette(
            dominantColor = Color(ColorUtils.HSLToColor(floatArrayOf(domHue, domSat, domLum))),
            secondaryColor = Color(ColorUtils.HSLToColor(floatArrayOf(secHue, secSat, secLum))),
            accentColor = Color(ColorUtils.HSLToColor(floatArrayOf(accentHue, accentSat, accentLum)))
        )
    }.getOrDefault(fallback)

    fun buildSeedPaletteFromHex(accentHex: Long, seed: Int): ArtworkExtractedPalette {
        val baseColor = Color(accentHex)
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(baseColor.toArgb(), hsl)
        val hue = hsl[0]
        val secHue = (hue + 35f + (seed % 25)) % 360f

        val dominant = Color(ColorUtils.HSLToColor(floatArrayOf(hue, 0.56f, 0.28f)))
        val secondary = Color(ColorUtils.HSLToColor(floatArrayOf(secHue, 0.48f, 0.22f)))
        val accent = Color(ColorUtils.HSLToColor(floatArrayOf(hue, 0.68f, 0.65f)))
        return ArtworkExtractedPalette(dominant, secondary, accent)
    }
}

private fun Float.quantize(step: Float = 0.02f): Float =
    ((this / step).roundToInt() * step).coerceIn(0f, 1f)

/**
 * Real-time Color Reactive Engine Composable:
 * - Extracts dominant, secondary, and accent colors from current song artwork on background thread
 * - Smoothly transitions colors over 800ms when the song changes
 * - Smoothly updates the audio envelope without causing high-frequency root recompositions
 */
@Composable
fun rememberLumoColorReactiveState(
    currentMedia: MediaItemEntity?,
    isPlaying: Boolean,
    positionSec: Int = 0,
    durationSec: Int = 1,
    liveAudioBass: Float = 0f,
    liveAudioEnergy: Float = 0f,
    intensityMode: ColorReactiveIntensity = ColorReactiveIntensity.MEDIUM,
    isFullPlayerActive: Boolean = false
): LumoColorReactiveState {
    val context = LocalContext.current

    var targetPalette by remember {
        mutableStateOf(
            ArtworkExtractedPalette(
                dominantColor = Color(0xFF1A2639),
                secondaryColor = Color(0xFF131C2E),
                accentColor = Color(0xFF5B9CF6)
            )
        )
    }

    // 1. Extract artwork colors whenever the song changes (on background IO dispatcher)
    LaunchedEffect(currentMedia?.id, currentMedia?.artworkUrl) {
        if (currentMedia != null) {
            targetPalette = ArtworkPaletteCache.getOrExtractPalette(context, currentMedia)
        }
    }

    // 2. Smooth 800ms color transition when song changes
    val smoothDominant by animateColorAsState(
        targetValue = targetPalette.dominantColor,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "reactiveDominantColor"
    )
    val smoothSecondary by animateColorAsState(
        targetValue = targetPalette.secondaryColor,
        animationSpec = tween(durationMillis = 840, easing = FastOutSlowInEasing),
        label = "reactiveSecondaryColor"
    )
    val smoothBaseAccent by animateColorAsState(
        targetValue = targetPalette.accentColor,
        animationSpec = tween(durationMillis = 760, easing = FastOutSlowInEasing),
        label = "reactiveAccentColor"
    )

    // 3. Zero-overhead ambient state on browsing tabs; smooth beat envelope only when Full Player is active
    var smoothBass by remember { mutableFloatStateOf(0.14f) }
    var smoothBeatPulse by remember { mutableFloatStateOf(0f) }
    var smoothEnergy by remember { mutableFloatStateOf(0.20f) }
    var isBeatActive by remember { mutableStateOf(false) }

    val waveformProfile = remember(currentMedia?.id) {
        currentMedia?.generateWaveform(64) ?: List(64) { 0.45f }
    }

    LaunchedEffect(currentMedia?.id, isPlaying, intensityMode, isFullPlayerActive) {
        val mult = intensityMode.reactionMultiplier
        if (!isPlaying || currentMedia == null || !isFullPlayerActive) {
            // Static ambient values while browsing Home/Search/Library/Downloads so scrolling is 120fps with zero timer recompositions
            smoothBass = ((if (isPlaying) 0.24f else 0.12f) * mult).quantize(0.05f)
            smoothBeatPulse = 0f
            smoothEnergy = ((if (isPlaying) 0.30f else 0.16f) * mult).quantize(0.05f)
            isBeatActive = false
            return@LaunchedEffect
        }

        val seed = currentMedia.waveformSeed.coerceAtLeast(11)
        val bpm = 98f + (seed % 28)
        val beatPeriodMs = (60_000f / bpm).toLong().coerceIn(460L, 640L)

        while (isActive && isPlaying && isFullPlayerActive) {
            val nowMs = System.currentTimeMillis()
            val safeDur = durationSec.coerceAtLeast(1)
            val progress = (positionSec.toFloat() / safeDur.toFloat()).coerceIn(0f, 1f)
            val waveIdx = (progress * (waveformProfile.size - 1)).toInt().coerceIn(0, waveformProfile.lastIndex)
            val sectionAmplitude = waveformProfile[waveIdx].coerceIn(0.16f, 1.0f)

            val quietDamper = when {
                sectionAmplitude < 0.34f -> 0.45f
                sectionAmplitude < 0.52f -> 0.72f
                else -> 1.0f
            }

            val beatPhase = (nowMs % beatPeriodMs).toFloat() / beatPeriodMs.toFloat()
            val measureBeatIndex = ((nowMs / beatPeriodMs) % 4L).toInt()
            val isKickBeat = measureBeatIndex == 0 || measureBeatIndex == 2

            val instantBeat = if (beatPhase < 0.24f) {
                val attack = sin((beatPhase / 0.24f) * (Math.PI.toFloat() / 2f))
                attack * (if (isKickBeat) 1.0f else 0.65f) * quietDamper
            } else {
                val decayProgress = (beatPhase - 0.24f) / 0.76f
                ((1f - decayProgress) * (1f - decayProgress) * 0.38f) * quietDamper
            }

            val harmonicSubBass = ((sin(nowMs * 0.004 + seed) + 1.0) * 0.5).toFloat()
            val computedBass = ((sectionAmplitude * 0.52f + instantBeat * 0.36f + harmonicSubBass * 0.12f) * quietDamper * mult)
                .quantize(0.04f)
            val computedEnergy = ((sectionAmplitude * 0.65f + instantBeat * 0.25f + abs(cos(nowMs * 0.0025f)) * 0.10f) * quietDamper * mult)
                .quantize(0.04f)
            val computedBeat = (instantBeat * mult).quantize(0.05f)

            smoothBass = computedBass
            smoothEnergy = computedEnergy
            smoothBeatPulse = computedBeat
            isBeatActive = instantBeat > 0.54f

            delay(220L)
        }
    }

    val reactiveAccent = remember(smoothBaseAccent, smoothBeatPulse, smoothEnergy) {
        val brightenFraction = (smoothBeatPulse * 0.24f + smoothEnergy * 0.10f).coerceIn(0f, 0.32f)
        lerp(smoothBaseAccent, Color(0xFFF8FAFC), brightenFraction)
    }

    return remember(
        smoothDominant,
        smoothSecondary,
        smoothBaseAccent,
        reactiveAccent,
        smoothBass,
        smoothBeatPulse,
        smoothEnergy,
        isBeatActive,
        intensityMode,
        isPlaying
    ) {
        LumoColorReactiveState(
            dominantColor = smoothDominant,
            secondaryColor = smoothSecondary,
            accentColor = smoothBaseAccent,
            reactiveAccentColor = reactiveAccent,
            bassLevel = smoothBass,
            beatPulse = smoothBeatPulse,
            energyLevel = smoothEnergy,
            isBeatActive = isBeatActive,
            intensityMode = intensityMode,
            isPlaying = isPlaying
        )
    }
}
