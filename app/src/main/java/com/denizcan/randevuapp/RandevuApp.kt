package com.denizcan.randevuapp

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class RandevuApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }
} 