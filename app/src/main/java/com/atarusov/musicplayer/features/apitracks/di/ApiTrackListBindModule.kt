package com.atarusov.musicplayer.features.apitracks.di

import com.atarusov.musicplayer.features.apitracks.data.TrackRepositoryImpl
import com.atarusov.musicplayer.features.apitracks.domain.TrackRepository
import dagger.Binds
import dagger.Module

@Module
interface ApiTrackListBindModule {
    @Binds
    fun bindTrackRepository(trackRepositoryImpl: TrackRepositoryImpl): TrackRepository
}