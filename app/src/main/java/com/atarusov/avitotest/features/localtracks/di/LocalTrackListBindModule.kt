package com.atarusov.avitotest.features.localtracks.di

import com.atarusov.avitotest.features.localtracks.data.TrackRepositoryImpl
import com.atarusov.avitotest.features.localtracks.domain.TrackRepository
import dagger.Binds
import dagger.Module

@Module
interface LocalTrackListBindModule {
    @Binds
    fun bindTrackRepository(trackRepositoryImpl: TrackRepositoryImpl): TrackRepository
}