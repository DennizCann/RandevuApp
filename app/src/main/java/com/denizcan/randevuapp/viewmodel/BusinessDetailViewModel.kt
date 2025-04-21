package com.denizcan.randevuapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denizcan.randevuapp.model.Appointment
import com.denizcan.randevuapp.model.User
import com.denizcan.randevuapp.service.FirebaseService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import com.denizcan.randevuapp.model.AppointmentStatus

class BusinessDetailViewModel : ViewModel() {
    private val firebaseService = FirebaseService()
    private val db = FirebaseFirestore.getInstance()
    
    private val _uiState = MutableStateFlow<BusinessDetailState>(BusinessDetailState.Loading)
    val uiState = _uiState.asStateFlow()

    private var selectedDateTime: LocalDateTime? = null
    private var appointmentNote: String = ""
    private var currentBusiness: User.Business? = null

    sealed class BusinessDetailState {
        object Loading : BusinessDetailState()
        data class Success(
            val business: User.Business,
            val availableSlots: List<String>,
            val selectedDate: LocalDate = LocalDate.now()
        ) : BusinessDetailState()
        data class Error(val message: String) : BusinessDetailState()
    }

    fun loadBusinessDetails(businessId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = BusinessDetailState.Loading
                
                Log.d("BusinessDetail", "İşletme detayları yükleniyor, ID: $businessId")
                
                // Kolay debug için önce tüm kullanıcıları al
                val allUsers = db.collection("users").get().await()
                Log.d("BusinessDetail", "Tüm kullanıcı sayısı: ${allUsers.size()}")
                
                // İşletme ID'si var mı kontrol et
                val foundUser = allUsers.documents.find { it.id == businessId }
                if (foundUser != null) {
                    Log.d("BusinessDetail", "İşletme veritabanında bulundu: $businessId")
                    Log.d("BusinessDetail", "İşletme verisi: ${foundUser.data}")
                } else {
                    Log.w("BusinessDetail", "Bu ID ile kullanıcı bulunamadı: $businessId")
                    
                    // Tüm belge ID'lerini logla
                    Log.d("BusinessDetail", "Mevcut belge ID'leri:")
                    allUsers.documents.forEach { 
                        Log.d("BusinessDetail", "Belge ID: ${it.id}") 
                    }
                }
                
                // İşletmeyi global değişkene atama eklendi
                val business = User.Business(
                    id = businessId,
                    businessName = "Berber Osman",
                    address = "İstanbul",
                    phone = "0123456789",
                    sector = "Berber",
                    workingDays = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "SATURDAY", "SUNDAY"),
                    workingHours = User.WorkingHours("07:00", "16:00", 15)
                )
                
                // Global değişkene kaydet - bu çok önemli!
                currentBusiness = business
                
                // Gerçek slot hesaplaması için
                val currentDate = LocalDate.now()
                val availableSlots = calculateAvailableSlots(business, currentDate, emptyList())
                
                // Başarılı durumu güncelle
                _uiState.value = BusinessDetailState.Success(
                    business = business,
                    availableSlots = availableSlots,
                    selectedDate = currentDate
                )
                
                Log.d("BusinessDetail", "Test verileri ile sayfa yüklendi")
                
            } catch (e: Exception) {
                Log.e("BusinessDetail", "İşletme detayları yüklenirken hata", e)
                _uiState.value = BusinessDetailState.Error("İşletme detayları yüklenirken hata: ${e.message}")
            }
        }
    }

    fun loadAvailableSlots(date: LocalDate) {
        viewModelScope.launch {
            try {
                // Mevcut işletme bilgisini al
                val business = currentBusiness ?: run {
                    Log.e("BusinessDetail", "Slot hesaplama için işletme bilgisi bulunamadı")
                    return@launch
                }
                
                Log.d("BusinessDetail", "Müsait slotlar yükleniyor, Tarih: $date")
                
                try {
                    // O güne ait randevuları çek
                    val startOfDay = date.atStartOfDay()
                    val endOfDay = date.plusDays(1).atStartOfDay().minusSeconds(1)
                    
                    val startTimestamp = startOfDay.toString()
                    val endTimestamp = endOfDay.toString()
                    
                    Log.d("BusinessDetail", "Randevu sorgusu: $startTimestamp - $endTimestamp")
                    
                    val appointmentsSnapshot = db.collection("appointments")
                        .whereEqualTo("businessId", business.id)
                        .whereGreaterThanOrEqualTo("dateTime", startTimestamp)
                        .whereLessThanOrEqualTo("dateTime", endTimestamp)
                        .get()
                        .await()
                    
                    val appointments = appointmentsSnapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: return@mapNotNull null
                            val dateTimeString = data["dateTime"] as? String ?: return@mapNotNull null
                            val statusString = data["status"] as? String ?: "pending"
                            
                            val appointmentStatus = when(statusString) {
                                "pending" -> AppointmentStatus.PENDING
                                "confirmed" -> AppointmentStatus.CONFIRMED
                                "cancelled" -> AppointmentStatus.CANCELLED
                                "completed" -> AppointmentStatus.CONFIRMED
                                else -> AppointmentStatus.PENDING
                            }
                            
                            if (appointmentStatus == AppointmentStatus.CANCELLED) return@mapNotNull null
                            
                            val dateTime = try {
                                LocalDateTime.parse(dateTimeString)
                            } catch (e: Exception) {
                                Log.e("BusinessDetail", "Randevu tarih formatı hatası: $dateTimeString", e)
                                return@mapNotNull null
                            }
                            
                            Appointment(
                                id = doc.id,
                                businessId = business.id,
                                customerId = data["customerId"] as? String ?: "",
                                dateTime = dateTime,
                                status = appointmentStatus,
                                note = data["note"] as? String ?: ""
                            )
                        } catch (e: Exception) {
                            Log.e("BusinessDetail", "Randevu okuma hatası", e)
                            null
                        }
                    }
                    
                    Log.d("BusinessDetail", "Çekilen randevu sayısı: ${appointments.size}")
                    
                    // Müsait slotları hesapla
                    val availableSlots = calculateAvailableSlots(business, date, appointments)
                    
                    // Yeni state ile güncelle
                    val currentState = _uiState.value
                    if (currentState is BusinessDetailState.Success) {
                        _uiState.value = BusinessDetailState.Success(
                            business = currentState.business,
                            availableSlots = availableSlots,
                            selectedDate = date
                        )
                        Log.d("BusinessDetail", "Müsait slotlar başarıyla güncellendi: ${availableSlots.size} slot")
                    } else {
                        Log.w("BusinessDetail", "Durum Success değil, güncellenemiyor")
                    }
                } catch (e: Exception) {
                    Log.e("BusinessDetail", "Müsait slotlar yüklenirken hata", e)
                    // Hata durumunda mevcut state'i koru ama hatayı loglayalım
                }
            } catch (e: Exception) {
                Log.e("BusinessDetail", "Müsait slotlar yüklenirken beklenmeyen hata", e)
            }
        }
    }

    private fun calculateAvailableSlots(
        business: User.Business,
        date: LocalDate,
        appointments: List<Appointment>
    ): List<String> {
        val dayName = date.dayOfWeek.name
        Log.d("BusinessDetail", "Slot hesaplama: Gün: $dayName, İşletme günleri: ${business.workingDays}")
        
        // Eğer seçilen gün işletmenin çalışma günlerinden biri değilse boş liste döndür
        if (!business.workingDays.contains(dayName)) {
            Log.d("BusinessDetail", "$dayName işletmenin çalışma günlerinde değil")
            return emptyList()
        }

        // Çalışma saatleri - veri tabanı formatına uygun
        val workingHours = business.workingHours
        val startTime = try {
            LocalTime.parse(workingHours.opening)
        } catch (e: Exception) {
            Log.e("BusinessDetail", "Açılış saati parse hatası: ${workingHours.opening}", e)
            LocalTime.of(9, 0) // Varsayılan değer
        }
        
        val endTime = try {
            LocalTime.parse(workingHours.closing)
        } catch (e: Exception) {
            Log.e("BusinessDetail", "Kapanış saati parse hatası: ${workingHours.closing}", e)
            LocalTime.of(18, 0) // Varsayılan değer
        }
        
        // Veritabanındaki slotDuration değerini kullan (15 dakika)
        val slotDuration = workingHours.slotDuration.takeIf { it > 0 } ?: 30 // Sıfır veya negatifse 30 dakika kullan

        // Slots oluştur
        val allSlots = mutableListOf<String>()
        var currentTime = startTime
        while (currentTime.plusMinutes(slotDuration.toLong()) <= endTime) {
            allSlots.add(currentTime.format(DateTimeFormatter.ofPattern("HH:mm")))
            currentTime = currentTime.plusMinutes(slotDuration.toLong())
        }
        
        Log.d("BusinessDetail", "Oluşturulan tüm slotlar: $allSlots")

        // Dolu slotları filtrele
        val bookedSlots = appointments
            .filter { it.dateTime.toLocalDate() == date }
            .map { it.dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")) }
        
        // Müsait slotları hesapla ve döndür
        val availableSlots = allSlots.filter { slot -> !bookedSlots.contains(slot) }
        return availableSlots
    }

    fun updateSelectedDate(date: LocalDate) {
        viewModelScope.launch {
            try {
                Log.d("BusinessDetail", "Yeni tarih seçildi: $date")
                
                // Eğer hala işletme verisi yüklenmediyse uyarı
                if (currentBusiness == null) {
                    Log.e("BusinessDetail", "İşletme bilgisi mevcut değil, slotlar hesaplanamıyor")
                    return@launch
                }
                
                // Mevcut state'i al ve tarih değişikliğini uygula
                val currentState = _uiState.value
                if (currentState is BusinessDetailState.Success) {
                    // Seçilen tarih için boş slotlar hesapla
                    val availableSlots = calculateAvailableSlots(currentBusiness!!, date, emptyList())
                    
                    // UI state'i güncelle
                    _uiState.value = BusinessDetailState.Success(
                        business = currentState.business,
                        availableSlots = availableSlots,
                        selectedDate = date
                    )
                    
                    Log.d("BusinessDetail", "Tarih güncellendi: $date, Müsait slotlar: $availableSlots")
                    
                    // Seçilen tarih için randevu verilerini yükle
                    loadAvailableSlots(date)
                } else {
                    Log.w("BusinessDetail", "State Success değil, tarih güncellenemedi")
                }
                
                // Yeni tarih seçildiğinde önceki saat seçimini sıfırla
                selectedDateTime = null
                
            } catch (e: Exception) {
                Log.e("BusinessDetail", "Tarih güncellenirken hata", e)
            }
        }
    }

    fun updateSelectedTime(time: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is BusinessDetailState.Success) {
                val timeParts = time.split(":")
                val hour = timeParts[0].toInt()
                val minute = timeParts[1].toInt()
                
                selectedDateTime = LocalDateTime.of(
                    currentState.selectedDate,
                    LocalTime.of(hour, minute)
                )
                
                Log.d("BusinessDetail", "Seçilen tarih ve saat: $selectedDateTime")
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
                if (currentState !is BusinessDetailState.Success || selectedDateTime == null) {
                    return@launch
                }
                
                val business = currentState.business
                
                val appointment = mapOf(
                    "businessId" to business.id,
                    "customerId" to customerId,
                    "dateTime" to selectedDateTime.toString(),
                    "status" to AppointmentStatus.PENDING.name,
                    "note" to appointmentNote,
                    "createdAt" to LocalDateTime.now().toString()
                )
                
                // Randevuyu Firestore'a kaydet
                db.collection("appointments").add(appointment).await()
                
                // State'i sıfırla
                selectedDateTime = null
                appointmentNote = ""
                
                Log.d("BusinessDetail", "Randevu başarıyla oluşturuldu")
            } catch (e: Exception) {
                Log.e("BusinessDetail", "Randevu oluşturulurken hata", e)
            }
        }
    }

    private fun generateAvailableTimes(date: LocalDate, opening: String, closing: String, slotDuration: Int): List<LocalDateTime> {
        try {
            val startTime = LocalTime.parse(opening)
            val endTime = LocalTime.parse(closing)
            
            val availableTimes = mutableListOf<LocalDateTime>()
            var currentTime = startTime
            
            while (currentTime.plusMinutes(slotDuration.toLong()) <= endTime) {
                val dateTime = date.atTime(currentTime)
                availableTimes.add(dateTime)
                currentTime = currentTime.plusMinutes(slotDuration.toLong())
            }
            
            return availableTimes
        } catch (e: Exception) {
            println("Müsait zamanlar hesaplanırken hata: ${e.message}")
            return emptyList()
        }
    }
} 