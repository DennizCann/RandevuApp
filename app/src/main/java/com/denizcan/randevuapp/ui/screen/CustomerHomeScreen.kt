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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    customerName: String,
    onBusinessListClick: () -> Unit,
    onAppointmentsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TopAppBar(
            title = { Text(stringResource(id = R.string.app_name)) },
            actions = {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(id = R.string.settings)
                    )
                }
            }
        )

        Text(
            text = stringResource(id = R.string.welcome),
            style = MaterialTheme.typography.headlineMedium
        )

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

        Spacer(modifier = Modifier.weight(1f))

        val context = LocalContext.current
        
        OutlinedButton(
            onClick = {
                Log.d("CustomerHomeScreen", "Logout button was pressed")
                MainActivity.logout(context)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.logout))
        }
    }
} 