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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.denizcan.randevuapp.R
import com.denizcan.randevuapp.ui.components.AppTopBar
import com.denizcan.randevuapp.viewmodel.BusinessDetailViewModel
import com.denizcan.randevuapp.viewmodel.BusinessDetailViewModel.BusinessDetailState


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
    isLoading: Boolean = false,
    viewModel: BusinessDetailViewModel,
    customerId: String
) {
    var selectedTimeSlot by remember { mutableStateOf<String?>(null) }
    var appointmentNote by remember { mutableStateOf("") }

    val today = LocalDate.now()
    val dateRange = generateDateRange(today, 14)

    val uiState = viewModel.uiState.collectAsState().value

    Scaffold(
        topBar = {
            AppTopBar(
                title = business.businessName,
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Business Information
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEEEEEE))
                        .padding(16.dp)
                ) {
                    Column {
                        if (business.address.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.address_colon, business.address),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        if (business.phone.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.phone_number, business.phone),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Date Selection
            item {
                Text(
                    text = stringResource(R.string.select_date),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dateRange) { date ->
                        val isSelected = date == selectedDate

                        // English day abbreviations
                        val dayName = when (date.dayOfWeek.toString()) {
                            "MONDAY" -> "Mon"
                            "TUESDAY" -> "Tue"
                            "WEDNESDAY" -> "Wed"
                            "THURSDAY" -> "Thu"
                            "FRIDAY" -> "Fri"
                            "SATURDAY" -> "Sat"
                            "SUNDAY" -> "Sun"
                            else -> ""
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else Color.White
                                )
                                .clickable { onDateSelect(date) }
                                .padding(8.dp)
                        ) {
                            Text(
                                text = dayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) Color.White else Color.Black
                            )

                            Text(
                                text = date.dayOfMonth.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Time Selection
            item {
                Text(
                    text = stringResource(R.string.select_time),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.available_slots),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (availableSlots.isEmpty()) {
                    // Check if the selected day is a working day
                    val workingDayName = selectedDate.dayOfWeek.name
                    val isWorkingDay = business.workingDays.contains(workingDayName)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isWorkingDay)
                                stringResource(R.string.no_slots_available)
                            else
                                stringResource(R.string.closed_on_selected_day),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    FlowRow(
                        maxItemsInEachRow = 4,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableSlots.forEach { slot ->
                            TimeSlotItem(
                                slot = slot,
                                isSelected = slot == selectedTimeSlot,
                                onSelect = {
                                    selectedTimeSlot = slot
                                    onTimeSelect(slot)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Appointment Note
            item {
                Text(
                    text = stringResource(R.string.note),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = appointmentNote,
                    onValueChange = {
                        appointmentNote = it
                        onNoteChange(it)
                    },
                    placeholder = { Text(stringResource(R.string.appointment_note_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Request Appointment Button
            item {
                Button(
                    onClick = onAppointmentRequest,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedDate != null && selectedTimeSlot != null && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(R.string.request_appointment))
                    }
                }
            }
        }
    }

    when (uiState) {
        is BusinessDetailState.Loading -> {
            // Yükleniyor...
        }
        is BusinessDetailState.Success -> {
            // BusinessDetailScreen sayfasında state.availableSlots'u kullanıyoruz
            // Bu listeye kapatılmış slotlar dahil olmamalı 
        }
        // ...
        is BusinessDetailState.Error -> TODO()
    }
}

@Composable
fun TimeSlotItem(
    slot: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else Color(0xFFEDEDED)  // Light gray
            )
            .clickable { onSelect() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = slot,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) Color.White else Color.Black
        )
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

@Composable
private fun BusinessInfoSection(business: User.Business) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = business.businessName,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.address_colon, business.address),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(id = R.string.phone_number, business.phone),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(
                    id = R.string.working_hours_full,
                    business.workingHours.opening,
                    business.workingHours.closing
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = stringResource(
                    id = R.string.appointment_duration,
                    business.workingHours.slotDuration
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(id = R.string.working_days_colon),
                style = MaterialTheme.typography.bodyMedium
            )

            // İşletmenin çalışma günleri
            business.workingDays.forEach { day ->
                val dayName = when (day) {
                    "MONDAY" -> stringResource(id = R.string.day_monday)
                    "TUESDAY" -> stringResource(id = R.string.day_tuesday)
                    "WEDNESDAY" -> stringResource(id = R.string.day_wednesday)
                    "THURSDAY" -> stringResource(id = R.string.day_thursday)
                    "FRIDAY" -> stringResource(id = R.string.day_friday)
                    "SATURDAY" -> stringResource(id = R.string.day_saturday)
                    "SUNDAY" -> stringResource(id = R.string.day_sunday)
                    else -> day
                }
                Text("• $dayName", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun DateSelectionSection(
    selectedDate: LocalDate,
    onDateSelect: (LocalDate) -> Unit,
    workingDays: List<String>
) {
    val currentLocale = LocalContext.current.resources.configuration.locales.get(0)
    val dates = generateDateRange(LocalDate.now(), 14)

    Column {
        Text(
            text = stringResource(id = R.string.select_date),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dates) { date ->
                val isSelected = date == selectedDate
                val isWorkingDay = workingDays.contains(date.dayOfWeek.name)
                val backgroundColor = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    !isWorkingDay -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val textColor = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    !isWorkingDay -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 70.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(backgroundColor)
                        .clickable { onDateSelect(date) }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = date.dayOfWeek.name.take(3),
                            style = MaterialTheme.typography.labelMedium,
                            color = textColor
                        )
                        Text(
                            text = date.format(DateTimeFormatter.ofPattern("d", currentLocale)),
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeSlotSection(
    availableSlots: List<String>,
    selectedSlot: String,
    onSlotSelect: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(availableSlots) { slot ->
            val isSelected = slot == selectedSlot

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onSlotSelect(slot) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = slot,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun generateDateRange(startDate: LocalDate, days: Int): List<LocalDate> {
    return (0 until days).map { startDate.plusDays(it.toLong()) }
}

private fun formatLocalDate(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr"))
    return date.format(formatter)
} 