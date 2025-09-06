package com.aos.f_lab_trash

import android.app.Application
import timber.log.Timber

class Application: Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.Forest.plant(Timber.DebugTree())
    }
}