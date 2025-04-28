package com.denizcan.randevuapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.denizcan.randevuapp.model.User
import com.denizcan.randevuapp.ui.components.AppTopBar
import com.denizcan.randevuapp.ui.components.SystemAwareScaffold
import com.denizcan.randevuapp.viewmodel.BusinessHomeViewModel
import kotlinx.coroutines.delay
import com.denizcan.randevuapp.R
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkingHoursScreen(
    workingDays: List<String>,
    workingHours: User.WorkingHours,
    onWorkingDaysChange: (List<String>) -> Unit,
    onWorkingHoursChange: (User.WorkingHours) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    isLoading: Boolean,
    workingHoursState: BusinessHomeViewModel.WorkingHoursState
) {
    var localWorkingDays by remember { mutableStateOf(workingDays) }
    var opening by remember { mutableStateOf(workingHours.opening) }
    var closing by remember { mutableStateOf(workingHours.closing) }
    var slotDuration by remember { mutableStateOf(workingHours.slotDuration.toString()) }

    // Değişiklik yapıldığında kontrol için
    val hasChanges = localWorkingDays != workingDays ||
            opening != workingHours.opening ||
            closing != workingHours.closing ||
            slotDuration.toIntOrNull() != workingHours.slotDuration

    // İşlem süresi fazla uzarsa otomatik zaman aşımına uğratsın
    val isLoading by remember { mutableStateOf(false) }
    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(5000) // 5 saniye sonra
            if (isLoading) {
                // Hala yükleniyor durumundaysa zaman aşımı
                onBackClick()
            }
        }
    }

    SystemAwareScaffold(
        title = stringResource(id = R.string.working_hours),
        onBackClick = onBackClick,
        bottomButtonText = stringResource(id = R.string.save_changes),
        isBottomButtonEnabled = hasChanges, // Değişiklik varsa aktif et
        isBottomButtonVisible = true, // Butonun görünürlüğünü zorla
        isLoading = isLoading,
        onBottomButtonClick = {
            // Yerel değişiklikleri ViewModel'e gönder
            onWorkingDaysChange(localWorkingDays)
            onWorkingHoursChange(
                User.WorkingHours(
                    opening = opening,
                    closing = closing,
                    slotDuration = slotDuration.toIntOrNull() ?: 30
                )
            )
            onSaveClick()
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Çalışma günleri seçimi
            Text(
                text = stringResource(id = R.string.working_days),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            DaySelectionSection(
                selectedDays = localWorkingDays,
                onSelectionChange = { day, isSelected ->
                    localWorkingDays = if (isSelected) {
                        localWorkingDays + day
                    } else {
                        localWorkingDays - day
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Çalışma saatleri ayarı
            Text(
                text = stringResource(id = R.string.working_hours),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Açılış saati
            OutlinedTextField(
                value = opening,
                onValueChange = { opening = it },
                label = { Text(stringResource(id = R.string.opening_time)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Kapanış saati
            OutlinedTextField(
                value = closing,
                onValueChange = { closing = it },
                label = { Text(stringResource(id = R.string.closing_time)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Randevu süresi
            OutlinedTextField(
                value = slotDuration,
                onValueChange = { slotDuration = it },
                label = { Text(stringResource(id = R.string.slot_duration)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Debug bilgileri (seçimli)
            Text(
                text = stringResource(id = R.string.selected_days) + ": " + localWorkingDays.joinToString(", "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // Durum mesajını göster
    if (workingHoursState is BusinessHomeViewModel.WorkingHoursState.Error) {
        Snackbar(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = workingHoursState.message)
        }
    } else if (workingHoursState is BusinessHomeViewModel.WorkingHoursState.Success) {
        // Başarılı mesajı
        LaunchedEffect(workingHoursState) {
            // Başarı mesajı gösterdikten sonra geri dön
            delay(1000)
            onBackClick()
        }
    }
}

private val daysOfWeek = listOf(
    "Pazartesi",
    "Salı",
    "Çarşamba",
    "Perşembe",
    "Cuma",
    "Cumartesi",
    "Pazar"
)

@Composable
private fun DaySelectionSection(
    selectedDays: List<String>,
    onSelectionChange: (String, Boolean) -> Unit
) {
    val allDays = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        allDays.forEach { day ->
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.bodyMedium
                )

                Checkbox(
                    checked = selectedDays.contains(day),
                    onCheckedChange = { isChecked ->
                        onSelectionChange(day, isChecked)
                    }
                )
            }
        }
    }
} 