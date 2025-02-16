package com.atarusov.avitotest.features.player.presentation.service

fun interface PlayerNotificationListener {
    fun doNotificationAction(action: NotificationAction)
}