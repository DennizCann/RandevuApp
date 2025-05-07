package com.denizcan.randevuapp.ui.screen

import android.util.Log
import androidx.compose.foundation.clickable
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
                title = { Text(stringResource(id = R.string.welcome_business, businessName)) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bekleyen Randevu Kartı
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRequestsClick() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.pending_appointments),
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Text(
                        text = pendingAppointments.toString(),
                        style = MaterialTheme.typography.displayLarge
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Çalışma Saatleri Butonu
            Button(
                onClick = onWorkingHoursClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.working_hours))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Randevular Butonu
            Button(
                onClick = onCalendarClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.appointments))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Randevu Talepleri Butonu
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
                    // MainActivity.logout metodunu kullan
                    MainActivity.logout(context)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.logout))
            }
        }
    }
} 