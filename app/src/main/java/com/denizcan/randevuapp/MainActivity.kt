package com.denizcan.randevuapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.denizcan.randevuapp.navigation.NavGraph
import com.denizcan.randevuapp.ui.theme.RandevuAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RandevuAppTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}