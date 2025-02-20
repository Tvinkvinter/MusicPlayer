package com.atarusov.musicplayer.features.player.presentation.service

fun interface PlayerNotificationListener {
    fun doNotificationAction(action: NotificationAction)
}