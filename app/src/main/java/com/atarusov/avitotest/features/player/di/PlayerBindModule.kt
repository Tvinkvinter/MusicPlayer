package com.atarusov.avitotest.features.player.di

import com.atarusov.avitotest.features.player.data.TrackRepositoryImpl
import com.atarusov.avitotest.features.player.domain.TrackRepository
import dagger.Binds
import dagger.Module

@Module
interface PlayerBindModule {
    @Binds
    fun bindTracRepository(trackRepositoryImpl: TrackRepositoryImpl): TrackRepository
}