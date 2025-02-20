package com.atarusov.musicplayer.features.player.presentation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Binder
import android.os.IBinder
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.atarusov.musicplayer.App
import com.atarusov.musicplayer.R
import com.atarusov.musicplayer.features.player.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject


class PlayerService : Service() {
    var listener: PlayerNotificationListener? = null

    @Inject
    lateinit var player: ExoPlayer
    private var currentTrack: Track? = null
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): PlayerService = this@PlayerService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        (applicationContext as App).appComponent.inject(this)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification(false))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let {
            val notificationAction = NotificationAction.valueOf(it)
            listener?.doNotificationAction(notificationAction)
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(isPlaying: Boolean): Notification {

        val playPauseAction = createNotificationButton(
            if (isPlaying) R.drawable.ic_pause_notification_24 else R.drawable.ic_play_notification_24,
            if (isPlaying) R.string.accessibility_play_pause_btn else R.string.accessibility_play_pause_btn,
            NotificationAction.PLAY_PAUSE
        )

        val prevAction = createNotificationButton(
            R.drawable.ic_prev_notification_24,
            R.string.accessibility_prev_btn,
            NotificationAction.PREV
        )

        val nextAction = createNotificationButton(
            R.drawable.ic_next_notification_24,
            R.string.accessibility_next_btn,
            NotificationAction.NEXT
        )

        val rewindBackAction = createNotificationButton(
            R.drawable.ic_rewind_back_notification_24,
            R.string.accessibility_rewind_back_btn,
            NotificationAction.REWIND_BACK
        )

        val rewindForwardAction = createNotificationButton(
            R.drawable.ic_rewind_forward_notification_24,
            R.string.accessibility_rewind_forward_btn,
            NotificationAction.REWIND_FORWARD
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_icon_24)
            .setContentTitle(currentTrack?.trackTitle)
            .setContentText(currentTrack?.artistName)
            .setOngoing(isPlaying)
            .setActions(
                prevAction,
                rewindBackAction,
                playPauseAction,
                rewindForwardAction,
                nextAction
            )
            .setColor(getColor(R.color.surface))
            .setColorized(true)
            .setStyle(Notification.MediaStyle())
            .build()

    }

    private fun createNotificationButton(
        iconResource: Int,
        stringResource: Int,
        notificationAction: NotificationAction
    ): Notification.Action {
        val pendingIntent = PendingIntent.getService(
            this, 0,
            Intent(this, PlayerService::class.java).apply {
                action = notificationAction.toString()
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Action.Builder(
            Icon.createWithResource(this, iconResource),
            getString(stringResource),
            pendingIntent
        ).build()
    }


    fun subscribeConsumerOnProgress(consumer: (Flow<Pair<Long, Long>>) -> Unit) {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    consumer.invoke(getTrackTimeFlow())
                }
            }
        })
    }

    fun getTrackTimeFlow(): Flow<Pair<Long, Long>> {
        return flow {
            while (true) {
                val timeElapsed = player.currentPosition
                val totalTime = player.duration
                emit(timeElapsed to totalTime)
                kotlinx.coroutines.delay(100)
            }
        }
    }

    fun setTrackAndPlay(track: Track) {
        if (currentTrack?.trackURI == track.trackURI) return
        currentTrack = track
        player.setMediaItem(MediaItem.fromUri(track.trackURI))
        play()
    }

    fun play() {
        player.prepare()
        player.play()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(true))

    }

    fun pause() {
        startForeground(NOTIFICATION_ID, createNotification(false))
        player.pause()
    }

    fun seek(time: Long) {
        player.seekTo(time)
        play()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        listener = null
        player.apply {
            stop()
            release()
        }
    }


    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "PlayerServiceChannelID"
        private const val CHANNEL_NAME = "PlayerServiceChannel"
    }
}
