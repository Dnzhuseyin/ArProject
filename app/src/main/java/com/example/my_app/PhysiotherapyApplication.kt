package com.example.my_app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PhysiotherapyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
    }
}