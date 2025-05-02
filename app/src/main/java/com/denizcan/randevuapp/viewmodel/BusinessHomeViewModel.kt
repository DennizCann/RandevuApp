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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.math.ceil

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

    private fun validateWorkingHours(hours: User.WorkingHours): String? {
        try {
            val startTime = LocalTime.parse(hours.opening)
            val endTime = LocalTime.parse(hours.closing)
            
            // Kapanış saati açılış saatinden sonra olmalı
            if (endTime <= startTime) {
                // Özel durum: 24 saat çalışma durumu (örneğin 00:00 - 00:00 veya benzeri)
                if (startTime == LocalTime.of(0, 0) && endTime == LocalTime.of(0, 0)) {
                    // 24 saat çalışma durumunu kabul et
                } else {
                    return "Kapanış saati açılış saatinden sonra olmalıdır"
                }
            }
            
            // Çalışma süresi 24 saate kadar olabilir
            val duration = org.threeten.bp.Duration.between(startTime, endTime)
            val minutesDiff = duration.toMinutes()
            
            // Eğer startTime > endTime ise bu genellikle gece yarısını geçen bir durum anlamına gelir
            // (örn. 22:00 - 04:00) - Bu durumda süreyi farklı hesaplamalıyız
            val adjustedMinutesDiff = if (startTime > endTime && endTime != LocalTime.of(0, 0)) {
                // Gece yarısına kadar + gece yarısından sonra
                val minutesToMidnight = org.threeten.bp.Duration.between(startTime, LocalTime.of(23, 59, 59)).toMinutes() + 1
                val minutesFromMidnight = org.threeten.bp.Duration.between(LocalTime.of(0, 0), endTime).toMinutes()
                minutesToMidnight + minutesFromMidnight
            } else {
                minutesDiff
            }
            
            // 24 saat = 1440 dakika
            if (adjustedMinutesDiff > 24 * 60 && endTime != LocalTime.of(0, 0)) {
                return "Çalışma süresi 24 saatten fazla olamaz"
            }
            
            // Randevu süresi makul olmalı
            if (hours.slotDuration < 5) {
                return "Randevu süresi en az 5 dakika olmalıdır"
            }
            
            if (hours.slotDuration > 240) { // 4 saat
                return "Randevu süresi en fazla 4 saat olabilir"
            }
            
            return null // Validasyon başarılı
        } catch (e: Exception) {
            return "Geçersiz saat formatı: ${e.message}"
        }
    }

    fun saveWorkingHours() {
        viewModelScope.launch {
            try {
                _workingHoursState.value = WorkingHoursState.Loading

                // İşletme ID'si kontrolü
                val businessId = currentBusinessId
                if (businessId == null) {
                    _workingHoursState.value = WorkingHoursState.Error("İşletme ID'si bulunamadı")
                    return@launch
                }

                // Kaydetmeden önce veri doğrulaması
                val workingDays = updatedWorkingDays
                if (workingDays.isNullOrEmpty()) {
                    _workingHoursState.value = WorkingHoursState.Error("En az bir çalışma günü seçmelisiniz")
                    return@launch
                }

                val workingHours = updatedWorkingHours
                if (workingHours == null || workingHours.opening.isEmpty() || workingHours.closing.isEmpty()) {
                    _workingHoursState.value = WorkingHoursState.Error("Açılış ve kapanış saatleri gereklidir")
                    return@launch
                }
                
                // Yeni eklenen validasyon kontrolü
                val validationError = validateWorkingHours(workingHours)
                if (validationError != null) {
                    _workingHoursState.value = WorkingHoursState.Error(validationError)
                    return@launch
                }

                // Non-nullable değerler ile Map oluşturma
                val workingHoursData = mapOf<String, Any>(
                    "opening" to workingHours.opening,
                    "closing" to workingHours.closing,
                    "slotDuration" to workingHours.slotDuration
                )

                // Doğru veri formatlama (Any? -> Any dönüşümü yaparak)
                val dataToUpdate = mapOf<String, Any>(
                    "workingDays" to workingDays,
                    "workingHours" to workingHoursData
                )

                // Detaylı loglama
                Log.d("BusinessHome", "Kaydedilecek veriler: $dataToUpdate")
                Log.d("BusinessHome", "BusinessID: $businessId")

                // Veri tabanına kaydet - nullable olmayan Map kullanarak
                firebaseService.updateBusinessData(businessId, dataToUpdate)

                // İşletmeyi tekrar yükle
                loadBusinessData(businessId)

                _workingHoursState.value = WorkingHoursState.Success(
                    message = "Çalışma saatleri başarıyla kaydedildi",
                    workingDays = workingDays,
                    workingHours = workingHours
                )

                // Kaydettikten sonra veritabanından tekrar alarak doğrulama
                val updatedDoc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(businessId)
                    .get().await()

                Log.d("BusinessHome", "Kaydedilen veri (doğrulama): ${updatedDoc.data}")

            } catch (e: Exception) {
                Log.e("BusinessHome", "Çalışma saatleri kaydedilemedi", e)
                _workingHoursState.value = WorkingHoursState.Error("Çalışma saatleri kaydedilemedi: ${e.message}")
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
                        calculateTimeSlots(date, business.workingHours, business.workingDays)
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

    private fun isSameDay(time1: LocalTime, time2: LocalTime): Boolean {
        // Eğer time2, time1'den "önce" görünüyorsa, muhtemelen gün değişmiştir
        // Örneğin, time1 = 23:30, time2 = 00:01 ise, bu farklı günleri gösterir
        return time1.hour <= time2.hour || 
              (time1.hour == 23 && time2.hour == 0 && time1.minute <= time2.minute)
    }

    private fun calculateTimeSlots(date: LocalDate, workingHours: User.WorkingHours?, workingDays: List<String>): List<String> {
        try {
            // Gün çalışma günü değilse boş liste döndür
            if (!workingDays.contains(date.dayOfWeek.name)) {
                Log.d("BusinessHome", "Seçilen gün çalışma günü değil: ${date.dayOfWeek.name}")
                return emptyList()
            }
            
            // İş saatleri yoksa boş liste döndür
            if (workingHours == null) {
                Log.d("BusinessHome", "Çalışma saatleri tanımlanmamış")
                return emptyList()
            }
            
            try {
                val startTime = LocalTime.parse(workingHours.opening)
                val endTime = LocalTime.parse(workingHours.closing)
                val slotDuration = workingHours.slotDuration.toLong()
                
                // Başlangıç ve bitiş saatleri arasındaki dakika farkını hesapla
                val minutesBetween = org.threeten.bp.Duration.between(startTime, endTime).toMinutes()
                
                // Gece yarısını geçen durumlar için ayarlama yap
                val adjustedMinutesBetween = if (endTime.isBefore(startTime)) {
                    org.threeten.bp.Duration.between(startTime, LocalTime.of(23, 59)).toMinutes() + 1
                } else {
                    minutesBetween
                }
                
                // ÖNEMLİ: 23:59 kapanışı için son slotu (23:30) eklemek özel bir durum gerektirir
                val isSpecialEndTime = endTime.equals(LocalTime.of(23, 59))
                
                // Toplam slot sayısını yukarı yuvarlayarak hesapla
                val totalSlots = if (isSpecialEndTime) {
                    Math.ceil(adjustedMinutesBetween.toDouble() / slotDuration).toInt()
                } else {
                    (adjustedMinutesBetween / slotDuration).toInt()
                }
                
                Log.d("BusinessHome", "Toplam randevu aralığı sayısı: $totalSlots (${startTime}-${endTime}, ${slotDuration}dk aralıklarla)")
                
                // *** LİMİT KALDIRILDI: Artık maksimum slot sayısı sınırı yok ***
                val slots = mutableListOf<String>()
                
                // Tüm slotları oluştur
                for (i in 0 until totalSlots) {
                    val slotTime = startTime.plus(i * slotDuration, org.threeten.bp.temporal.ChronoUnit.MINUTES)
                    
                    // Sadece bir güvenlik kontrolü: Hesaplanan zaman 23:59'u geçmemeli
                    if (slotTime.isAfter(LocalTime.of(23, 59))) {
                        Log.d("BusinessHome", "Gün sınırını aşan zaman dilimi: $slotTime - döngü sonlandırılıyor")
                        break
                    }
                    
                    slots.add(slotTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                }
                
                // Kontrol: Eğer son slot 23:30 olarak bekleniyor ama eklenmemişse manuel ekle
                if (isSpecialEndTime && workingHours.slotDuration == 30 && 
                    (slots.isEmpty() || slots.last() != "23:30")) {
                    slots.add("23:30")
                    Log.d("BusinessHome", "Son slot (23:30) manuel olarak eklendi")
                }
                
                return slots
                
            } catch (e: Exception) {
                Log.e("BusinessHome", "Zaman formatı hatası: ${e.message}", e)
                return emptyList()
            }
        } catch (e: Exception) {
            Log.e("BusinessHome", "Zaman aralıkları oluşturulurken hata: ${e.message}", e)
            return emptyList()
        }
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
            try {
                val startTime = LocalTime.parse(opening)
                val endTime = LocalTime.parse(closing)
                
                // Başlangıç ve bitiş saatleri arasındaki dakika farkını hesapla
                val minutesBetween = org.threeten.bp.Duration.between(startTime, endTime).toMinutes()
                
                // Gece yarısını geçen durumlar için ayarlama yap
                val adjustedMinutesBetween = if (endTime.isBefore(startTime)) {
                    org.threeten.bp.Duration.between(startTime, LocalTime.of(23, 59)).toMinutes() + 1
                } else {
                    minutesBetween
                }
                
                // 23:59 kapanışı için özel durum
                val isSpecialEndTime = endTime.equals(LocalTime.of(23, 59))
                
                // Toplam slot sayısını hesapla
                val totalSlots = if (isSpecialEndTime) {
                    Math.ceil(adjustedMinutesBetween.toDouble() / slotDuration).toInt()
                } else {
                    (adjustedMinutesBetween / slotDuration).toInt()
                }
                
                // *** LİMİT KALDIRILDI ***
                val timeSlots = mutableListOf<org.threeten.bp.LocalDateTime>()
                
                // Tüm slotları oluştur
                for (i in 0 until totalSlots) {
                    val slotTime = startTime.plus(i * slotDuration.toLong(), org.threeten.bp.temporal.ChronoUnit.MINUTES)
                    
                    if (slotTime.isAfter(LocalTime.of(23, 59))) {
                        break
                    }
                    
                    val dateTime = date.atTime(slotTime)
                    timeSlots.add(dateTime)
                }
                
                // 23:30 slot kontrolü
                if (isSpecialEndTime && slotDuration == 30) {
                    val lastSlotTime = LocalTime.of(23, 30)
                    val exists = timeSlots.any { it.toLocalTime() == lastSlotTime }
                    
                    if (!exists) {
                        timeSlots.add(date.atTime(lastSlotTime))
                        Log.d("BusinessHome", "Son LocalDateTime slot (23:30) manuel olarak eklendi")
                    }
                }
                
                return timeSlots
                
            } catch (e: Exception) {
                Log.e("BusinessHome", "Zaman formatı hatası: ${e.message}", e)
                return emptyList()
            }
        } catch (e: Exception) {
            Log.e("BusinessHome", "Zaman aralıkları oluşturulurken hata: ${e.message}", e)
            return emptyList()
        }
    }
} 