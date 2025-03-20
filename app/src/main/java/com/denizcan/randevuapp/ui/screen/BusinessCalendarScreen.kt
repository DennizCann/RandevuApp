package com.denizcan.randevuapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denizcan.randevuapp.model.Appointment
import com.denizcan.randevuapp.model.AppointmentStatus
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessCalendarScreen(
    selectedDate: LocalDate,
    appointments: List<Appointment>,
    onDateSelect: (LocalDate) -> Unit,
    onAppointmentStatusChange: (String, AppointmentStatus) -> Unit,
    onBackClick: () -> Unit
) {
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
                text = "Takvim",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tarih Seçici
        DateSelector(
            selectedDate = selectedDate,
            onDateSelect = onDateSelect
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Seçili Tarih
        Text(
            text = selectedDate.format(
                DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr"))
            ),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Randevu Listesi
        if (appointments.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Bu tarihte randevu bulunmuyor")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(appointments) { appointment ->
                    AppointmentCard(appointment = appointment)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppointmentCard(
    appointment: Appointment
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (appointment.status) {
                AppointmentStatus.CONFIRMED -> MaterialTheme.colorScheme.primaryContainer
                AppointmentStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer
                AppointmentStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appointment.dateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = when (appointment.status) {
                        AppointmentStatus.CONFIRMED -> "✓ Onaylandı"
                        AppointmentStatus.PENDING -> "⏳ Beklemede"
                        AppointmentStatus.CANCELLED -> "✕ İptal Edildi"
                    },
                    color = when (appointment.status) {
                        AppointmentStatus.CONFIRMED -> MaterialTheme.colorScheme.primary
                        AppointmentStatus.PENDING -> MaterialTheme.colorScheme.secondary
                        AppointmentStatus.CANCELLED -> MaterialTheme.colorScheme.error
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = appointment.customerName.ifEmpty { "İsimsiz Müşteri" },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
} 