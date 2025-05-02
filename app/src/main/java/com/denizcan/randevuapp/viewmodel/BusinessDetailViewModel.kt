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
import kotlin.math.ceil
import androidx.compose.ui.res.stringResource
import com.denizcan.randevuapp.R
import java.util.*

class BusinessDetailViewModel : ViewModel() {
    private val firebaseService = FirebaseService()
    private val db = FirebaseFirestore.getInstance()
    
    private val _uiState = MutableStateFlow<BusinessDetailState>(BusinessDetailState.Loading)
    val uiState = _uiState.asStateFlow()

    private var selectedDateTime: LocalDateTime? = null
    private var appointmentNote: String = ""
    private var currentBusiness: User.Business? = null
    private var isCreatingAppointment = false

    sealed class BusinessDetailState {
        object Loading : BusinessDetailState()
        data class Success(
            val business: User.Business,
            val availableSlots: List<String>,
            val selectedDate: LocalDate = LocalDate.now(),
            val selectedTime: String? = null,
            val note: String = ""
        ) : BusinessDetailState()
        data class Error(val message: String) : BusinessDetailState()
    }

    fun loadBusinessDetails(businessId: String) {
        // Hatalı boş ID kontrol et
        if (businessId.isBlank()) {
            Log.e("BusinessDetail", "İşletme ID'si boş veya geçersiz")
            _uiState.value = BusinessDetailState.Error("Geçersiz işletme ID'si")
            return
        }

        Log.d("BusinessDetail", "İşletme detayları yükleniyor - ID: '$businessId'")

        viewModelScope.launch {
            try {
                _uiState.value = BusinessDetailState.Loading
                
                // İlk olarak "users" koleksiyonunda ara
                var businessDoc = db.collection("users").document(businessId).get().await()
                
                // Bulunamazsa "businesses" koleksiyonunda da dene
                if (!businessDoc.exists()) {
                    Log.d("BusinessDetail", "users koleksiyonunda bulunamadı, businesses koleksiyonunda aranıyor")
                    businessDoc = db.collection("businesses").document(businessId).get().await()
                }
                
                // Debug için belge verilerini göster
                if (businessDoc.exists()) {
                    Log.d("BusinessDetail", "İşletme belgesi bulundu: ${businessDoc.id}")
                    Log.d("BusinessDetail", "Belge verileri: ${businessDoc.data}")
                } else {
                    Log.e("BusinessDetail", "İşletme belgesi bulunamadı! ID: $businessId")
                    _uiState.value = BusinessDetailState.Error("İşletme bulunamadı (Belge mevcut değil)")
                    return@launch
                }
                
                // Veri alanlarını doğru şekilde oku
                val businessName = businessDoc.getString("businessName") ?: businessDoc.getString("name") ?: ""
                val address = businessDoc.getString("address") ?: ""
                val phone = businessDoc.getString("phone") ?: ""
                val sector = businessDoc.getString("sector") ?: ""
                
                if (businessName.isEmpty()) {
                    Log.w("BusinessDetail", "İşletme adı boş: ${businessDoc.id}")
                }
                
                // Çalışma günleri
                var workingDays = emptyList<String>()
                val workingDaysRaw = businessDoc.get("workingDays")
                if (workingDaysRaw != null) {
                    workingDays = when (workingDaysRaw) {
                        is List<*> -> workingDaysRaw.filterIsInstance<String>()
                        else -> emptyList()
                    }
                } else {
                    Log.w("BusinessDetail", "Çalışma günleri bulunamadı, varsayılan günler kullanılıyor")
                    workingDays = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")
                }
                
                // Çalışma saatleri
                var opening = "09:00"
                var closing = "17:00"
                var slotDuration = 30
                
                try {
                    val workingHoursMap = businessDoc.get("workingHours") as? Map<*, *>
                    if (workingHoursMap != null) {
                        opening = workingHoursMap["opening"] as? String ?: opening
                        closing = workingHoursMap["closing"] as? String ?: closing
                        slotDuration = (workingHoursMap["slotDuration"] as? Number)?.toInt() ?: slotDuration
                    } else {
                        Log.w("BusinessDetail", "Çalışma saatleri bulunamadı, varsayılan saatler kullanılıyor")
                    }
                } catch (e: Exception) {
                    Log.e("BusinessDetail", "Çalışma saatleri ayrıştırma hatası", e)
                }
                
                val workingHours = User.WorkingHours(opening, closing, slotDuration)
                
                // İşletme nesnesi oluştur
                val business = User.Business(
                    id = businessId,
                    businessName = businessName,
                    address = address,
                    phone = phone,
                    sector = sector,
                    workingDays = workingDays,
                    workingHours = workingHours
                )
                
                Log.d("BusinessDetail", "İşletme modeli oluşturuldu: $business")
                
                // Global değişkene kaydet
                currentBusiness = business
                
                // Başlangıç tarihi için uygun slotları hesapla
                val currentDate = LocalDate.now()
                val availableSlots = calculateAvailableSlots(business, currentDate)
                
                Log.d("BusinessDetail", "Hesaplanan slotlar: $availableSlots")
                
                // UI state'i güncelle
                _uiState.value = BusinessDetailState.Success(
                    business = business,
                    availableSlots = availableSlots,
                    selectedDate = currentDate
                )
                
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
                                businessName = business.businessName,
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
                    val availableSlots = calculateAvailableSlots(business, date)
                    
                    // Yeni state ile güncelle
                    val currentState = _uiState.value
                    if (currentState is BusinessDetailState.Success) {
                        _uiState.value = BusinessDetailState.Success(
                            business = currentState.business,
                            availableSlots = availableSlots,
                            selectedDate = date,
                            selectedTime = currentState.selectedTime,
                            note = currentState.note
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

    private suspend fun calculateAvailableSlots(
        business: User.Business,
        date: LocalDate
    ): List<String> {
        val workingHours = business.workingHours
        
        // İşletmenin çalışma günleri kontrolü
        val dayName = date.dayOfWeek.name
        if (!business.workingDays.contains(dayName)) {
            Log.d("BusinessDetail", "$dayName işletmenin çalışma günlerinde değil")
            return emptyList()
        }
        
        // Tüm randevuları al
        val appointments = firebaseService.getAppointmentsByDate(business.id, date)
        
        // Dolu saatleri bul
        val bookedSlots = appointments
            .filter { it.status == AppointmentStatus.PENDING || 
                     it.status == AppointmentStatus.CONFIRMED || 
                     it.status == AppointmentStatus.BLOCKED }
            .map { it.dateTime.format(DateTimeFormatter.ofPattern("HH:mm")) }
        
        Log.d("BusinessDetail", "Rezerve edilmiş ve kapatılmış slotlar: $bookedSlots")
        
        // Müsait saatleri hesapla (tüm çalışma saatleri - dolu saatler)
        val allSlots = generateTimeSlots(workingHours)
        val availableSlots = allSlots.filter { slot -> !bookedSlots.contains(slot) }
        
        // Bugün için geçmiş saatleri filtrele
        val today = LocalDate.now()
        val now = LocalTime.now()
        
        val filteredSlots = if (date.equals(today)) {
            availableSlots.filter { slot ->
                try {
                    val slotTime = LocalTime.parse(slot, DateTimeFormatter.ofPattern("HH:mm"))
                    slotTime.isAfter(now)
                } catch (e: Exception) {
                    Log.e("BusinessDetail", "Slot filtreleme hatası: $slot", e)
                    true // Hata durumunda slotu göster
                }
            }
        } else {
            availableSlots
        }
        
        return filteredSlots
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
                    val availableSlots = calculateAvailableSlots(currentBusiness!!, date)
                    
                    // UI state'i güncelle
                    _uiState.value = BusinessDetailState.Success(
                        business = currentState.business,
                        availableSlots = availableSlots,
                        selectedDate = date,
                        selectedTime = currentState.selectedTime,
                        note = currentState.note
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
                try {
                    // HH:mm formatındaki saati parse et
                    val selectedTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
                    
                    // Seçilen tarih ve saat birleştir
                    selectedDateTime = LocalDateTime.of(
                        currentState.selectedDate,
                        selectedTime
                    )
                    
                    // UI state'i güncelle
                    _uiState.value = currentState.copy(
                        selectedTime = time
                    )
                    
                    Log.d("BusinessDetail", "Seçilen tarih ve saat güncellendi: $selectedDateTime")
                    Log.d("BusinessDetail", "Formatlı saat: ${selectedTime.format(DateTimeFormatter.ofPattern("HH:mm"))}")
                } catch (e: Exception) {
                    Log.e("BusinessDetail", "Saat formatı hatası: $time", e)
                }
            }
        }
    }

    fun updateNote(note: String) {
        appointmentNote = note
    }

    suspend fun createAppointment(customerId: String): String {
        // Mevcut state'den business bilgisini al
        val currentState = _uiState.value
        if (currentState !is BusinessDetailState.Success) {
            throw IllegalStateException("İşletme bilgisi yüklenmedan randevu oluşturulamaz")
        }
        
        // currentBusiness veya state'den business bilgisini al
        val business = currentBusiness ?: currentState.business
        
        // Gerekli bilgileri al
        val selectedDate = currentState.selectedDate
        val selectedTime = currentState.selectedTime ?: throw IllegalStateException("Zaman seçilmedi")
        val note = currentState.note
        
        // Tarih ve saati birleştir
        val dateTime = LocalDateTime.of(
            selectedDate,
            LocalTime.parse(selectedTime, DateTimeFormatter.ofPattern("HH:mm"))
        )
        
        val appointment = Appointment(
            id = UUID.randomUUID().toString(),
            businessId = business.id,
            businessName = business.businessName,
            customerId = customerId,
            dateTime = dateTime,
            status = AppointmentStatus.PENDING,
            note = note
        )
        
        // Veritabanı işlemleri...
        viewModelScope.launch {
            firebaseService.saveAppointment(appointment)
        }
        
        return appointment.id
    }

    private fun generateTimeSlots(workingHours: User.WorkingHours): List<String> {
        try {
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
                // Örneğin: 00:00-23:59 = 1439 dakika, 1439/30 = 47.96 -> 48 slot olmalı
                val totalSlots = if (isSpecialEndTime) {
                    Math.ceil(adjustedMinutesBetween.toDouble() / slotDuration).toInt()
                } else {
                    (adjustedMinutesBetween / slotDuration).toInt()
                }
                
                Log.d("BusinessDetail", "Toplam randevu aralığı sayısı: $totalSlots (${startTime}-${endTime}, ${slotDuration}dk aralıklarla)")
                
                // *** LİMİT KALDIRILDI: Artık maksimum slot sayısı sınırı yok ***
                val slots = mutableListOf<String>()
                
                // Tüm slotları oluştur
                for (i in 0 until totalSlots) {
                    val slotTime = startTime.plus(i * slotDuration, org.threeten.bp.temporal.ChronoUnit.MINUTES)
                    
                    // Sadece bir güvenlik kontrolü: Hesaplanan zaman 23:59'u geçmemeli
                    if (slotTime.isAfter(LocalTime.of(23, 59))) {
                        Log.d("BusinessDetail", "Gün sınırını aşan zaman dilimi: $slotTime - döngü sonlandırılıyor")
                        break
                    }
                    
                    slots.add(slotTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                }
                
                // Kontrol: Eğer son slot 23:30 olarak bekleniyor ama eklenmemişse manuel ekle
                if (isSpecialEndTime && workingHours.slotDuration == 30 && 
                    (slots.isEmpty() || slots.last() != "23:30")) {
                    slots.add("23:30")
                    Log.d("BusinessDetail", "Son slot (23:30) manuel olarak eklendi")
                }
                
                Log.d("BusinessDetail", "Oluşturulan zaman aralıkları: ${slots.size} adet - başlangıç: ${slots.firstOrNull()}, bitiş: ${slots.lastOrNull()}")
                return slots
                
            } catch (e: Exception) {
                Log.e("BusinessDetail", "Zaman formatı hatası: ${e.message}", e)
                return emptyList()
            }
        } catch (e: Exception) {
            Log.e("BusinessDetail", "Zaman aralıkları oluşturulurken hata: ${e.message}", e)
            return emptyList()
        }
    }

    // İşletme için saat kapatma fonksiyonu
    fun blockTimeSlot(time: String) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState !is BusinessDetailState.Success) {
                    Log.e("BusinessDetail", "Saat kapatılamadı: Geçersiz durum")
                    return@launch
                }
                
                val business = currentState.business
                val selectedDate = currentState.selectedDate
                
                // Zaman formatını parse et
                val selectedTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
                
                // Tarih ve saat birleştir
                val dateTime = LocalDateTime.of(selectedDate, selectedTime)
                Log.d("BusinessDetail", "Kapatılacak saat: $dateTime")
                
                // FirebaseService'deki blockTimeSlot metodunu çağır
                firebaseService.blockTimeSlot(business.id, dateTime)
                
                // Başarılı ise, slotları yeniden yükle
                loadAvailableSlots(selectedDate)
                
                // UI'a bilgi mesajı göster (ViewModel'de bir event flow kullanabilirsiniz)
                Log.d("BusinessDetail", "Saat başarıyla kapatıldı: $time")
            } catch (e: Exception) {
                Log.e("BusinessDetail", "Saat kapatılırken hata", e)
            }
        }
    }
} 