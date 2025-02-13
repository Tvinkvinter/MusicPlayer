package com.atarusov.avitotest.features.apitracks.di

import com.atarusov.avitotest.features.apitracks.data.TrackService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module(includes = [ApiTrackListBindModule::class])
object ApiTrackListModule {

    @Singleton
    @Provides
    fun provideTracksService(retrofit: Retrofit): TrackService {
        return retrofit.create(TrackService::class.java)
    }
}