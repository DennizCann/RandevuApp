package com.denizcan.randevuapp.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.denizcan.randevuapp.R
import com.denizcan.randevuapp.model.Appointment
import com.denizcan.randevuapp.model.AppointmentStatus
import com.denizcan.randevuapp.ui.components.AppTopBar
import org.threeten.bp.LocalDate
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
                title = stringResource(id = R.string.calendar_title),
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
            BusinessDatePicker(
                selectedDate = selectedDate,
                onDateSelect = onDateSelect
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Takvim sayfasındaki tarih formatlaması için çözüm
            val context = LocalContext.current
            val locale = context.resources.configuration.locales[0]
            val dateFormatter = remember(locale) {
                DateTimeFormatter.ofPattern("d MMMM yyyy", locale)
            }

            // Seçilen tarihi gösterme
            Text(
                text = selectedDate.format(dateFormatter),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Zaman aralıkları ve randevuların birleştirilmiş listesi
            if (availableTimeSlots.isEmpty()) {
                Text(
                    text = stringResource(R.string.calendar_no_slots),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        // Sadece temel filtreleme yapılıyor - ek filtreler artık gerekli değil çünkü
                        // slot listesi oluşturulurken doğru hesaplanıyor
                        availableTimeSlots
                    ) { timeSlot ->
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

            Spacer(modifier = Modifier.height(16.dp))
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
                            AppointmentStatus.CONFIRMED -> stringResource(id = R.string.status_confirmed)
                            AppointmentStatus.PENDING -> stringResource(id = R.string.status_pending)
                            AppointmentStatus.CANCELLED -> stringResource(id = R.string.status_cancelled)
                            AppointmentStatus.BLOCKED -> stringResource(id = R.string.status_blocked)
                            AppointmentStatus.COMPLETED -> stringResource(id = R.string.status_completed)
                        },
                        color = when (appointment.status) {
                            AppointmentStatus.CONFIRMED -> MaterialTheme.colorScheme.primary
                            AppointmentStatus.PENDING -> MaterialTheme.colorScheme.secondary
                            AppointmentStatus.CANCELLED -> MaterialTheme.colorScheme.error
                            AppointmentStatus.BLOCKED -> MaterialTheme.colorScheme.outline
                            AppointmentStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    // Boş zaman dilimi
                    Text(
                        text = stringResource(id = R.string.status_available),
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
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(id = R.string.calendar_button_close))
                    }
                } else if (appointment.status == AppointmentStatus.BLOCKED) {
                    // Kapatılmış zaman dilimi için Aç butonu
                    Button(
                        onClick = { onUnblockSlot(appointment.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(id = R.string.calendar_button_open))
                    }
                } else if (appointment.status == AppointmentStatus.CONFIRMED) {
                    // Onaylanmış randevu için İptal Et butonu
                    Button(
                        onClick = { onCancelAndBlock(appointment.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(id = R.string.calendar_button_cancel))
                    }
                }
            }
        }
    }
}

@Composable
fun BusinessDatePicker(
    selectedDate: LocalDate,
    onDateSelect: (LocalDate) -> Unit
) {
    // Normal Row yerine LazyRow kullanılıyor (kaydırılabilir)
    val today = LocalDate.now()
    
    // 7 gün yerine 30 gün gösterelim (daha fazla kaydırma imkanı için)
    val dates = remember {
        List(30) { i -> today.plusDays(i.toLong()) }
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(dates.size) { index ->
            val date = dates[index]
            // Doğru gün kısaltmasını al
            val dayName = when (date.dayOfWeek.value) {
                1 -> stringResource(id = R.string.day_short_monday)
                2 -> stringResource(id = R.string.day_short_tuesday)
                3 -> stringResource(id = R.string.day_short_wednesday)
                4 -> stringResource(id = R.string.day_short_thursday)
                5 -> stringResource(id = R.string.day_short_friday)
                6 -> stringResource(id = R.string.day_short_saturday)
                7 -> stringResource(id = R.string.day_short_sunday)
                else -> ""
            }

            // Gün kartı
            Card(
                modifier = Modifier
                    .padding(4.dp)
                    .width(50.dp)
                    .height(70.dp)
                    .clickable(onClick = { onDateSelect(date) }),
                colors = CardDefaults.cardColors(
                    containerColor = if (date.isEqual(selectedDate)) 
                                       MaterialTheme.colorScheme.primaryContainer 
                                     else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}