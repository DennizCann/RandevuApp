package com.denizcan.randevuapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BusinessInfoScreen(
    onSaveClick: (String, String, String, String) -> Unit,
    userId: String
) {
    var businessName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var sector by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "İşletme Bilgileri",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = businessName,
            onValueChange = { businessName = it },
            label = { Text("İşletme Adı") },
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Adres") },
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Telefon") },
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = sector,
            onValueChange = { sector = it },
            label = { Text("Sektör") },
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { onSaveClick(businessName, address, phone, sector) },
            enabled = businessName.isNotEmpty() && address.isNotEmpty() && 
                     phone.isNotEmpty() && sector.isNotEmpty()
        ) {
            Text("Kaydet")
        }
    }
} 