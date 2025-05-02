package com.denizcan.randevuapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.denizcan.randevuapp.R
import com.denizcan.randevuapp.ui.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessInfoScreen(
    initialEmail: String = "",
    initialBusinessName: String = "",
    initialAddress: String = "",
    initialPhone: String = "",
    initialSector: String = "",
    onSaveClick: (String, String, String, String) -> Unit,
    onBackClick: () -> Unit
) {
    var businessName by remember { mutableStateOf(initialBusinessName) }
    var address by remember { mutableStateOf(initialAddress) }
    var phone by remember { mutableStateOf(initialPhone) }
    var sector by remember { mutableStateOf(initialSector) }
    var isFormValid by remember { mutableStateOf(false) }
    
    // Form doğrulama
    LaunchedEffect(businessName, address, phone, sector) {
        isFormValid = businessName.isNotEmpty() && address.isNotEmpty() && 
                     phone.isNotEmpty() && sector.isNotEmpty()
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.business_information),
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text(stringResource(id = R.string.business_name)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text(stringResource(id = R.string.address)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(stringResource(id = R.string.phone)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            OutlinedTextField(
                value = sector,
                onValueChange = { sector = it },
                label = { Text(stringResource(id = R.string.sector)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
            
            Button(
                onClick = { 
                    if (isFormValid) {
                        onSaveClick(businessName, address, phone, sector)
                    }
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.save))
            }
        }
    }
} 