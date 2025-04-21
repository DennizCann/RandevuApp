package com.denizcan.randevuapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.denizcan.randevuapp.ui.components.AppTopBar
import com.denizcan.randevuapp.R
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterClick: (String, String) -> Unit,
    onBackToLoginClick: () -> Unit,
    isBusinessRegister: Boolean,
    onSwitchRegisterType: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (isBusinessRegister) 
                    stringResource(id = R.string.business) 
                else 
                    stringResource(id = R.string.customer),
                onBackClick = onBackToLoginClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Scaffold padding'lerini uygula
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.create_account),
                style = MaterialTheme.typography.headlineMedium
            )
            
            // Kullanıcı tipi seçici
            SingleChoiceSegmentedButtonRow(modifier = Modifier.padding(bottom = 24.dp)) {
                SegmentedButton(
                    selected = !isBusinessRegister,
                    onClick = { if(isBusinessRegister) onSwitchRegisterType() },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text(stringResource(id = R.string.customer))
                }
                SegmentedButton(
                    selected = isBusinessRegister,
                    onClick = { if(!isBusinessRegister) onSwitchRegisterType() },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text(stringResource(id = R.string.business))
                }
            }
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(id = R.string.email)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(id = R.string.password)) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Şifre Tekrar") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { 
                    if (password == confirmPassword) {
                        onRegisterClick(email, password)
                    }
                },
                enabled = email.isNotEmpty() && password.isNotEmpty() && 
                         password == confirmPassword && password.length >= 6,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.register))
            }

            TextButton(onClick = onBackToLoginClick) {
                Text(stringResource(id = R.string.have_account))
            }
        }
    }
} 