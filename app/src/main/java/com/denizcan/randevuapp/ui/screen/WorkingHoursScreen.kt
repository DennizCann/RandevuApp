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
fun WorkingHoursScreen(
    workingDays: List<String>,
    workingHours: User.WorkingHours,
    onSaveClick: (List<String>, User.WorkingHours) -> Unit,
    onBackClick: () -> Unit
) {
    var selectedDays by remember { mutableStateOf(workingDays) }
    var openingTime by remember { mutableStateOf(workingHours.opening) }
    var closingTime by remember { mutableStateOf(workingHours.closing) }
    var slotDuration by remember { mutableStateOf(workingHours.slotDuration.toString()) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Çalışma Saatleri",
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
            // Çalışma Günleri
            Text(
                text = "Çalışma Günleri",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(
                    items = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"),
                    key = { it }
                ) { day ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (day) {
                                "MONDAY" -> "Pazartesi"
                                "TUESDAY" -> "Salı"
                                "WEDNESDAY" -> "Çarşamba"
                                "THURSDAY" -> "Perşembe"
                                "FRIDAY" -> "Cuma"
                                "SATURDAY" -> "Cumartesi"
                                "SUNDAY" -> "Pazar"
                                else -> day
                            }
                        )
                        Checkbox(
                            checked = selectedDays.contains(day),
                            onCheckedChange = { checked ->
                                selectedDays = if (checked) {
                                    selectedDays + day
                                } else {
                                    selectedDays - day
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Çalışma Saatleri
            Text(
                text = "Çalışma Saatleri",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = openingTime,
                    onValueChange = { openingTime = it },
                    label = { Text("Başlangıç") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                OutlinedTextField(
                    value = closingTime,
                    onValueChange = { closingTime = it },
                    label = { Text("Bitiş") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Randevu Süresi
            OutlinedTextField(
                value = slotDuration,
                onValueChange = { 
                    slotDuration = it
                },
                label = { Text("Randevu Süresi (dakika)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // Kaydet Butonu
            Button(
                onClick = {
                    val updatedHours = User.WorkingHours(
                        opening = openingTime,
                        closing = closingTime,
                        slotDuration = slotDuration.toIntOrNull() ?: 60
                    )
                    onSaveClick(selectedDays, updatedHours)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kaydet")
            }
        }
    }
}

private val daysOfWeek = listOf(
    "Pazartesi",
    "Salı",
    "Çarşamba",
    "Perşembe",
    "Cuma",
    "Cumartesi",
    "Pazar"
) 