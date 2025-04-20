package com.denizcan.randevuapp.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.denizcan.randevuapp.model.User
import org.threeten.bp.LocalDate
import org.threeten.bp.format.TextStyle
import java.util.*
import com.denizcan.randevuapp.ui.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            AppTopBar(
                title = business.businessName,
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .padding(bottom = 80.dp) // Buton için yer ayırma
                    .verticalScroll(rememberScrollState())
            ) {
                // İşletme Bilgileri
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

                Spacer(modifier = Modifier.height(16.dp))

                // Tarih Seçici Başlık
                Text(
                    text = "Tarih Seçin",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Tarih Seçici
                DateSelectorRow(
                    selectedDate = selectedDate,
                    onDateSelect = onDateSelect
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Saat Seçici Başlık
                Text(
                    text = "Saat Seçin",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Saat Seçici
                TimeSlotGrid(
                    availableSlots = availableSlots,
                    selectedTime = selectedTime,
                    onTimeSelect = { time ->
                        selectedTime = time
                        onTimeSelect(time)
                    }
                )

                // Not alanı
                if (selectedTime != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Not Başlık
                    Text(
                        text = "Randevu Notu",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = note,
                        onValueChange = { 
                            note = it
                            onNoteChange(it)
                        },
                        placeholder = { Text("İşletmeye iletmek istediğiniz notlar (isteğe bağlı)...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }

                // Alt boşluk
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Sabit Randevu Talebi Butonu
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = onAppointmentRequest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = selectedTime != null && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Randevu Talep Et")
                    }
                }
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
        // Bugünden başlayarak 14 günlük liste oluştur
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
private fun TimeSlotGrid(
    availableSlots: List<String>,
    selectedTime: String?,
    onTimeSelect: (String) -> Unit
) {
    val columns = 4
    
    // Her satırda 4 öğe olacak şekilde satır sayısını hesapla
    val rows = (availableSlots.size + columns - 1) / columns
    
    // Grid layout'u manuel oluştur
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (rowIndex in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Her satırda 4 buton veya daha az (son satır için)
                for (colIndex in 0 until columns) {
                    val index = rowIndex * columns + colIndex
                    if (index < availableSlots.size) {
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            TimeSlotButton(
                                timeSlot = availableSlots[index],
                                isSelected = availableSlots[index] == selectedTime,
                                onClick = { onTimeSelect(availableSlots[index]) }
                            )
                        }
                    } else {
                        // Boş yer tutucusu, satırın düzgün hizalanması için
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeSlotButton(
    timeSlot: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        colors = ButtonDefaults.outlinedButtonColors(
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
        )
    ) {
        Text(
            text = timeSlot,
            color = if (isSelected) 
                MaterialTheme.colorScheme.onPrimary 
            else 
                MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
} 