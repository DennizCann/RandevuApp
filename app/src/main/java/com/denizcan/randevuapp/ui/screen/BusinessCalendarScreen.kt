package com.denizcan.randevuapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.denizcan.randevuapp.model.Appointment
import com.denizcan.randevuapp.model.AppointmentStatus
import com.denizcan.randevuapp.ui.components.AppTopBar
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessCalendarScreen(
    selectedDate: LocalDate,
    appointments: List<Appointment>,
    availableTimeSlots: List<String>,
    onDateSelect: (LocalDate) -> Unit,
    onAppointmentStatusChange: (String, AppointmentStatus) -> Unit,
    onTimeSlotBlock: (String) -> Unit,
    onTimeSlotUnblock: (String) -> Unit,
    onCancelAndBlock: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Takvim",
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

            // Zaman aralıkları ve randevuların birleştirilmiş listesi
            if (availableTimeSlots.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Bu tarihte çalışma saati bulunmuyor")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableTimeSlots) { timeSlot ->
                        // Bu zaman diliminde randevu var mı kontrol et
                        val appointment = appointments.find { 
                            it.dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")) == timeSlot 
                        }
                        
                        TimeSlotCard(
                            timeSlot = timeSlot,
                            appointment = appointment,
                            onBlockSlot = { onTimeSlotBlock(timeSlot) },
                            onStatusChange = { id, status -> onAppointmentStatusChange(id, status) },
                            onUnblockSlot = { id -> onTimeSlotUnblock(id) },
                            onCancelAndBlock = { id -> onCancelAndBlock(id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeSlotCard(
    timeSlot: String,
    appointment: Appointment?,
    onBlockSlot: () -> Unit,
    onStatusChange: (String, AppointmentStatus) -> Unit,
    onUnblockSlot: (String) -> Unit = {},
    onCancelAndBlock: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                appointment == null -> MaterialTheme.colorScheme.surface  // Boş
                appointment.status == AppointmentStatus.CONFIRMED -> MaterialTheme.colorScheme.primaryContainer  // Onaylanmış
                appointment.status == AppointmentStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer  // Bekliyor
                appointment.status == AppointmentStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer  // İptal
                appointment.status == AppointmentStatus.BLOCKED -> MaterialTheme.colorScheme.surfaceVariant  // Kapatılmış
                else -> MaterialTheme.colorScheme.surface
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
                    text = timeSlot,
                    style = MaterialTheme.typography.titleMedium
                )

                if (appointment != null) {
                    Text(
                        text = when (appointment.status) {
                            AppointmentStatus.CONFIRMED -> "✓ Onaylandı"
                            AppointmentStatus.PENDING -> "⏳ Beklemede"
                            AppointmentStatus.CANCELLED -> "✕ İptal Edildi"
                            AppointmentStatus.BLOCKED -> "🔒 Kapatıldı"
                        },
                        color = when (appointment.status) {
                            AppointmentStatus.CONFIRMED -> MaterialTheme.colorScheme.primary
                            AppointmentStatus.PENDING -> MaterialTheme.colorScheme.secondary
                            AppointmentStatus.CANCELLED -> MaterialTheme.colorScheme.error
                            AppointmentStatus.BLOCKED -> MaterialTheme.colorScheme.outline
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    // Boş zaman dilimi
                    Text(
                        text = "✓ Müsait",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            if (appointment != null && appointment.status != AppointmentStatus.BLOCKED) {
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = appointment.customerName.ifEmpty { "İsimsiz Müşteri" },
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Eğer not varsa göster
                if (appointment.note.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Not: ${appointment.note}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // İşlemler
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (appointment == null) {
                    // Boş zaman dilimi için Kapat butonu
                    Button(
                        onClick = onBlockSlot,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Kapat")
                    }
                } else if (appointment.status == AppointmentStatus.BLOCKED) {
                    // Kapatılmış zaman dilimi için Aç butonu
                    Button(
                        onClick = { onUnblockSlot(appointment.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Aç")
                    }
                } else if (appointment.status == AppointmentStatus.CONFIRMED) {
                    // Onaylanmış randevu için İptal Et butonu
                    Button(
                        onClick = { onCancelAndBlock(appointment.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("İptal Et")
                    }
                }
            }
        }
    }
} 