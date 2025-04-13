package com.denizcan.randevuapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denizcan.randevuapp.model.Appointment
import com.denizcan.randevuapp.model.User
import com.denizcan.randevuapp.service.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

class BusinessDetailViewModel : ViewModel() {
    private val firebaseService = FirebaseService()
    
    private val _uiState = MutableStateFlow<BusinessDetailState>(BusinessDetailState.Loading)
    val uiState = _uiState.asStateFlow()

    private var selectedDateTime: LocalDateTime? = null
    private var appointmentNote: String = ""

    sealed class BusinessDetailState {
        object Loading : BusinessDetailState()
        data class Success(
            val business: User.Business,
            val availableSlots: List<String>,
            val selectedDate: LocalDate = LocalDate.now()
        ) : BusinessDetailState()
        data class Error(val message: String) : BusinessDetailState()
    }

    fun loadBusinessDetail(businessId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = BusinessDetailState.Loading
                val business = firebaseService.getBusinessById(businessId)
                if (business != null) {
                    // TODO: Müsait saatleri yükle
                    val availableSlots = listOf<String>() // Şimdilik boş liste
                    _uiState.value = BusinessDetailState.Success(
                        business = business,
                        availableSlots = availableSlots
                    )
                } else {
                    _uiState.value = BusinessDetailState.Error("İşletme bulunamadı")
                }
            } catch (e: Exception) {
                _uiState.value = BusinessDetailState.Error(e.message ?: "Bir hata oluştu")
            }
        }
    }

    private fun calculateAvailableSlots(
        business: User.Business,
        date: LocalDate,
        appointments: List<Appointment>
    ): List<String> {
        // Eğer seçilen gün işletmenin çalışma günlerinden biri değilse boş liste döndür
        if (!business.workingDays.contains(date.dayOfWeek.name)) {
            return emptyList()
        }

        val workingHours = business.workingHours
        val startTime = LocalTime.parse(workingHours.startTime)
        val endTime = LocalTime.parse(workingHours.endTime)
        val slotDuration = workingHours.slotDuration

        // Tüm zaman dilimlerini oluştur
        val allSlots = mutableListOf<String>()
        var currentTime = startTime
        while (currentTime.plusMinutes(slotDuration.toLong()) <= endTime) {
            allSlots.add(currentTime.format(DateTimeFormatter.ofPattern("HH:mm")))
            currentTime = currentTime.plusMinutes(slotDuration.toLong())
        }

        // O güne ait randevuları filtrele
        val bookedSlots = appointments
            .filter { it.dateTime.toLocalDate() == date }
            .map { it.dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")) }

        // Müsait olmayan saatleri çıkar
        return allSlots.filter { slot -> !bookedSlots.contains(slot) }
    }

    fun loadAvailableSlots(date: LocalDate) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is BusinessDetailState.Success) {
                    val appointments = firebaseService.getAppointmentsByDate(
                        currentState.business.id,
                        date
                    )
                    val availableSlots = calculateAvailableSlots(
                        currentState.business,
                        date,
                        appointments
                    )
                    _uiState.value = currentState.copy(
                        availableSlots = availableSlots,
                        selectedDate = date
                    )
                }
            } catch (e: Exception) {
                _uiState.value = BusinessDetailState.Error(e.message ?: "Müsait saatler yüklenemedi")
            }
        }
    }

    fun updateSelectedDate(date: LocalDate) {
        selectedDateTime = null
        loadAvailableSlots(date)
    }

    fun updateSelectedTime(time: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is BusinessDetailState.Success) {
                val timeParts = time.split(":")
                selectedDateTime = currentState.selectedDate
                    .atTime(timeParts[0].toInt(), timeParts[1].toInt())
            }
        }
    }

    fun updateNote(note: String) {
        appointmentNote = note
    }

    fun createAppointment(customerId: String) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is BusinessDetailState.Success && selectedDateTime != null) {
                    firebaseService.createAppointment(
                        businessId = currentState.business.id,
                        customerId = customerId,
                        dateTime = selectedDateTime!!,
                        note = appointmentNote
                    )
                    // Başarılı mesajı göster veya navigasyon yap
                }
            } catch (e: Exception) {
                // Hata mesajı göster
            }
        }
    }
} 