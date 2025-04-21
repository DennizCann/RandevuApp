package com.denizcan.randevuapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.*

class LanguageManager(private val context: Context) {
    
    private val PREF_NAME = "language_settings"
    private val LANGUAGE_KEY = "selected_language"
    
    private val preferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    // Desteklenen diller
    val supportedLanguages = mapOf(
        "system" to "Sistem Dili",
        "tr" to "Türkçe",
        "en" to "English",
        "de" to "Deutsch", 
        "ru" to "Русский"
    )
    
    // Dil değişikliklerini izlemek için LiveData
    private val _selectedLanguage = MutableLiveData<String>()
    val selectedLanguage: LiveData<String> = _selectedLanguage
    
    init {
        // Mevcut dil ayarını yükle
        _selectedLanguage.value = getLanguage()
    }
    
    // Dil kodunu al
    fun getLanguage(): String {
        return preferences.getString(LANGUAGE_KEY, "system") ?: "system"
    }
    
    // Dili değiştir ve kaydet
    fun setLanguage(languageCode: String) {
        preferences.edit().putString(LANGUAGE_KEY, languageCode).apply()
        
        // Ana threade geçiş yap - postValue kullan
        _selectedLanguage.postValue(languageCode)
        
        updateResources(context, languageCode)
    }
    
    // Ayarlanan dili uygula
    fun applyLanguage(context: Context): Context {
        val language = getLanguage()
        return updateResources(context, language)
    }
    
    // Uygulama kaynaklarını güncelle
    private fun updateResources(context: Context, language: String): Context {
        val locale = when (language) {
            "system" -> Resources.getSystem().configuration.locales.get(0)
            else -> Locale(language)
        }
        
        Locale.setDefault(locale)
        
        val configuration = Configuration(context.resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale)
            return context.createConfigurationContext(configuration)
        } else {
            configuration.locale = locale
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            return context
        }
    }
    
    // Sistem dilini al
    fun getSystemLanguageCode(): String {
        val locale = Resources.getSystem().configuration.locales.get(0)
        val languageCode = locale.language
        
        // Debug için log ekleyelim
        Log.d("LanguageManager", "Sistem dili algılandı: $languageCode")
        Log.d("LanguageManager", "Sistem ülkesi: ${locale.country}")
        Log.d("LanguageManager", "Tam locale: ${locale.toString()}")
        
        // Eğer desteklenen diller içinde varsa, o dili kullan
        return if (supportedLanguages.containsKey(languageCode)) {
            languageCode
        } else {
            // Yoksa varsayılan olarak İngilizce
            "en"
        }
    }
    
    // İlk açılışta sistem dilini ayarla (eğer henüz bir dil seçilmemişse)
    fun initializeWithSystemLanguage() {
        // SharedPreferences'dan mevcut dil ayarını kontrol et
        val currentLanguage = getLanguage()
        
        // Eğer "system" ise, sistem dilini kullan
        if (currentLanguage == "system") {
            val systemLanguage = getSystemLanguageCode()
            if (systemLanguage != "system") {
                // Eğer sistem dili desteklenen dillerden biriyse, onu ayarla
                setLanguage(systemLanguage)
            }
        }
    }
    
    // Dil ayarlarını temizle ve sistem diline geri dön
    fun resetToSystemLanguage() {
        preferences.edit().remove(LANGUAGE_KEY).apply()
        _selectedLanguage.postValue("system")
        
        // Kaydedilen dil ayarını kontrol et
        val savedLanguage = getLanguage()
        Log.d("LanguageManager", "Dil ayarları sıfırlandı, yeni değer: $savedLanguage")
    }
} 