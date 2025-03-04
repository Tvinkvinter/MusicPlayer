package com.atarusov.musicplayer.features.tracklist.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LocalRepository

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RemoteRepository