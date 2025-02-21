package com.atarusov.musicplayer.features.localtracks.di

import com.atarusov.musicplayer.features.localtracks.data.TrackRepositoryImpl
import com.atarusov.musicplayer.features.localtracks.domain.TrackRepository
import dagger.Binds
import dagger.Module

@Module
interface LocalTrackListBindModule {
    @Binds
    fun bindTrackRepository(trackRepositoryImpl: TrackRepositoryImpl): TrackRepository
}