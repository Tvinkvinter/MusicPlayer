package com.atarusov.avitotest.features.player.di

import com.atarusov.avitotest.features.player.data.remote.TrackService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module(includes = [PlayerBindModule::class])
object PlayerModule {

    @Singleton
    @Provides
    fun provideTracksService(retrofit: Retrofit): TrackService {
        return retrofit.create(TrackService::class.java)
    }
}