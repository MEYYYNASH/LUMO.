package com.example.media

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.IBinder
import com.example.MainActivity
import com.example.R

class LumoPlaybackService : Service() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val session = MediaSession(this, "LumoMediaSession").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() {
                    LumoMediaSessionBridge.onPlayPauseToggle?.invoke(true)
                }

                override fun onPause() {
                    LumoMediaSessionBridge.onPlayPauseToggle?.invoke(false)
                }

                override fun onSkipToNext() {
                    LumoMediaSessionBridge.onSkipNext?.invoke()
                }

                override fun onSkipToPrevious() {
                    LumoMediaSessionBridge.onSkipPrevious?.invoke()
                }

                override fun onSeekTo(pos: Long) {
                    LumoMediaSessionBridge.onSeekToSec?.invoke((pos / 1000L).toInt())
                }
            })
            isActive = true
        }
        mediaSession = session
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TOGGLE_PLAY -> {
                val currentlyPlaying = LumoMediaSessionBridge.isPlaying
                LumoMediaSessionBridge.onPlayPauseToggle?.invoke(!currentlyPlaying)
            }
            ACTION_PREV -> LumoMediaSessionBridge.onSkipPrevious?.invoke()
            ACTION_NEXT -> LumoMediaSessionBridge.onSkipNext?.invoke()
            ACTION_UPDATE_SESSION -> {
                updateMediaSessionAndNotification()
            }
        }
        return START_NOT_STICKY
    }

    private fun updateMediaSessionAndNotification() {
        val session = mediaSession ?: return
        val title = LumoMediaSessionBridge.title
        val artist = LumoMediaSessionBridge.artist
        val album = LumoMediaSessionBridge.album
        val durationMs = LumoMediaSessionBridge.durationSec * 1000L
        val positionMs = LumoMediaSessionBridge.positionSec * 1000L
        val isPlaying = LumoMediaSessionBridge.isPlaying
        val artworkRes = LumoMediaSessionBridge.artworkResId

        val artBitmap = runCatching {
            BitmapFactory.decodeResource(resources, artworkRes)
        }.getOrNull()

        val metadataBuilder = MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, title)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
            .putString(MediaMetadata.METADATA_KEY_ALBUM, album)
            .putLong(MediaMetadata.METADATA_KEY_DURATION, durationMs)
        if (artBitmap != null) {
            metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, artBitmap)
        }
        session.setMetadata(metadataBuilder.build())

        val playbackState = PlaybackState.Builder()
            .setActions(
                PlaybackState.ACTION_PLAY or
                    PlaybackState.ACTION_PAUSE or
                    PlaybackState.ACTION_PLAY_PAUSE or
                    PlaybackState.ACTION_SKIP_TO_NEXT or
                    PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                    PlaybackState.ACTION_SEEK_TO
            )
            .setState(
                if (isPlaying) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED,
                positionMs,
                LumoMediaSessionBridge.playbackSpeed
            )
            .build()
        session.setPlaybackState(playbackState)

        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevPending = PendingIntent.getService(
            this,
            1,
            Intent(this, LumoPlaybackService::class.java).setAction(ACTION_PREV),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val playPausePending = PendingIntent.getService(
            this,
            2,
            Intent(this, LumoPlaybackService::class.java).setAction(ACTION_TOGGLE_PLAY),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val nextPending = PendingIntent.getService(
            this,
            3,
            Intent(this, LumoPlaybackService::class.java).setAction(ACTION_NEXT),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        val notification = builder
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText("LUMO")
            .setLargeIcon(artBitmap)
            .setContentIntent(openAppIntent)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .addAction(
                Notification.Action.Builder(
                    android.R.drawable.ic_media_previous,
                    "Previous",
                    prevPending
                ).build()
            )
            .addAction(
                Notification.Action.Builder(
                    if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                    if (isPlaying) "Pause" else "Play",
                    playPausePending
                ).build()
            )
            .addAction(
                Notification.Action.Builder(
                    android.R.drawable.ic_media_next,
                    "Next",
                    nextPending
                ).build()
            )
            .setStyle(
                Notification.MediaStyle()
                    .setMediaSession(session.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "lumo_media_playback_channel"
        const val NOTIFICATION_ID = 4102
        const val ACTION_UPDATE_SESSION = "com.example.media.ACTION_UPDATE_SESSION"
        const val ACTION_TOGGLE_PLAY = "com.example.media.ACTION_TOGGLE_PLAY"
        const val ACTION_PREV = "com.example.media.ACTION_PREV"
        const val ACTION_NEXT = "com.example.media.ACTION_NEXT"
    }
}

object LumoMediaSessionBridge {
    var title: String = "Glass Monolith"
    var artist: String = "Kaelen Vance"
    var album: String = "Neon Echoes"
    var artworkResId: Int = R.drawable.img_album_neon_echoes_1791694225919
    var durationSec: Int = 222
    var positionSec: Int = 84
    var isPlaying: Boolean = false
    var playbackSpeed: Float = 1.0f

    var onPlayPauseToggle: ((Boolean) -> Unit)? = null
    var onSkipNext: (() -> Unit)? = null
    var onSkipPrevious: (() -> Unit)? = null
    var onSeekToSec: ((Int) -> Unit)? = null

    fun syncService(context: Context) {
        runCatching {
            val intent = Intent(context, LumoPlaybackService::class.java).apply {
                action = LumoPlaybackService.ACTION_UPDATE_SESSION
            }
            context.startService(intent)
        }
    }
}
