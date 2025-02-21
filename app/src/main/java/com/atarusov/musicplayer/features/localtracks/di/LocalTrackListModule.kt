package com.atarusov.musicplayer.features.localtracks.di

import android.content.ContentResolver
import android.content.Context
import dagger.Module
import dagger.Provides

@Module(includes = [LocalTrackListBindModule::class])
object LocalTrackListModule {

    @Provides
    fun provideContentResolver(context: Context): ContentResolver {
        return context.contentResolver
    }
}