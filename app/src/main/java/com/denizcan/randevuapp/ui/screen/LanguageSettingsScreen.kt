package com.denizcan.randevuapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.denizcan.randevuapp.R
import com.denizcan.randevuapp.ui.components.SystemAwareScaffold
import com.denizcan.randevuapp.utils.LanguageManager
import kotlinx.coroutines.launch

@Composable
fun LanguageSettingsScreen(
    onBackClick: () -> Unit,
    onLanguageChanged: () -> Unit
) {
    val context = LocalContext.current
    val languageManager = remember { LanguageManager(context) }
    val coroutineScope = rememberCoroutineScope()
    
    // Mevcut seçili dili LiveData olarak izle
    val currentLanguage = languageManager.selectedLanguage.observeAsState("system")
    
    // Sistem dilini getir
    val systemLanguage = remember { languageManager.getSystemLanguageCode() }
    
    SystemAwareScaffold(
        title = stringResource(id = R.string.language),
        onBackClick = onBackClick
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Sistemin dilini göstermek için bilgi
            if (currentLanguage.value == "system") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(id = R.string.system_language) + ": ${languageManager.supportedLanguages[systemLanguage] ?: systemLanguage}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = stringResource(id = R.string.system_language_info),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            if (currentLanguage.value != "system") {
                Button(
                    onClick = {
                        languageManager.resetToSystemLanguage()
                        onLanguageChanged()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(stringResource(id = R.string.reset_to_system))
                }
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(languageManager.supportedLanguages.toList()) { (code, name) ->
                    val isSelected = currentLanguage.value == code
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                coroutineScope.launch {
                                    languageManager.setLanguage(code)
                                    onLanguageChanged()
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                    
                    if (code != languageManager.supportedLanguages.keys.last()) {
                        Divider()
                    }
                }
            }
        }
    }
} 