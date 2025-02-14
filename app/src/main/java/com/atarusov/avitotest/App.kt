package com.atarusov.avitotest

import android.app.Application
import com.atarusov.avitotest.di.AppComponent
import com.atarusov.avitotest.di.DaggerAppComponent

class App: Application() {
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder().appContext(applicationContext).build()
    }
}