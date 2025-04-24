package com.denizcan.randevuapp.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.denizcan.randevuapp.R
import com.denizcan.randevuapp.model.User
import com.denizcan.randevuapp.ui.components.AppTopBar
import androidx.navigation.NavController
import com.denizcan.randevuapp.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessListScreen(
    businesses: List<User.Business>,
    sectors: List<String>,
    onBusinessClick: (String) -> Unit,
    onBackClick: () -> Unit,
    navController: NavController
) {
    var selectedSector by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    val filteredBusinesses = businesses.filter { business ->
        val matchesSector = selectedSector == null || selectedSector == "" || business.sector == selectedSector
        val matchesQuery = searchQuery.isEmpty() || business.businessName.contains(searchQuery, ignoreCase = true)
        matchesSector && matchesQuery
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.businesses),
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.businesses),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Sektör filtresi
            Text(
                text = stringResource(id = R.string.select_sector),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedSector ?: stringResource(id = R.string.all_sectors),
                    onValueChange = { },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    label = { Text(stringResource(id = R.string.select_sector)) }
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.all_sectors)) },
                        onClick = { 
                            selectedSector = null
                            expanded = false
                        }
                    )
                    sectors.forEach { sector ->
                        DropdownMenuItem(
                            text = { Text(sector) },
                            onClick = { 
                                selectedSector = sector
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.total_businesses, businesses.size, filteredBusinesses.size),
                style = MaterialTheme.typography.bodySmall
            )

            if (businesses.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.business_list_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (filteredBusinesses.isEmpty()) {
                if (selectedSector != null && selectedSector != "") {
                    Text(
                        text = stringResource(id = R.string.no_business_sector, selectedSector ?: ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.no_business_all_sectors),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // İşletme Listesi
            if (filteredBusinesses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.no_business_found),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredBusinesses) { business ->
                        BusinessCard(
                            business = business,
                            onClick = { onBusinessClick(business.id) },
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessCard(
    business: User.Business,
    onClick: () -> Unit,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                onClick()
            }
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