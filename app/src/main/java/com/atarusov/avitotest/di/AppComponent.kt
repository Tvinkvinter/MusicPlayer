package com.atarusov.avitotest.di

import com.atarusov.avitotest.features.apitracks.di.ApiTrackListModule
import com.atarusov.avitotest.features.apitracks.presentation.ApiTrackListFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [NetworkModule::class, ApiTrackListModule::class])
interface AppComponent {
    fun inject(fragment: ApiTrackListFragment)
}