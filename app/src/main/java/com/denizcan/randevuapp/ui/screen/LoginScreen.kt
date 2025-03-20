package com.denizcan.randevuapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: () -> Unit,
    isBusinessLogin: Boolean,
    onSwitchLoginType: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isBusinessLogin) "İşletme Girişi" else "Müşteri Girişi",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-posta") }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Şifre") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = { onLoginClick(email, password) }) {
            Text("Giriş Yap")
        }
        
        TextButton(onClick = onRegisterClick) {
            Text("Hesap Oluştur")
        }

        TextButton(onClick = onSwitchLoginType) {
            Text(if (isBusinessLogin) "Müşteri Girişine Geç" else "İşletme Girişine Geç")
        }
    }
} 