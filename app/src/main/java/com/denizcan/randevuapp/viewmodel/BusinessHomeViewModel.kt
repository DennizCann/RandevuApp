package com.denizcan.randevuapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denizcan.randevuapp.model.User
import com.denizcan.randevuapp.model.Appointment
import com.denizcan.randevuapp.model.AppointmentStatus
import com.denizcan.randevuapp.service.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.LocalDateTime

class BusinessHomeViewModel : ViewModel() {
    private val firebaseService = FirebaseService()
    
    private val _uiState = MutableStateFlow<BusinessHomeState>(BusinessHomeState.Loading)
    val uiState = _uiState.asStateFlow()

    private var currentBusinessId: String? = null

    private val _calendarState = MutableStateFlow<CalendarState>(CalendarState.Loading)
    val calendarState = _calendarState.asStateFlow()

    sealed class BusinessHomeState {
        object Loading : BusinessHomeState()
        data class Success(
            val business: User.Business,
            val pendingAppointments: Int
        ) : BusinessHomeState()
        data class Error(val message: String) : BusinessHomeState()
    }

    sealed class CalendarState {
        object Loading : CalendarState()
        data class Success(
            val appointments: List<Appointment>,
            val availableTimeSlots: List<String>,
            val selectedDate: LocalDate = LocalDate.now()
        ) : CalendarState()
        data class Error(val message: String) : CalendarState()
    }

    sealed class AppointmentRequestsState {
        object Loading : AppointmentRequestsState()
        data class Success(
            val appointments: List<Appointment>
        ) : AppointmentRequestsState()
        data class Error(val message: String) : AppointmentRequestsState()
    }

    private val _appointmentRequestsState = MutableStateFlow<AppointmentRequestsState>(AppointmentRequestsState.Loading)
    val appointmentRequestsState = _appointmentRequestsState.asStateFlow()

    private val _workingHoursState = MutableStateFlow<WorkingHoursState>(WorkingHoursState.Loading)
    val workingHoursState = _workingHoursState.asStateFlow()

    sealed class WorkingHoursState {
        object Loading : WorkingHoursState()
        data class Success(
            val message: String,
            val workingDays: List<String>,
            val workingHours: User.WorkingHours
        ) : WorkingHoursState()
        data class Error(val message: String) : WorkingHoursState()
    }

    // İki ayrı güncelleme metodu ve bir kaydetme metodu
    private var updatedWorkingDays: List<String>? = null
    private var updatedWorkingHours: User.WorkingHours? = null

    fun updateWorkingDays(days: List<String>) {
        updatedWorkingDays = days
    }

    fun updateWorkingHours(hours: User.WorkingHours) {
        updatedWorkingHours = hours
    }

    fun saveWorkingHoursAndDays() {
        viewModelScope.launch {
            try {
                val days = updatedWorkingDays ?: return@launch
                val hours = updatedWorkingHours ?: return@launch
                
                _workingHoursState.value = WorkingHoursState.Loading
                
                // Firebase'e kaydetmeyi dene
                try {
                    firebaseService.updateWorkingHours(
                        businessId = currentBusinessId ?: "",
                        workingDays = days,
                        workingHours = hours
                    )
                    
                    // Başarı durumunu ayarla
                    _workingHoursState.value = WorkingHoursState.Success(
                        message = "Çalışma saatleri başarıyla güncellendi",
                        workingDays = days,
                        workingHours = hours
                    )
                    
                    // Yerel veriye ayarla (Firebase başarısız olsa bile UI güncellensin)
                    val currentState = _uiState.value
                    if (currentState is BusinessHomeState.Success) {
                        val updatedBusiness = currentState.business.copy(
                            workingDays = days,
                            workingHours = hours
                        )
                        _uiState.value = BusinessHomeState.Success(
                            business = updatedBusiness,
                            pendingAppointments = currentState.pendingAppointments
                        )
                    }
                } catch (e: Exception) {
                    Log.e("BusinessHomeViewModel", "Firebase güncelleme hatası: ${e.message}")
                    
                    // Yine de yerel olarak güncelle (offline işlem)
                    val currentState = _uiState.value
                    if (currentState is BusinessHomeState.Success) {
                        val updatedBusiness = currentState.business.copy(
                            workingDays = days,
                            workingHours = hours
                        )
                        _uiState.value = BusinessHomeState.Success(
                            business = updatedBusiness,
                            pendingAppointments = currentState.pendingAppointments
                        )
                        
                        // Offline başarı durumu
                        _workingHoursState.value = WorkingHoursState.Success(
                            message = "Çalışma saatleri yerel olarak güncellendi (offline mod)",
                            workingDays = days,
                            workingHours = hours
                        )
                    }
                }
            } catch (e: Exception) {
                _workingHoursState.value = WorkingHoursState.Error(
                    message = "Çalışma saatleri güncellenirken hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun loadBusinessData(businessId: String) {
        currentBusinessId = businessId  // ID'yi sakla
        viewModelScope.launch {
            try {
                _uiState.value = BusinessHomeState.Loading
                val business = firebaseService.getBusinessById(businessId)
                if (business != null) {
                    val pendingAppointments = firebaseService.getPendingAppointmentsCount(businessId)
                    _uiState.value = BusinessHomeState.Success(
                        business = business,
                        pendingAppointments = pendingAppointments
                    )
                } else {
                    _uiState.value = BusinessHomeState.Error("İşletme bilgileri yüklenemedi")
                }
            } catch (e: Exception) {
                _uiState.value = BusinessHomeState.Error(e.message ?: "Bir hata oluştu")
            }
        }
    }

    fun updateWorkingHours(workingDays: List<String>, workingHours: User.WorkingHours) {
        viewModelScope.launch {
            try {
                // İstek göndermeden önce yükleniyor durumunu ayarla
                _workingHoursState.value = WorkingHoursState.Loading
                
                // Güncellenmiş çalışma saatleri
                val updatedWorkingHours = User.WorkingHours(
                    opening = workingHours.opening,
                    closing = workingHours.closing,
                    slotDuration = workingHours.slotDuration
                )
                
                // Log ekleyelim
                Log.d("BusinessHomeViewModel", "Güncellenecek çalışma günleri: $workingDays")
                Log.d("BusinessHomeViewModel", "Güncellenecek çalışma saatleri: $updatedWorkingHours")
                
                // Firebase'e kaydet
                firebaseService.updateWorkingHours(
                    businessId = currentBusinessId ?: "",
                    workingDays = workingDays,
                    workingHours = updatedWorkingHours
                )
                
                // Business nesnesini güncelle (load Business fonksiyonu çağrılabilir veya yerel state'i güncelleyebiliriz)
                loadBusinessData(currentBusinessId ?: "")
                
                // Success state'e güncelle
                _workingHoursState.value = WorkingHoursState.Success(
                    message = "Çalışma saatleri başarıyla güncellendi",
                    workingDays = workingDays,
                    workingHours = updatedWorkingHours
                )
            } catch (e: Exception) {
                Log.e("BusinessHomeViewModel", "Çalışma saatleri güncellenirken hata: ${e.message}")
                _workingHoursState.value = WorkingHoursState.Error(
                    message = "Çalışma saatleri güncellenirken hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun loadAppointments(date: LocalDate) {
        viewModelScope.launch {
            try {
                currentBusinessId?.let { businessId ->
                    _calendarState.value = CalendarState.Loading
                    
                    // İşletme bilgilerini al
                    val business = firebaseService.getBusinessById(businessId)
                    
                    // Tüm randevuları getir
                    val appointments = firebaseService.getAppointmentsByDate(businessId, date)
                    
                    // İşletmenin çalışma saatlerine göre tüm zaman aralıklarını oluştur
                    val availableTimeSlots = if (business != null) {
                        calculateTimeSlots(business, date)
                    } else {
                        emptyList()
                    }
                    
                    _calendarState.value = CalendarState.Success(
                        appointments = appointments,
                        availableTimeSlots = availableTimeSlots,
                        selectedDate = date
                    )
                }
            } catch (e: Exception) {
                _calendarState.value = CalendarState.Error(e.message ?: "Randevular yüklenemedi")
            }
        }
    }

    private fun calculateTimeSlots(business: User.Business, date: LocalDate): List<String> {
        // İşletmenin o gün çalışıp çalışmadığını kontrol et
        if (!business.workingDays.contains(date.dayOfWeek.name)) {
            return emptyList()
        }
        
        val workingHours = business.workingHours
        val startTime = LocalTime.parse(workingHours.opening)
        val endTime = LocalTime.parse(workingHours.closing)
        val slotDuration = workingHours.slotDuration
        
        // Tüm zaman dilimlerini oluştur
        val timeSlots = mutableListOf<String>()
        var currentTime = startTime
        
        while (currentTime.plusMinutes(slotDuration.toLong()) <= endTime) {
            timeSlots.add(currentTime.format(DateTimeFormatter.ofPattern("HH:mm")))
            currentTime = currentTime.plusMinutes(slotDuration.toLong())
        }
        
        return timeSlots
    }

    fun blockTimeSlot(date: LocalDate, timeSlot: String) {
        viewModelScope.launch {
            try {
                currentBusinessId?.let { businessId ->
                    // Zaman dilimini "HH:mm" formatından LocalDateTime'a çevir
                    val timeParts = timeSlot.split(":")
                    val dateTime = date.atTime(timeParts[0].toInt(), timeParts[1].toInt())
                    
                    // İşletme adını al
                    val business = firebaseService.getBusinessById(businessId)
                    
                    // Zaman dilimini kapat (BLOCKED olarak işaretle)
                    firebaseService.createBlockedAppointment(
                        businessId = businessId,
                        businessName = business?.businessName ?: "Bilinmeyen İşletme",
                        dateTime = dateTime
                    )
                    
                    // Takvimi güncelle
                    loadAppointments(date)
                }
            } catch (e: Exception) {
                _calendarState.value = CalendarState.Error(e.message ?: "Zaman dilimi kapatılamadı")
            }
        }
    }

    fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus) {
        viewModelScope.launch {
            try {
                // Eğer CANCELLED ise randevuyu tamamen sil
                if (status == AppointmentStatus.CANCELLED) {
                    firebaseService.deleteAppointment(appointmentId)
                } else {
                    // Diğer durumlar için normal güncelleme yap
                    firebaseService.updateAppointmentStatus(appointmentId, status)
                }
                
                // Hem randevu taleplerini hem de takvimi güncelle
                val currentRequestsState = _appointmentRequestsState.value
                if (currentRequestsState is AppointmentRequestsState.Success) {
                    loadAppointmentRequests(currentRequestsState.appointments.firstOrNull()?.businessId ?: currentBusinessId ?: "")
                }

                // Takvimi güncelle
                val currentCalendarState = _calendarState.value
                if (currentCalendarState is CalendarState.Success) {
                    loadAppointments(currentCalendarState.selectedDate)
                }

                // Ana sayfadaki bekleyen randevu sayısını güncelle
                currentBusinessId?.let { businessId ->
                    loadBusinessData(businessId)
                }
            } catch (e: Exception) {
                _appointmentRequestsState.value = AppointmentRequestsState.Error(e.message ?: "Randevu durumu güncellenemedi")
            }
        }
    }

    fun loadAppointmentRequests(businessId: String) {
        viewModelScope.launch {
            try {
                _appointmentRequestsState.value = AppointmentRequestsState.Loading
                val appointments = firebaseService.getAppointmentRequests(businessId)
                _appointmentRequestsState.value = AppointmentRequestsState.Success(appointments)
            } catch (e: Exception) {
                _appointmentRequestsState.value = AppointmentRequestsState.Error(e.message ?: "Randevu talepleri yüklenemedi")
            }
        }
    }

    fun unblockTimeSlot(appointmentId: String) {
        viewModelScope.launch {
            try {
                // Kapatılmış zaman dilimini tamamen sil
                firebaseService.deleteAppointment(appointmentId)
                
                // Takvimi güncelle
                val currentState = _calendarState.value
                if (currentState is CalendarState.Success) {
                    loadAppointments(currentState.selectedDate)
                }
            } catch (e: Exception) {
                _calendarState.value = CalendarState.Error(e.message ?: "Zaman dilimi açılamadı")
            }
        }
    }

    fun cancelAppointment(appointmentId: String) {
        viewModelScope.launch {
            try {
                // Randevuyu tamamen sil
                firebaseService.deleteAppointment(appointmentId)
                
                // Takvimi güncelle
                val currentState = _calendarState.value
                if (currentState is CalendarState.Success) {
                    loadAppointments(currentState.selectedDate)
                }
            } catch (e: Exception) {
                _calendarState.value = CalendarState.Error(e.message ?: "Randevu iptal edilemedi")
            }
        }
    }

    private fun generateTimeSlots(date: LocalDate, opening: String, closing: String, slotDuration: Int): List<org.threeten.bp.LocalDateTime> {
        try {
            val startTime = LocalTime.parse(opening)
            val endTime = LocalTime.parse(closing)
            
            val timeSlots = mutableListOf<org.threeten.bp.LocalDateTime>()
            var currentTime = startTime
            
            while (currentTime.plusMinutes(slotDuration.toLong()) <= endTime) {
                val dateTime = date.atTime(currentTime)
                timeSlots.add(dateTime)
                currentTime = currentTime.plusMinutes(slotDuration.toLong())
            }
            
            return timeSlots
        } catch (e: Exception) {
            println("Zaman aralıkları oluşturulurken hata: ${e.message}")
            return emptyList()
        }
    }
} 