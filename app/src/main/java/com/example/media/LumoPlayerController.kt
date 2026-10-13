package com.example.media

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import com.example.data.LumoYouTubeStreamAuth
import com.example.data.MediaItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

data class PlayerPlaybackState(
    val currentMedia: MediaItemEntity? = null,
    val queue: List<MediaItemEntity> = emptyList(),
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionSec: Int = 0,
    val durationSec: Int = 30,
    val liveAudioBass: Float = 0f,
    val liveAudioEnergy: Float = 0f,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: String = "ALL", // "OFF", "ALL", "ONE"
    val playbackSpeed: Float = 1.0f,
    val visualizerEnabled: Boolean = true,
    val eqPreset: String = "Flat",
    val sleepTimerMinutes: Int = 0, // 0 = Off, -1 = End of song, 15/30/45/60 = minutes
    val sleepTimerRemainingSec: Int = 0,
    val isMiniPlayerVisible: Boolean = false,
    val isFullPlayerOpen: Boolean = false,
    val isVideoPlayerOpen: Boolean = false,
    val subtitlesEnabled: Boolean = true,
    val videoBrightness: Float = 0.85f,
    val videoVolume: Float = 0.85f,
    val isLandscapeVideoRequested: Boolean = false,
    val lockScreenSessionActive: Boolean = true
)

class LumoPlayerController(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onRecordPlayback: suspend (String, Int) -> Unit,
    private val onUpdatePosition: suspend (String, Int) -> Unit,
    private val onUpdateDuration: suspend (String, Int) -> Unit,
    private val onRefreshStreamUrl: suspend (MediaItemEntity, Boolean) -> String = { item, _ ->
        item.effectivePlayableUrlOrPath
    }
) {
    private val _state = MutableStateFlow(PlayerPlaybackState())
    val state: StateFlow<PlayerPlaybackState> = _state.asStateFlow()

    @Volatile
    private var mediaPlayer: MediaPlayer? = null
    private var prepareJob: Job? = null
    private var progressJob: Job? = null

    init {
        LumoMediaSessionBridge.onPlayPauseToggle = { play ->
            if (play) resume() else pause()
        }
        LumoMediaSessionBridge.onSkipNext = { skipNext() }
        LumoMediaSessionBridge.onSkipPrevious = { skipPrevious() }
        LumoMediaSessionBridge.onSeekToSec = { sec -> seekTo(sec) }
    }

    fun syncCatalogUpdate(catalog: List<MediaItemEntity>) {
        if (catalog.isEmpty()) return
        val currentId = _state.value.currentMedia?.id
        if (currentId == null) {
            val first = catalog.first()
            _state.update {
                it.copy(
                    currentMedia = first,
                    queue = catalog,
                    positionSec = first.lastPositionSec.coerceIn(0, first.durationSec),
                    durationSec = first.durationSec.coerceAtLeast(1),
                    isMiniPlayerVisible = true
                )
            }
            syncMediaSession()
        } else {
            val refreshed = catalog.find { it.id == currentId }
            if (refreshed != null) {
                _state.update { it.copy(currentMedia = refreshed, queue = catalog) }
            }
        }
    }

    fun playMedia(
        media: MediaItemEntity,
        newQueue: List<MediaItemEntity>? = null,
        openFullExperience: Boolean = false
    ) {
        val updatedQueue = newQueue ?: _state.value.queue.ifEmpty { listOf(media) }
        _state.update {
            it.copy(
                currentMedia = media,
                queue = updatedQueue,
                positionSec = 0,
                durationSec = media.durationSec.coerceAtLeast(1),
                isPlaying = true,
                isBuffering = true,
                isMiniPlayerVisible = true,
                isFullPlayerOpen = if (openFullExperience && !media.isVideo) true else if (openFullExperience) false else it.isFullPlayerOpen,
                isVideoPlayerOpen = if (openFullExperience && media.isVideo) true else if (openFullExperience) false else it.isVideoPlayerOpen
            )
        }

        scope.launch(Dispatchers.IO) {
            onRecordPlayback(media.id, 0)
        }
        prepareAndStreamRealAudio(media, retryAttempt = 0)
        syncMediaSession()
    }

    private fun prepareAndStreamRealAudio(media: MediaItemEntity, retryAttempt: Int = 0) {
        prepareJob?.cancel()
        progressJob?.cancel()

        val previousPlayer = mediaPlayer
        mediaPlayer = null
        if (previousPlayer != null) {
            scope.launch(Dispatchers.IO) {
                runCatching { previousPlayer.stop() }
                runCatching { previousPlayer.release() }
            }
        }

        prepareJob = scope.launch(Dispatchers.IO) {
            val candidateSource = media.effectivePlayableUrlOrPath
            val needsLiveResolve = retryAttempt > 0 ||
                candidateSource.isBlank() ||
                candidateSource.contains("invidious.f5.si") ||
                candidateSource.contains("api-v2.soundcloud.com") ||
                (!candidateSource.startsWith("http") && !candidateSource.startsWith("content://") && !candidateSource.startsWith("file://") && !File(candidateSource).exists())

            val resolvedSource = if (needsLiveResolve) {
                runCatching { onRefreshStreamUrl(media, retryAttempt > 0 || candidateSource.contains("api-v2.soundcloud.com")) }
                    .getOrDefault(candidateSource)
            } else {
                candidateSource
            }

            if (resolvedSource.isBlank()) {
                if (retryAttempt < 2 && _state.value.currentMedia?.id == media.id) {
                    val fresh = runCatching { onRefreshStreamUrl(media, true) }.getOrDefault("")
                    if (fresh.isNotBlank()) {
                        prepareAndStreamRealAudio(media.copy(streamUrl = fresh), retryAttempt = retryAttempt + 1)
                        return@launch
                    }
                }
                _state.update { it.copy(isBuffering = false, isPlaying = false) }
                return@launch
            }

            if (resolvedSource != media.streamUrl && resolvedSource.startsWith("http")) {
                _state.update { state ->
                    if (state.currentMedia?.id == media.id) {
                        state.copy(currentMedia = state.currentMedia.copy(streamUrl = resolvedSource))
                    } else state
                }
            }

            runCatching {
                val mp = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    val localFile = File(resolvedSource)
                    when {
                        localFile.exists() && localFile.length() > 1024L -> {
                            setDataSource(localFile.absolutePath)
                        }
                        resolvedSource.startsWith("content://") || resolvedSource.startsWith("file://") -> {
                            setDataSource(context, Uri.parse(resolvedSource))
                        }
                        else -> {
                            setDataSource(
                                context,
                                Uri.parse(resolvedSource),
                                LumoYouTubeStreamAuth.getHeadersForUrl()
                            )
                        }
                    }

                    setOnPreparedListener { player ->
                        if (_state.value.currentMedia?.id != media.id) {
                            runCatching { player.release() }
                            return@setOnPreparedListener
                        }
                        val realDurSec = (player.duration / 1000).coerceAtLeast(1)
                        _state.update {
                            it.copy(
                                isBuffering = false,
                                durationSec = realDurSec,
                                isPlaying = true
                            )
                        }
                        scope.launch(Dispatchers.IO) { onUpdateDuration(media.id, realDurSec) }
                        val vol = _state.value.videoVolume.coerceIn(0f, 1f)
                        player.setVolume(vol, vol)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            runCatching {
                                player.playbackParams = PlaybackParams().setSpeed(_state.value.playbackSpeed)
                            }
                        }
                        player.start()
                        startTicker()
                        syncMediaSession()
                    }

                    setOnCompletionListener {
                        val s = _state.value
                        when (s.repeatMode) {
                            "ONE" -> {
                                seekTo(0)
                                resume()
                            }
                            "ALL" -> skipNext()
                            else -> {
                                _state.update { it.copy(isPlaying = false, positionSec = 0) }
                                syncMediaSession()
                            }
                        }
                    }

                    setOnErrorListener { player, _, _ ->
                        runCatching { player.release() }
                        if (retryAttempt < 2 && _state.value.currentMedia?.id == media.id) {
                            prepareAndStreamRealAudio(media, retryAttempt = retryAttempt + 1)
                        } else {
                            _state.update { it.copy(isBuffering = false, isPlaying = false) }
                        }
                        true
                    }

                    prepareAsync()
                }
                mediaPlayer = mp
            }.onFailure {
                if (retryAttempt < 2 && _state.value.currentMedia?.id == media.id) {
                    prepareAndStreamRealAudio(media, retryAttempt = retryAttempt + 1)
                } else {
                    _state.update { it.copy(isBuffering = false, isPlaying = false) }
                }
            }
        }
    }

    fun togglePlayPause() {
        if (_state.value.isPlaying) {
            pause()
        } else {
            resume()
        }
    }

    fun resume() {
        val current = _state.value.currentMedia ?: return
        val mp = mediaPlayer
        if (mp != null) {
            runCatching {
                mp.start()
                _state.update { it.copy(isPlaying = true, isMiniPlayerVisible = true) }
                startTicker()
            }.onFailure {
                prepareAndStreamRealAudio(current)
            }
        } else {
            _state.update { it.copy(isPlaying = true, isBuffering = true, isMiniPlayerVisible = true) }
            prepareAndStreamRealAudio(current)
        }
        syncMediaSession()
    }

    fun pause() {
        _state.update { it.copy(isPlaying = false, isBuffering = false) }
        val mp = mediaPlayer
        if (mp != null) {
            scope.launch(Dispatchers.IO) {
                runCatching {
                    if (mp.isPlaying) {
                        mp.pause()
                    }
                }
            }
        }
        progressJob?.cancel()
        val current = _state.value.currentMedia
        if (current != null) {
            val pos = _state.value.positionSec
            scope.launch(Dispatchers.IO) { onUpdatePosition(current.id, pos) }
        }
        syncMediaSession()
    }

    fun seekTo(seconds: Int) {
        val duration = _state.value.durationSec.coerceAtLeast(1)
        val clamped = seconds.coerceIn(0, duration)
        _state.update { it.copy(positionSec = clamped) }
        val mp = mediaPlayer
        if (mp != null) {
            scope.launch(Dispatchers.IO) {
                runCatching { mp.seekTo(clamped * 1000) }
            }
        }
        val current = _state.value.currentMedia
        if (current != null) {
            scope.launch(Dispatchers.IO) { onUpdatePosition(current.id, clamped) }
        }
        syncMediaSession()
    }

    fun skipNext() {
        val queue = _state.value.queue
        val current = _state.value.currentMedia ?: return
        if (queue.isEmpty()) return

        val nextMedia = if (_state.value.isShuffleEnabled && queue.size > 1) {
            queue.filter { it.id != current.id }.random()
        } else {
            val idx = queue.indexOfFirst { it.id == current.id }
            if (idx in 0 until queue.lastIndex) queue[idx + 1] else queue.first()
        }
        playMedia(nextMedia, queue, openFullExperience = false)
    }

    fun skipPrevious() {
        if (_state.value.positionSec > 5) {
            seekTo(0)
            return
        }
        val queue = _state.value.queue
        val current = _state.value.currentMedia ?: return
        if (queue.isEmpty()) return
        val idx = queue.indexOfFirst { it.id == current.id }
        val prevMedia = if (idx > 0) queue[idx - 1] else queue.last()
        playMedia(prevMedia, queue, openFullExperience = false)
    }

    fun toggleShuffle() {
        _state.update { it.copy(isShuffleEnabled = !it.isShuffleEnabled) }
    }

    fun cycleRepeatMode() {
        val next = when (_state.value.repeatMode) {
            "OFF" -> "ALL"
            "ALL" -> "ONE"
            else -> "OFF"
        }
        _state.update { it.copy(repeatMode = next) }
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.5f, 2.0f)
        _state.update { it.copy(playbackSpeed = clamped) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val mp = mediaPlayer
            if (mp != null) {
                scope.launch(Dispatchers.IO) {
                    runCatching {
                        if (mp.isPlaying) {
                            mp.playbackParams = PlaybackParams().setSpeed(clamped)
                        }
                    }
                }
            }
        }
        syncMediaSession()
    }

    fun setVideoBrightness(value: Float) {
        _state.update { it.copy(videoBrightness = value.coerceIn(0.1f, 1.0f)) }
    }

    fun setVideoVolume(value: Float) {
        val clamped = value.coerceIn(0.0f, 1.0f)
        _state.update { it.copy(videoVolume = clamped) }
        val mp = mediaPlayer
        if (mp != null) {
            scope.launch(Dispatchers.IO) {
                runCatching { mp.setVolume(clamped, clamped) }
            }
        }
    }

    fun toggleSubtitles() {
        _state.update { it.copy(subtitlesEnabled = !it.subtitlesEnabled) }
    }

    fun toggleVisualizer() {
        _state.update { it.copy(visualizerEnabled = !it.visualizerEnabled) }
    }

    fun setEqualizerPreset(preset: String) {
        _state.update { it.copy(eqPreset = preset) }
    }

    fun setSleepTimer(minutes: Int) {
        val remaining = if (minutes > 0) minutes * 60 else 0
        _state.update {
            it.copy(
                sleepTimerMinutes = minutes,
                sleepTimerRemainingSec = remaining
            )
        }
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        _state.update { current ->
            val mutable = current.queue.toMutableList()
            if (fromIndex in mutable.indices && toIndex in mutable.indices && fromIndex != toIndex) {
                val item = mutable.removeAt(fromIndex)
                mutable.add(toIndex, item)
                current.copy(queue = mutable)
            } else {
                current
            }
        }
    }

    fun removeFromQueue(mediaId: String) {
        _state.update { current ->
            if (current.currentMedia?.id == mediaId) {
                current
            } else {
                current.copy(queue = current.queue.filterNot { it.id == mediaId })
            }
        }
    }

    fun playNextInQueue(media: MediaItemEntity) {
        _state.update { current ->
            val currentId = current.currentMedia?.id
            val withoutTarget = current.queue.filterNot { it.id == media.id }.toMutableList()
            val currentIdx = withoutTarget.indexOfFirst { it.id == currentId }
            val insertIdx = if (currentIdx >= 0) (currentIdx + 1).coerceAtMost(withoutTarget.size) else 0
            withoutTarget.add(insertIdx, media)
            current.copy(queue = withoutTarget)
        }
    }

    fun clearQueueExceptCurrent() {
        _state.update { current ->
            val active = current.currentMedia
            current.copy(queue = if (active != null) listOf(active) else emptyList())
        }
    }

    fun toggleLandscapeVideoLayout() {
        _state.update { it.copy(isLandscapeVideoRequested = !it.isLandscapeVideoRequested) }
    }

    fun openFullPlayer() {
        val current = _state.value.currentMedia ?: return
        if (current.isVideo) {
            _state.update { it.copy(isVideoPlayerOpen = true, isFullPlayerOpen = false) }
        } else {
            _state.update { it.copy(isFullPlayerOpen = true, isVideoPlayerOpen = false) }
        }
    }

    fun openAudioPlayerExplicitly() {
        _state.update { it.copy(isFullPlayerOpen = true, isVideoPlayerOpen = false) }
    }

    fun openVideoPlayerExplicitly() {
        _state.update { it.copy(isVideoPlayerOpen = true, isFullPlayerOpen = false) }
    }

    fun closeFullOverlays() {
        _state.update { it.copy(isFullPlayerOpen = false, isVideoPlayerOpen = false) }
    }

    fun dismissMiniPlayer() {
        pause()
        _state.update { it.copy(isMiniPlayerVisible = false, isFullPlayerOpen = false, isVideoPlayerOpen = false) }
    }

    private fun startTicker() {
        progressJob?.cancel()
        progressJob = scope.launch(Dispatchers.IO) {
            while (isActive && _state.value.isPlaying) {
                delay(850L)
                val mp = mediaPlayer
                val currentPosSec = runCatching {
                    if (mp != null && mp.isPlaying) (mp.currentPosition / 1000).coerceAtLeast(0) else _state.value.positionSec
                }.getOrDefault(_state.value.positionSec)

                val clampedPos = currentPosSec.coerceAtMost(_state.value.durationSec)
                val currentTimerRemaining = _state.value.sleepTimerRemainingSec
                val nextTimerRemaining = if (currentTimerRemaining > 0) (currentTimerRemaining - 1).coerceAtLeast(0) else 0

                if (currentTimerRemaining == 1 && nextTimerRemaining == 0 && _state.value.sleepTimerMinutes > 0) {
                    _state.update { it.copy(sleepTimerMinutes = 0, sleepTimerRemainingSec = 0) }
                    pause()
                    break
                }

                if (clampedPos != _state.value.positionSec || nextTimerRemaining != currentTimerRemaining) {
                    _state.update {
                        it.copy(
                            positionSec = clampedPos,
                            sleepTimerRemainingSec = nextTimerRemaining
                        )
                    }
                }
            }
        }
    }

    private fun syncMediaSession() {
        val s = _state.value
        val media = s.currentMedia ?: return
        LumoMediaSessionBridge.title = media.title
        LumoMediaSessionBridge.artist = media.artist
        LumoMediaSessionBridge.album = media.album
        LumoMediaSessionBridge.artworkResId = media.artworkResId
        LumoMediaSessionBridge.durationSec = s.durationSec
        LumoMediaSessionBridge.positionSec = s.positionSec
        LumoMediaSessionBridge.isPlaying = s.isPlaying
        LumoMediaSessionBridge.playbackSpeed = s.playbackSpeed
        LumoMediaSessionBridge.syncService(context)
    }

    fun releaseMediaPlayer() {
        prepareJob?.cancel()
        progressJob?.cancel()
        val mp = mediaPlayer
        mediaPlayer = null
        if (mp != null) {
            scope.launch(Dispatchers.IO) {
                runCatching { mp.stop() }
                runCatching { mp.release() }
            }
        }
    }
}
