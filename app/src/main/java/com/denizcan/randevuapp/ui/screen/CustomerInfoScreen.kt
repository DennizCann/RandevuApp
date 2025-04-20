package com.denizcan.randevuapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.denizcan.randevuapp.viewmodel.UserInfoViewModel
import androidx.compose.ui.text.input.KeyboardType
import com.denizcan.randevuapp.viewmodel.UserInfoViewModel.UserInfoState
import android.util.Log

@Composable
fun CustomerInfoScreen(
    onSaveClick: (String, String) -> Unit,
    userId: String
) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isFormValid by remember { mutableStateOf(false) }
    
    val userInfoViewModel: UserInfoViewModel = viewModel()
    val userInfoState = userInfoViewModel.userInfoState.collectAsState().value
    
    // Form doğrulama
    LaunchedEffect(fullName, phone) {
        isFormValid = fullName.isNotEmpty() && phone.isNotEmpty() && phone.length >= 10
    }
    
    // Debug için userInfoState değişimlerini izliyoruz
    LaunchedEffect(userInfoState) {
        Log.d("CustomerInfoScreen", "UserInfoState değişti: $userInfoState")
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Kişisel Bilgileriniz",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Ad Soyad") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Telefon Numarası") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { 
                if (isFormValid) {
                    Log.d("CustomerInfoScreen", "Kaydet butonuna basıldı: $fullName, $phone")
                    onSaveClick(fullName, phone)
                }
            },
            enabled = isFormValid && userInfoState !is UserInfoState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (userInfoState is UserInfoState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kaydediliyor...")
            } else {
                Text("Kaydet")
            }
        }
        
        // Hata gösterim
        if (userInfoState is UserInfoState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = userInfoState.message,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
} 