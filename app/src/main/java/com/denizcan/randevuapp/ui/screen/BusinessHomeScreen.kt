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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessHomeScreen(
    businessName: String,
    pendingAppointments: Int,
    onWorkingHoursClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onRequestsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Üst Bar
        Text(
            text = businessName,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // İstatistikler
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Bekleyen Randevular",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = pendingAppointments.toString(),
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Menü Butonları
        Button(
            onClick = onWorkingHoursClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Çalışma Saatleri")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCalendarClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Takvim")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRequestsClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Randevu Talepleri")
        }

        Spacer(modifier = Modifier.weight(1f))

        // Çıkış Butonu
        val context = LocalContext.current
        OutlinedButton(
            onClick = {
                Log.d("BusinessHomeScreen", "Çıkış butonuna basıldı")
                MainActivity.logout(context)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Çıkış Yap")
        }
    }
} 