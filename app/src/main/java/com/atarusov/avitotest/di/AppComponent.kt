package com.atarusov.avitotest.di

import android.content.Context
import com.atarusov.avitotest.features.apitracks.di.ApiTrackListModule
import com.atarusov.avitotest.features.apitracks.presentation.ApiTrackListFragment
import com.atarusov.avitotest.features.localtracks.di.LocalTrackListModule
import com.atarusov.avitotest.features.localtracks.presentation.LocalTrackListFragment
import com.atarusov.avitotest.features.player.di.PlayerModule
import com.atarusov.avitotest.features.player.presentation.PlayerFragment
import com.atarusov.avitotest.features.player.presentation.service.PlayerService
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