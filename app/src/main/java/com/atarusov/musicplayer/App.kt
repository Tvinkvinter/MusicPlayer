package com.atarusov.musicplayer

import android.app.Application
import com.atarusov.musicplayer.di.AppComponent
import com.atarusov.musicplayer.di.DaggerAppComponent

class App: Application() {
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder().appContext(applicationContext).build()
    }
}