package com.denizcan.randevuapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Sistem çubuklarıyla uyumlu, sabit alt çubuk içeren bir düzen bileşeni.
 * Bu bileşen, ekranın alt kısmında sabit bir buton veya panel göstermek 
 * istediğiniz her ekran için kullanılabilir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemAwareScaffold(
    title: String,
    onBackClick: () -> Unit,
    bottomButtonText: String? = null,
    isBottomButtonEnabled: Boolean = true,
    isBottomButtonVisible: Boolean = true,
    isLoading: Boolean = false,
    onBottomButtonClick: () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        bottomBar = {
            if (bottomButtonText != null && isBottomButtonVisible) {
                BottomAppBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = onBottomButtonClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = isBottomButtonEnabled && !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(bottomButtonText)
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        content(paddingValues)
    }
} 