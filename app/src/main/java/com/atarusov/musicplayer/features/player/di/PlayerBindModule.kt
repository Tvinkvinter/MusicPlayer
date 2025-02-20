package com.atarusov.musicplayer.features.player.di

import com.atarusov.musicplayer.features.player.data.TrackRepositoryImpl
import com.atarusov.musicplayer.features.player.domain.TrackRepository
import dagger.Binds
import dagger.Module

@Module
interface PlayerBindModule {
    @Binds
    fun bindTrackRepository(trackRepositoryImpl: TrackRepositoryImpl): TrackRepository
}