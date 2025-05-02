package com.denizcan.randevuapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denizcan.randevuapp.R
import com.denizcan.randevuapp.model.Appointment
import com.denizcan.randevuapp.model.AppointmentStatus
import com.denizcan.randevuapp.ui.components.AppTopBar
import org.threeten.bp.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentRequestsScreen(
    appointments: List<Appointment>,
    onStatusChange: (String, AppointmentStatus) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.appointment_requests),
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        if (appointments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.no_requests),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(appointments.filter { it.status == AppointmentStatus.PENDING }) { appointment ->
                    AppointmentRequestCard(
                        appointment = appointment,
                        onStatusChange = onStatusChange
                    )
                }
            }
        }
    }
}

@Composable
private fun AppointmentRequestCard(
    appointment: Appointment,
    onStatusChange: (String, AppointmentStatus) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = appointment.businessName.ifEmpty { "İşletme: ${appointment.businessId}" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(
                    id = R.string.request_from,
                    appointment.customerName.ifEmpty { appointment.customerId }
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = stringResource(
                    id = R.string.date,
                    appointment.dateTime.toLocalDate().format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
                )
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = stringResource(
                    id = R.string.time,
                    appointment.dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                )
            )
            
            if (appointment.note.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Not: ${appointment.note}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = {
                        onStatusChange(appointment.id, AppointmentStatus.CANCELLED)
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(stringResource(id = R.string.reject))
                }
                
                Button(
                    onClick = {
                        onStatusChange(appointment.id, AppointmentStatus.CONFIRMED)
                    }
                ) {
                    Text(stringResource(id = R.string.approve))
                }
            }
        }
    }
} 