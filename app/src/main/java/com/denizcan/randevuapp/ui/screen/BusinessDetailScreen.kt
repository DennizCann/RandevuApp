package com.denizcan.randevuapp.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denizcan.randevuapp.model.User
import org.threeten.bp.LocalDate
import org.threeten.bp.format.TextStyle
import java.util.*

@Composable
fun BusinessDetailScreen(
    business: User.Business,
    availableSlots: List<String>,
    selectedDate: LocalDate,
    onDateSelect: (LocalDate) -> Unit,
    onTimeSelect: (String) -> Unit,
    onAppointmentRequest: () -> Unit,
    onBackClick: () -> Unit,
    isLoading: Boolean
) {
    var selectedTime by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                text = business.businessName,
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // İşletme Bilgileri
        Text(
            text = "Adres: ${business.address}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Telefon: ${business.phone}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tarih Seçici
        DateSelectorRow(
            selectedDate = selectedDate,
            onDateSelect = onDateSelect
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Saat Seçici
        TimeSlotGrid(
            availableSlots = availableSlots,
            selectedTime = selectedTime,
            onTimeSelect = { time ->
                selectedTime = time
                onTimeSelect(time)
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Randevu Talebi Butonu
        Button(
            onClick = onAppointmentRequest,
            modifier = Modifier.fillMaxWidth(),
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
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(availableSlots) { slot ->
            TimeSlotButton(
                timeSlot = slot,
                isSelected = slot == selectedTime,
                onClick = { onTimeSelect(slot) }
            )
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
                MaterialTheme.colorScheme.onSurface
        )
    }
} 