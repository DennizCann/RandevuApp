package com.denizcan.randevuapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denizcan.randevuapp.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BusinessListViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    
    private val _businessListState = MutableStateFlow<BusinessListState>(BusinessListState.Loading)
    val businessListState: StateFlow<BusinessListState> = _businessListState.asStateFlow()
    
    private var lastAttemptTime = 0L
    
    init {
        loadBusinesses()
    }
    
    fun loadBusinesses() {
        // Aşırı çağrıları önle - 1 saniyede bir kez çalışsın
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAttemptTime < 1000) {
            Log.d("BusinessList", "Çok fazla yeniden yükleme denemesi engellendi")
            return
        }
        lastAttemptTime = currentTime

        viewModelScope.launch {
            try {
                _businessListState.value = BusinessListState.Loading
                
                Log.d("BusinessList", "Firestore'dan işletme verileri çekiliyor...")
                
                // ÖNEMLİ: Veritabanı yapısını kontrol et - Sorgu yöntemini düzeltelim
                // 1. Önce "businesses" koleksiyonunu deneyelim
                var businessesSnapshot = db.collection("businesses").get().await()
                var businessesList = mutableListOf<User.Business>()
                
                if (businessesSnapshot.isEmpty) {
                    Log.d("BusinessList", "businesses koleksiyonu boş veya mevcut değil, users koleksiyonuna bakılıyor")
                    
                    // 2. "users" koleksiyonunda type=business olanları filtreleyelim
                    val usersSnapshot = db.collection("users").whereEqualTo("type", "business").get().await()
                    
                    Log.d("BusinessList", "users/type=business sorgusu: ${usersSnapshot.documents.size} sonuç")
                    
                    if (usersSnapshot.isEmpty) {
                        // 3. Son çare: Filtre olmadan tüm users'ları çekelim ve manuel filtreleyelim
                        Log.d("BusinessList", "users/type=business sorgusu boş sonuç, tüm users çekiliyor")
                        val allUsersSnapshot = db.collection("users").get().await()
                        
                        Log.d("BusinessList", "Tüm kullanıcı sayısı: ${allUsersSnapshot.documents.size}")
                        
                        // Her belgenin yapısını logla
                        allUsersSnapshot.documents.forEach { doc ->
                            Log.d("BusinessList", "Belge ID: ${doc.id}, Data: ${doc.data}")
                        }
                        
                        // Dönüşüm deneyelim
                        businessesList = allUsersSnapshot.documents.mapNotNull { doc ->
                            parseBusinessDocument(doc)
                        }.toMutableList()
                    } else {
                        businessesList = usersSnapshot.documents.mapNotNull { doc ->
                            parseBusinessDocument(doc)
                        }.toMutableList()
                    }
                } else {
                    businessesList = businessesSnapshot.documents.mapNotNull { doc ->
                        parseBusinessDocument(doc)
                    }.toMutableList()
                }
                
                Log.d("BusinessList", "Bulunan toplam işletme sayısı: ${businessesList.size}")
                
                // İşletme listesi hala boşsa, örnek veri kullan
                if (businessesList.isEmpty()) {
                    Log.w("BusinessList", "Hiç işletme bulunamadı, sabit veri kullanılıyor")
                    businessesList.add(
                        User.Business(
                            id = "sample1",
                            businessName = "Örnek İşletme",
                            address = "İstanbul",
                            phone = "0123456789",
                            sector = "Örnek Sektör",
                            workingDays = listOf("MONDAY", "TUESDAY", "WEDNESDAY"),
                            workingHours = User.WorkingHours("09:00", "18:00", 30)
                        )
                    )
                }
                
                val sectors = businessesList.map { it.sector }.distinct()
                
                _businessListState.value = BusinessListState.Success(
                    businesses = businessesList,
                    sectors = sectors
                )
            } catch (e: Exception) {
                Log.e("BusinessList", "İşletme verileri yükleme hatası", e)
                _businessListState.value = BusinessListState.Error(
                    message = "İşletme verileri yüklenirken hata oluştu: ${e.message}"
                )
            }
        }
    }
    
    // Belge ayrıştırmayı ayrı bir fonksiyona alalım
    private fun parseBusinessDocument(doc: com.google.firebase.firestore.DocumentSnapshot): User.Business? {
        try {
            val id = doc.id
            val data = doc.data ?: return null
            
            // Belge yapısını inceleme
            Log.d("BusinessList", "İşletme belge analizi: $id - $data")
            
            // Eğer type alanı varsa ve business değilse atla
            val type = data["type"] as? String
            if (type != null && type != "business") {
                Log.d("BusinessList", "Bu belge bir işletme değil: $id, type: $type")
                return null
            }
            
            // Veri çekerken null kontrolü yapılıyor
            val businessName = data["businessName"] as? String ?: data["name"] as? String ?: ""
            val address = data["address"] as? String ?: ""
            val phone = data["phone"] as? String ?: ""
            val sector = data["sector"] as? String ?: ""
            
            // Eğer kritik alan boşsa loglayalım
            if (businessName.isEmpty()) {
                Log.w("BusinessList", "İşletme adı boş: $id, data: $data")
            }
            
            // Çalışma günleri
            // Farklı veri yapılarını destekleyelim (string list, array, etc)
            val workingDaysRaw = data["workingDays"]
            val workingDays = when (workingDaysRaw) {
                is List<*> -> workingDaysRaw.filterIsInstance<String>()
                is Array<*> -> workingDaysRaw.filterIsInstance<String>().toList()
                else -> listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")
            }
            
            // Çalışma saatleri
            var opening = "09:00"
            var closing = "18:00"
            var slotDuration = 30
            
            try {
                // workingHours alt nesnesini kontrol et - farklı yapıları destekle
                if (data.containsKey("workingHours")) {
                    val workingHoursObj = data["workingHours"]
                    
                    when (workingHoursObj) {
                        is Map<*, *> -> {
                            opening = workingHoursObj["opening"] as? String ?: opening
                            closing = workingHoursObj["closing"] as? String ?: closing
                            slotDuration = (workingHoursObj["slotDuration"] as? Number)?.toInt() ?: slotDuration
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("BusinessList", "Çalışma saatleri ayrıştırma hatası: ${e.message}")
            }
            
            val workingHours = User.WorkingHours(
                opening = opening,
                closing = closing, 
                slotDuration = slotDuration
            )
            
            val business = User.Business(
                id = id,
                businessName = businessName,
                address = address,
                phone = phone,
                sector = sector,
                workingDays = workingDays,
                workingHours = workingHours
            )
            
            Log.d("BusinessList", "İşletme başarıyla oluşturuldu: $businessName, ID: $id")
            return business
        } catch (e: Exception) {
            Log.e("BusinessList", "İşletme verisi ayrıştırma hatası: ${e.message}", e)
            return null
        }
    }
    
    sealed class BusinessListState {
        object Loading : BusinessListState()
        data class Success(
            val businesses: List<User.Business>,
            val sectors: List<String>
        ) : BusinessListState()
        data class Error(val message: String) : BusinessListState()
    }
} 