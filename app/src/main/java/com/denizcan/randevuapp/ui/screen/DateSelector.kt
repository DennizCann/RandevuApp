package com.denizcan.randevuapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.threeten.bp.LocalDate
import org.threeten.bp.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelector(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateCard(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(80.dp)
            .height(90.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = date.dayOfWeek.getDisplayName(
                    TextStyle.SHORT,
                    Locale("tr")
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
} 