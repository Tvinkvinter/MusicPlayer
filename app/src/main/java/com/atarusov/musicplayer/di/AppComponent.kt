package com.atarusov.musicplayer.di

import android.content.Context
import com.atarusov.musicplayer.features.player.di.PlayerModule
import com.atarusov.musicplayer.features.player.presentation.PlayerFragment
import com.atarusov.musicplayer.features.player.presentation.service.PlayerService
import com.atarusov.musicplayer.features.tracklist.di.TrackListModule
import com.atarusov.musicplayer.features.tracklist.presentation.TrackListFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [NetworkModule::class, TrackListModule::class, PlayerModule::class]
)
interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun appContext(appContext: Context): Builder

        fun build(): AppComponent
    }

    fun inject(fragment: TrackListFragment)
    fun inject(fragment: PlayerFragment)
    fun inject(service: PlayerService)
}