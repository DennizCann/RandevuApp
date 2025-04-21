package com.denizcan.randevuapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.denizcan.randevuapp.navigation.NavGraph
import com.denizcan.randevuapp.ui.screen.LanguageSettingsScreen
import com.denizcan.randevuapp.ui.screen.SettingsScreen
import com.denizcan.randevuapp.ui.theme.RandevuAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.denizcan.randevuapp.utils.LanguageManager
import com.denizcan.randevuapp.viewmodel.BusinessListViewModel
import java.lang.ref.WeakReference
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    // Companion object ile statik bir metot tanımlayalım
    companion object {
        // Activity'ye erişim için bir referans
        private var activityRef: WeakReference<MainActivity>? = null
        
        fun logout(context: Context) {
            // Firebase oturumunu kapat
            FirebaseAuth.getInstance().signOut()
            Log.d("MainActivity", "User logged out")
            
            // Activity'yi yeniden başlat
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
            
            // Alternatif olarak mevcut activity'yi sonlandır
            activityRef?.get()?.finish()
        }
    }
    
    private lateinit var languageManager: LanguageManager
    private val viewModel: BusinessListViewModel by viewModels()
    
    override fun attachBaseContext(newBase: Context) {
        languageManager = LanguageManager(newBase)
        super.attachBaseContext(languageManager.applyLanguage(newBase))
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Activity referansını sakla
        activityRef = WeakReference(this)
        
        languageManager = LanguageManager(this)
        
        // Dil ayarlarını dinle (LiveData kullanarak)
        languageManager.selectedLanguage.observe(this) { language ->
            // Dil değiştiğinde burada işlem yapabilirsiniz
            Log.d("MainActivity", "Active language: $language")
            
            // Eğer "system" ayarı ise, sistem dili algılansın
            if (language == "system") {
                val systemLanguage = languageManager.getSystemLanguageCode()
                Log.d("MainActivity", "System language: $systemLanguage")
            }
        }
        
        setContent {
            RandevuAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    
                    // İki NavHost tanımı yapmak yerine, mevcut NavGraph yapısını kullan
                    // ve ona ek rotalar ekle
                    NavGraph(navController = navController)
                }
            }
        }
    }

    // Aktivite yeniden yaratıldığında state korunması için
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Gerekli durumları kaydet
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Dil değişiminden sonra işletme listesini yeniden yükle
        viewModel.loadBusinesses()
    }
}