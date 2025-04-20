package com.denizcan.randevuapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
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
            text = "Randevu Uygulaması",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Kullanıcı tipi seçici
        SingleChoiceSegmentedButtonRow(modifier = Modifier.padding(bottom = 24.dp)) {
            SegmentedButton(
                selected = !isBusinessLogin,
                onClick = { if (isBusinessLogin) onSwitchLoginType() },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text("Müşteri")
            }
            SegmentedButton(
                selected = isBusinessLogin,
                onClick = { if (!isBusinessLogin) onSwitchLoginType() },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text("İşletme")
            }
        }
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-posta") },
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Şifre") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { onLoginClick(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Giriş Yap")
        }
        
        TextButton(onClick = onRegisterClick) {
            Text("Hesap Oluştur")
        }
    }
} 