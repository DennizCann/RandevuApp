package com.denizcan.randevuapp.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denizcan.randevuapp.model.User
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle
import java.util.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.graphics.Color
import com.denizcan.randevuapp.ui.components.SystemAwareScaffold
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.text.font.FontStyle
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BusinessDetailScreen(
    business: User.Business,
    availableSlots: List<String>,
    selectedDate: LocalDate,
    onDateSelect: (LocalDate) -> Unit,
    onTimeSelect: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onAppointmentRequest: () -> Unit,
    onBackClick: () -> Unit,
    isLoading: Boolean
) {
    var selectedTime by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf("") }
    var showTimeError by remember { mutableStateOf(false) }
    
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    
    LaunchedEffect(Unit) {
        while(true) {
            delay(60000)
            currentTime = LocalTime.now()
        }
    }
    
    val today = LocalDate.now()
    val isTodaySelected = selectedDate.equals(today)

    SystemAwareScaffold(
        title = business.businessName,
        onBackClick = onBackClick,
        bottomButtonText = "Randevu Talep Et",
        isBottomButtonEnabled = selectedTime != null,
        isLoading = isLoading,
        onBottomButtonClick = onAppointmentRequest
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 80.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Adres: ${business.address}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Telefon: ${business.phone}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item {
                    Text(
                        text = "Tarih Seçin",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                item {
                    DateSelectorRow(
                        selectedDate = selectedDate,
                        onDateSelect = onDateSelect
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item {
                    Text(
                        text = "Saat Seçin",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                item {
                    Text(
                        text = "Uygun Saatler",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(items = availableSlots) { time ->
                            val timeSlot = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
                            val isTimeSlotInPast = isTodaySelected && timeSlot.isBefore(currentTime)
                            
                            TimeSlotChip(
                                time = time,
                                isSelected = time == selectedTime,
                                isInPast = isTimeSlotInPast,
                                onClick = { 
                                    if (isTimeSlotInPast) {
                                        showTimeError = true
                                    } else {
                                        selectedTime = time
                                        onTimeSelect(time)
                                        showTimeError = false
                                    }
                                }
                            )
                        }
                    }
                    
                    if (showTimeError) {
                        Text(
                            text = "Geçmiş saat aralıkları için randevu alamazsınız",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
                
                item {
                    Text(
                        text = "Randevu Notu",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    OutlinedTextField(
                        value = note,
                        onValueChange = { onNoteChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        placeholder = { Text("İşletmeye iletmek istediğiniz notlar (isteğe bağlı)...") },
                        maxLines = 3
                    )
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun DateSelectorRow(
    selectedDate: LocalDate,
    onDateSelect: (LocalDate) -> Unit
) {
    val dates = remember {
        (0..13).map { LocalDate.now().plusDays(it.toLong()) }
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(dates) { date ->
            DateCard(
                date = date,
                isSelected = date == selectedDate,
                onClick = { onDateSelect(date) }
            )
        }
    }
}

@Composable
private fun DateCard(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .width(72.dp)
            .height(80.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outline
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfWeek.getDisplayName(
                    TextStyle.SHORT,
                    Locale("tr")
                ),
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimary 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimary 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TimeSlotChip(
    time: String,
    isSelected: Boolean,
    isInPast: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.height(40.dp),
        shape = RoundedCornerShape(20.dp),
        color = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isInPast -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            else -> MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            1.dp, 
            when {
                isSelected -> Color.Transparent
                isInPast -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.outline
            }
        ),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = time,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isInPast -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    else -> MaterialTheme.colorScheme.onSurface
                },
                style = if (isInPast) 
                    MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic)
                else 
                    MaterialTheme.typography.bodyMedium
            )
        }
    }
} 