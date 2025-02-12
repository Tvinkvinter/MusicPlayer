package com.atarusov.avitotest.features.apitracks.di

import com.atarusov.avitotest.features.apitracks.data.TrackRepositoryImpl
import com.atarusov.avitotest.features.apitracks.domain.TrackRepository
import dagger.Binds
import dagger.Module

@Module
interface ApiTrackListBindModule {
    @Binds
    fun bindTrackRepository(trackRepositoryImpl: TrackRepositoryImpl): TrackRepository
}