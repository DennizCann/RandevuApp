package com.denizcan.randevuapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.denizcan.randevuapp.R
import com.denizcan.randevuapp.model.Appointment
import com.denizcan.randevuapp.model.AppointmentStatus
import com.denizcan.randevuapp.ui.components.AppTopBar
import org.threeten.bp.format.DateTimeFormatter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerAppointmentsScreen(
    appointments: List<Appointment>,
    onCancelAppointment: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.my_appointments_title),
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
            if (appointments.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(id = R.string.no_appointments_yet))
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppointmentCard(
    appointment: Appointment,
    onCancelAppointment: (String) -> Unit
) {
    val currentLocale = LocalContext.current.resources.configuration.locales.get(0)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
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
                text = stringResource(
                    id = R.string.date,
                    appointment.dateTime.toLocalDate().format(DateTimeFormatter.ofPattern("d MMMM yyyy", currentLocale))
                ),
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = stringResource(
                    id = R.string.time,
                    appointment.dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = appointment.businessName.ifEmpty { appointment.businessId },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            val statusTextId = when (appointment.status) {
                AppointmentStatus.PENDING -> R.string.appointment_status_pending
                AppointmentStatus.CONFIRMED -> R.string.appointment_status_confirmed
                AppointmentStatus.CANCELLED -> R.string.appointment_status_cancelled
                AppointmentStatus.COMPLETED -> R.string.status_completed
                AppointmentStatus.BLOCKED -> R.string.status_blocked
            }
            
            val statusText = stringResource(id = statusTextId)
            
            val statusColor = when (appointment.status) {
                AppointmentStatus.PENDING -> MaterialTheme.colorScheme.primary
                AppointmentStatus.CONFIRMED -> MaterialTheme.colorScheme.tertiary
                AppointmentStatus.CANCELLED -> MaterialTheme.colorScheme.error
                AppointmentStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
                AppointmentStatus.BLOCKED -> Color.Gray
            }
            
            Text(
                text = statusText,
                color = statusColor,
                style = MaterialTheme.typography.bodyMedium
            )

            if (appointment.status == AppointmentStatus.CONFIRMED || 
                appointment.status == AppointmentStatus.PENDING) {
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { onCancelAppointment(appointment.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(id = R.string.cancel_appointment))
                }
            }
        }
    }
} 