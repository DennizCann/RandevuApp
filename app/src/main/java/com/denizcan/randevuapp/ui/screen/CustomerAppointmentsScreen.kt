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
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerAppointmentsScreen(
    appointments: List<Appointment>,
    onCancelAppointment: (String) -> Unit,
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
                text = "Randevularım",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (appointments.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Henüz randevunuz bulunmuyor")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(appointments) { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        onCancelAppointment = onCancelAppointment
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppointmentCard(
    appointment: Appointment,
    onCancelAppointment: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (appointment.status) {
                AppointmentStatus.CONFIRMED -> MaterialTheme.colorScheme.primaryContainer
                AppointmentStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer
                AppointmentStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Tarih: ${appointment.dateTime.toLocalDate().format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr")))}",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Saat: ${appointment.dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "İşletme: ${appointment.businessId}", // TODO: İşletme adını göster
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            // Randevu durumu
            val (statusText, statusColor) = when (appointment.status) {
                AppointmentStatus.PENDING -> "Onay Bekliyor" to MaterialTheme.colorScheme.tertiary
                AppointmentStatus.CONFIRMED -> "Onaylandı" to MaterialTheme.colorScheme.primary
                AppointmentStatus.CANCELLED -> "Reddedildi" to MaterialTheme.colorScheme.error
                AppointmentStatus.BLOCKED -> "Kapatıldı" to MaterialTheme.colorScheme.outline
            }
            
            Text(
                text = statusText,
                color = statusColor,
                style = MaterialTheme.typography.bodyMedium
            )

            // Sadece CONFIRMED randevular için iptal butonu göster
            if (appointment.status == AppointmentStatus.CONFIRMED) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { onCancelAppointment(appointment.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Randevuyu İptal Et")
                }
            }
        }
    }
} 