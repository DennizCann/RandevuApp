package com.denizcan.randevuapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denizcan.randevuapp.model.User
import com.denizcan.randevuapp.ui.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessListScreen(
    businesses: List<User.Business>,
    sectors: List<String>,
    onBusinessClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var selectedSector by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredBusinesses = businesses.filter { business ->
        val matchesSector = selectedSector == null || business.sector == selectedSector
        val matchesQuery = business.businessName.contains(searchQuery, ignoreCase = true)
        matchesSector && matchesQuery
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "İşletmeler",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Scaffold padding'lerini uygula
                .padding(16.dp)
        ) {
            // Üst Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    // Geri butonu ikonu
                }
                Text(
                    text = "İşletmeler",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sektör Filtresi
            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedSector ?: "Tüm Sektörler",
                    onValueChange = { },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    label = { Text("Sektör Seçin") }
                )

                ExposedDropdownMenu(
                    expanded = false,
                    onDismissRequest = { }
                ) {
                    DropdownMenuItem(
                        text = { Text("Tüm Sektörler") },
                        onClick = { 
                            selectedSector = null
                        }
                    )
                    sectors.forEach { sector ->
                        DropdownMenuItem(
                            text = { Text(sector) },
                            onClick = { 
                                selectedSector = sector
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // İşletme Listesi
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredBusinesses) { business ->
                    BusinessCard(
                        business = business,
                        onClick = { onBusinessClick(business.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessCard(
    business: User.Business,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = business.businessName,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = business.sector,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = business.address,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
} 