package com.atarusov.musicplayer.di

import android.content.Context
import com.atarusov.musicplayer.features.apitracks.di.ApiTrackListModule
import com.atarusov.musicplayer.features.apitracks.presentation.ApiTrackListFragment
import com.atarusov.musicplayer.features.localtracks.di.LocalTrackListModule
import com.atarusov.musicplayer.features.localtracks.presentation.LocalTrackListFragment
import com.atarusov.musicplayer.features.player.di.PlayerModule
import com.atarusov.musicplayer.features.player.presentation.PlayerFragment
import com.atarusov.musicplayer.features.player.presentation.service.PlayerService
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [NetworkModule::class, ApiTrackListModule::class, LocalTrackListModule::class,
               PlayerModule::class]
)
interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun appContext(appContext: Context): Builder

        fun build(): AppComponent
    }

    fun inject(fragment: ApiTrackListFragment)
    fun inject(fragment: LocalTrackListFragment)
    fun inject(fragment: PlayerFragment)
    fun inject(service: PlayerService)
}