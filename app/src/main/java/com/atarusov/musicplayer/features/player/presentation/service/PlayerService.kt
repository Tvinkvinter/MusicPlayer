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
import androidx.media3.exoplayer.ExoPlayer
import com.atarusov.musicplayer.App
import com.atarusov.musicplayer.R
import com.atarusov.musicplayer.features.player.domain.model.Track
import com.atarusov.musicplayer.features.player.presentation.viewmodel.ServiceCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


class PlayerService : Service() {

    @Inject
    lateinit var player: ExoPlayer
    private var currentTrack: Track? = null
    private val binder = LocalBinder()

    private val _playbackTime = MutableStateFlow<PlaybackTime?>(null)
    val playbackTime: StateFlow<PlaybackTime?> = _playbackTime

    private val _notificationActions = MutableSharedFlow<NotificationAction>()
    val notificationActions: SharedFlow<NotificationAction> = _notificationActions

    private var isServiceCommandReceivingStarted = false

    inner class LocalBinder : Binder() {
        fun getService(): PlayerService = this@PlayerService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        (applicationContext as App).appComponent.inject(this)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification(false))
        startPlaybackTimeTransmission()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let {
            val notificationAction = NotificationAction.valueOf(it)

            CoroutineScope(Dispatchers.Main).launch {
                _notificationActions.emit(notificationAction)
            }
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
            .setColor(getColor(R.color.background))
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


    private fun startPlaybackTimeTransmission() {
        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                _playbackTime.value = PlaybackTime(
                    timeElapsed = player.currentPosition,
                    timeTotal = player.duration
                )
                delay(1000)
            }
        }
    }

    fun startServiceCommandReceiving(serverCommands: Flow<ServiceCommand>){
        if (isServiceCommandReceivingStarted) return
        isServiceCommandReceivingStarted = true
        CoroutineScope(Dispatchers.Main).launch {
            serverCommands.collectLatest { serviceCommand ->
                when(serviceCommand) {
                    ServiceCommand.Pause -> pause()
                    ServiceCommand.Play -> play()
                    is ServiceCommand.Seek -> seek(serviceCommand.time)
                    is ServiceCommand.SetTrackAndPlay -> setTrackAndPlay(serviceCommand.track)
                    ServiceCommand.StopService -> stopSelf()
                }
            }
        }
    }

    private fun setTrackAndPlay(track: Track) {
        if (currentTrack?.trackURI == track.trackURI) return
        currentTrack = track
        player.setMediaItem(MediaItem.fromUri(track.trackURI))
        play()
    }

    private fun play() {
        player.prepare()
        player.play()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(true))

    }

    private fun pause() {
        startForeground(NOTIFICATION_ID, createNotification(false))
        player.pause()
    }

    private fun seek(time: Long) {
        player.seekTo(time)
        play()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
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
