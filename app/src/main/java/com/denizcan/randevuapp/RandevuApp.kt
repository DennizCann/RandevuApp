package com.denizcan.randevuapp

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.lifecycle.lifecycleScope
import com.denizcan.randevuapp.utils.LanguageManager
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.os.Handler
import android.os.Looper
import android.util.Log

class RandevuApp : Application() {
    lateinit var languageManager: LanguageManager
    
    override fun attachBaseContext(base: Context) {
        languageManager = LanguageManager(base)
        super.attachBaseContext(languageManager.applyLanguage(base))
    }
    
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        languageManager = LanguageManager(this)
        
        // İlk açılışta sistem dilini kontrol et ve ayarla - ana thread'de çalıştır
        Handler(Looper.getMainLooper()).post {
            // Mevcut dil ayarını kontrol et
            val currentLang = languageManager.getLanguage()
            Log.d("RandevuApp", "Başlatılırken dil ayarı: $currentLang")
            
            // Sistem dilini al
            val systemLang = languageManager.getSystemLanguageCode()
            Log.d("RandevuApp", "Sistem dili: $systemLang")
            
            // Dil ayarını güncelle
            languageManager.initializeWithSystemLanguage()
            
            // Son dil ayarını kontrol et
            val finalLang = languageManager.getLanguage()
            Log.d("RandevuApp", "Güncelleme sonrası dil ayarı: $finalLang")
        }
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        languageManager.applyLanguage(this)
    }
} 