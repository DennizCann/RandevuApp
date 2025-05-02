package com.denizcan.randevuapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.denizcan.randevuapp.viewmodel.UserInfoViewModel
import com.denizcan.randevuapp.R
import com.denizcan.randevuapp.ui.components.AppTopBar
import android.util.Log
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerInfoScreen(
    initialName: String = "",
    initialPhone: String = "",
    onSaveClick: (String, String) -> Unit,
    onBackClick: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var phoneNumber by remember { mutableStateOf(initialPhone) }
    var isFormValid by remember { mutableStateOf(false) }
    
    val userInfoViewModel: UserInfoViewModel = viewModel()
    val userInfoState = userInfoViewModel.userInfoState.collectAsState().value
    
    // Form doğrulama
    LaunchedEffect(name, phoneNumber) {
        isFormValid = name.isNotEmpty() && phoneNumber.isNotEmpty() && phoneNumber.length >= 10
    }
    
    // Debug için userInfoState değişimlerini izliyoruz
    LaunchedEffect(userInfoState) {
        Log.d("CustomerInfoScreen", "UserInfoState değişti: $userInfoState")
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.personal_information),
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(id = R.string.full_name)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text(stringResource(id = R.string.phone_number_input)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            Button(
                onClick = { 
                    if (isFormValid) {
                        Log.d("CustomerInfoScreen", "Kaydet butonuna basıldı: $name, $phoneNumber")
                        onSaveClick(name, phoneNumber)
                    }
                },
                enabled = isFormValid && userInfoState !is UserInfoViewModel.UserInfoState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (userInfoState is UserInfoViewModel.UserInfoState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kaydediliyor...")
                } else {
                    Text(stringResource(id = R.string.save))
                }
            }
            
            // Hata gösterim
            if (userInfoState is UserInfoViewModel.UserInfoState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = (userInfoState as UserInfoViewModel.UserInfoState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
} 