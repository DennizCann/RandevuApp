package com.denizcan.randevuapp.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.denizcan.randevuapp.MainActivity
import com.denizcan.randevuapp.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight

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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hoş Geldin, $businessName") },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // İstatistikler kartı
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Bekleyen Randevu Talepleri",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = pendingAppointments.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
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
                Text(stringResource(id = R.string.appointments))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRequestsClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.appointment_requests))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Çıkış Butonu
            val context = LocalContext.current
            OutlinedButton(
                onClick = {
                    Log.d("BusinessHomeScreen", "Logout button was pressed")
                    MainActivity.logout(context)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.logout))
            }
        }
    }
} 