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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.denizcan.randevuapp.MainActivity
import com.denizcan.randevuapp.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    customerName: String,
    customerId: String,
    onBusinessListClick: () -> Unit,
    onAppointmentsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.welcome_customer, customerName)) },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onBusinessListClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(id = R.string.search_business))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onAppointmentsClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(id = R.string.my_appointments))
                }
            }

            val context = LocalContext.current

            OutlinedButton(
                onClick = {
                    Log.d("CustomerHomeScreen", "Logout button was pressed")
                    MainActivity.logout(context)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Text(stringResource(id = R.string.logout))
            }
        }
    }
} 