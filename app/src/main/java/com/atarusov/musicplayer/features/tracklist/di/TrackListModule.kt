package com.atarusov.musicplayer.features.tracklist.di

import android.content.ContentResolver
import android.content.Context
import com.atarusov.musicplayer.features.tracklist.data.remote.TrackService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module(includes = [TrackListBindModule::class])
object TrackListModule {

    @Singleton
    @Provides
    fun provideTracksService(retrofit: Retrofit): TrackService {
        return retrofit.create(TrackService::class.java)
    }

    @Provides
    fun provideContentResolver(context: Context): ContentResolver {
        return context.contentResolver
    }
}