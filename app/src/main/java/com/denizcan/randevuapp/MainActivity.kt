package com.denizcan.randevuapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.denizcan.randevuapp.navigation.NavGraph
import com.denizcan.randevuapp.ui.theme.RandevuAppTheme
import com.google.firebase.auth.FirebaseAuth
import java.lang.ref.WeakReference

class MainActivity : ComponentActivity() {
    // Companion object ile statik bir metot tanımlayalım
    companion object {
        // Activity'ye erişim için bir referans
        private var activityRef: WeakReference<MainActivity>? = null
        
        fun logout(context: Context) {
            // Firebase oturumunu kapat
            FirebaseAuth.getInstance().signOut()
            Log.d("MainActivity", "Kullanıcı çıkış yaptı")
            
            // Activity'yi yeniden başlat
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
            
            // Alternatif olarak mevcut activity'yi sonlandır
            activityRef?.get()?.finish()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Activity referansını sakla
        activityRef = WeakReference(this)
        
        setContent {
            RandevuAppTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}