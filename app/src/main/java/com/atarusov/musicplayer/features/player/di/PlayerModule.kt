package com.atarusov.musicplayer.features.player.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.atarusov.musicplayer.features.player.data.remote.TrackService
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

    @Provides
    fun provideExoPlayer(context: Context): ExoPlayer {
        return ExoPlayer.Builder(context).build()
    }
}