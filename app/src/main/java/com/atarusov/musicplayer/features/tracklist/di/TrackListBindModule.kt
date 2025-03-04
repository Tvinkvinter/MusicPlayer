package com.atarusov.musicplayer.features.tracklist.di

import com.atarusov.musicplayer.features.tracklist.data.local.TrackLocalRepository
import com.atarusov.musicplayer.features.tracklist.data.remote.TrackRemoteRepository
import com.atarusov.musicplayer.features.tracklist.domain.TrackRepository
import dagger.Module
import dagger.Provides

@Module
class TrackListBindModule {

    @Provides
    @RemoteRepository
    fun provideRemoteRepository(trackRemoteRepository: TrackRemoteRepository): TrackRepository =
        trackRemoteRepository

    @Provides
    @LocalRepository
    fun provideLocalRepository(trackLocalRepository: TrackLocalRepository): TrackRepository =
        trackLocalRepository
}