package com.denizcan.randevuapp.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.denizcan.randevuapp.MainActivity

@Composable
fun CustomerHomeScreen(
    customerName: String,
    onBusinessListClick: () -> Unit,
    onAppointmentsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Hoş geldin, $customerName",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBusinessListClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("İşletme Ara")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAppointmentsClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Randevularım")
        }

        Spacer(modifier = Modifier.weight(1f))

        val context = LocalContext.current
        
        OutlinedButton(
            onClick = {
                Log.d("CustomerHomeScreen", "Çıkış butonuna basıldı")
                MainActivity.logout(context)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Çıkış Yap")
        }
    }
} 