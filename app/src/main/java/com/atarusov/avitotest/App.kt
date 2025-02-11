package com.atarusov.avitotest

import android.app.Application
import com.atarusov.avitotest.di.DaggerAppComponent

class App: Application() {
    val appComponent = DaggerAppComponent.create()
}