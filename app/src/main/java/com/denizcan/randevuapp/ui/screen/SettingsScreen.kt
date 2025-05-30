package com.denizcan.randevuapp.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.denizcan.randevuapp.R
import com.denizcan.randevuapp.ui.components.SystemAwareScaffold
import java.util.*

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    SystemAwareScaffold(
        title = stringResource(id = R.string.settings),
        onBackClick = onBackClick
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            ListItem(
                headlineContent = { Text(stringResource(id = R.string.language)) },
                modifier = Modifier.clickable { onLanguageClick() }
            )
            
            Divider()
            
            ListItem(
                headlineContent = { Text(stringResource(id = R.string.logout)) },
                modifier = Modifier.clickable { onLogoutClick() }
            )
        }
    }
}

@Composable
fun LanguageSelector(onLanguageSelected: (Locale) -> Unit) {
    val languages = listOf(
        "English" to Locale.ENGLISH,
        "Türkçe" to Locale("tr"),
        "Русский" to Locale("ru"),
        "Deutsch" to Locale("de")
    )
    
    Column {
        Text(
            text = stringResource(R.string.settings_language),
            style = MaterialTheme.typography.titleMedium
        )
        
        languages.forEach { (name, locale) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLanguageSelected(locale) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = name)
            }
        }
    }
} 