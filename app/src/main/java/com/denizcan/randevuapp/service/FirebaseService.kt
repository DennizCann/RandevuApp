package com.denizcan.randevuapp.service

import com.denizcan.randevuapp.model.User
import com.denizcan.randevuapp.model.Appointment
import com.denizcan.randevuapp.model.AppointmentStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.threeten.bp.LocalDate
import com.google.firebase.Timestamp
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.Instant
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class FirebaseService {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun signIn(email: String, password: String) = 
        auth.signInWithEmailAndPassword(email, password).await()

    suspend fun signUp(email: String, password: String) =
        auth.createUserWithEmailAndPassword(email, password).await()

    suspend fun saveUserData(user: User) {
        try {
            Log.d("FirebaseService", "Kullanıcı verisi kaydediliyor: $user")
            
            when (user) {
                is User.Customer -> {
                    // Map tüm veriyi açık bir şekilde kontrol edelim
                    val userData = mapOf(
                        "id" to user.id,
                        "email" to user.email,
                        "fullName" to user.fullName,
                        "phone" to user.phone,
                        "type" to user.type
                    )
                    
                    Log.d("FirebaseService", "Müşteri verisi: $userData")
                    firestore.collection("customers").document(user.id).set(userData).await()
                    Log.d("FirebaseService", "Müşteri verisi başarıyla kaydedildi")
                }
                is User.Business -> {
                    // Benzer şekilde
                    val userData = mapOf(
                        "id" to user.id,
                        "email" to user.email,
                        "businessName" to user.businessName,
                        "address" to user.address,
                        "phone" to user.phone,
                        "sector" to user.sector,
                        "type" to user.type,
                        "workingDays" to user.workingDays,
                        "workingHours" to user.workingHours
                    )
                    
                    Log.d("FirebaseService", "İşletme verisi: $userData")
                    firestore.collection("businesses").document(user.id).set(userData).await()
                    Log.d("FirebaseService", "İşletme verisi başarıyla kaydedildi")
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseService", "Veri kaydedilemedi: ${e.message}")
            throw e
        }
    }

    suspend fun getBusinesses(): List<User.Business> {
        return firestore.collection("businesses")
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(User.Business::class.java)
            }
    }

    suspend fun getUserData(userId: String, userType: String): User? {
        return try {
            val collectionName = if (userType == "business") "businesses" else "customers"
            val docRef = firestore.collection(collectionName).document(userId).get().await()
            
            if (docRef.exists()) {
                if (userType == "business") {
                    docRef.toObject(User.Business::class.java)
                } else {
                    docRef.toObject(User.Customer::class.java)
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getBusinessById(businessId: String): User.Business? {
        return try {
            firestore.collection("businesses")
                .document(businessId)
                .get()
                .await()
                .toObject(User.Business::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getPendingAppointmentsCount(businessId: String): Int {
        return try {
            firestore.collection("appointments")
                .whereEqualTo("businessId", businessId)
                .whereEqualTo("status", AppointmentStatus.PENDING)
                .get()
                .await()
                .size()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun updateWorkingHours(
        businessId: String, 
        workingDays: List<String>,
        workingHours: User.WorkingHours
    ) {
        try {
            // Hata ayıklama için log ekleyelim
            Log.d("FirebaseService", "İşletme ID: $businessId")
            Log.d("FirebaseService", "Çalışma günleri: $workingDays")
            Log.d("FirebaseService", "Çalışma saatleri: $workingHours")
            
            val businessRef = FirebaseFirestore.getInstance()
                .collection("businesses")
                .document(businessId)
            
            // Farklı bir yaklaşım deneyelim - daha basit bir güncelleme
            val updates = hashMapOf<String, Any>(
                "workingDays" to workingDays,
                "workingHours.opening" to workingHours.opening,
                "workingHours.closing" to workingHours.closing,
                "workingHours.slotDuration" to workingHours.slotDuration
            )
            
            businessRef.update(updates).await()
            
            Log.d("FirebaseService", "Çalışma saatleri başarıyla güncellendi")
        } catch (e: Exception) {
            Log.e("FirebaseService", "Çalışma saatleri güncellenemedi: ${e.message}", e)
            throw e
        }
    }

    private fun Timestamp.toLocalDateTime(): LocalDateTime {
        return try {
            LocalDateTime.ofInstant(
                Instant.ofEpochSecond(seconds, nanoseconds.toLong()),
                ZoneId.systemDefault()
            ).also {
                println("Debug: Converting Timestamp $this (${this.seconds}) to LocalDateTime: $it")
            }
        } catch (e: Exception) {
            println("Error converting to LocalDateTime: $e")
            throw e
        }
    }

    private fun LocalDateTime.toTimestamp(): Timestamp {
        return try {
            val instant = this.atZone(ZoneId.systemDefault()).toInstant()
            Timestamp(instant.epochSecond, instant.nano).also {
                println("Debug: Converting LocalDateTime $this to Timestamp: $it (${it.seconds})")
            }
        } catch (e: Exception) {
            println("Error converting to Timestamp: $e")
            throw e
        }
    }

    suspend fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus) {
        try {
            firestore.collection("appointments")
                .document(appointmentId)
                .update("status", status.name)
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getAppointmentsByDate(businessId: String, date: LocalDate): List<Appointment> {
        return try {
            firestore.collection("appointments")
                .whereEqualTo("businessId", businessId)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.data?.let { data ->
                        val timestamp = data["dateTime"] as? Timestamp
                        val dateTime = timestamp?.toLocalDateTime()
                        
                        if (dateTime != null && dateTime.toLocalDate() == date) {
                            Appointment(
                                id = doc.id,
                                businessId = data["businessId"] as? String ?: "",
                                businessName = data["businessName"] as? String ?: "Bilinmeyen İşletme",
                                customerId = data["customerId"] as? String ?: "",
                                customerName = data["customerName"] as? String ?: "İsimsiz Müşteri",
                                dateTime = dateTime,
                                status = AppointmentStatus.valueOf(data["status"] as? String ?: AppointmentStatus.PENDING.name),
                                note = data["note"] as? String ?: ""
                            )
                        } else null
                    }
                }
                .sortedBy { it.dateTime }
        } catch (e: Exception) {
            println("Error fetching appointments: ${e.message}")
            emptyList()
        }
    }

    suspend fun getPendingAppointments(businessId: String): List<Appointment> {
        return try {
            firestore.collection("appointments")
                .whereEqualTo("businessId", businessId)
                .whereEqualTo("status", AppointmentStatus.PENDING)
                .get()
                .await()
                .toObjects(Appointment::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCustomerAppointments(customerId: String): List<Appointment> {
        return try {
            firestore.collection("appointments")
                .whereEqualTo("customerId", customerId)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.data?.let { data ->
                        Appointment(
                            id = doc.id,
                            businessId = data["businessId"] as? String ?: "",
                            businessName = data["businessName"] as? String ?: "Bilinmeyen İşletme",
                            customerId = data["customerId"] as? String ?: "",
                            customerName = data["customerName"] as? String ?: "İsimsiz Müşteri",
                            dateTime = (data["dateTime"] as? Timestamp)?.toLocalDateTime() ?: LocalDateTime.now(),
                            status = AppointmentStatus.valueOf(data["status"] as? String ?: AppointmentStatus.PENDING.name),
                            note = data["note"] as? String ?: ""
                        )
                    }
                }
                .sortedByDescending { it.dateTime }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createAppointment(
        businessId: String,
        customerId: String,
        dateTime: LocalDateTime,
        note: String = ""
    ): String {
        return try {
            // İşletme ve müşteri bilgilerini al
            val business = getBusinessById(businessId)
            val customer = getUserData(customerId, "customer") as? User.Customer

            val appointment = Appointment(
                businessId = businessId,
                businessName = business?.businessName ?: "Bilinmeyen İşletme",
                customerId = customerId,
                customerName = customer?.fullName ?: "İsimsiz Müşteri",
                dateTime = dateTime,
                status = AppointmentStatus.PENDING,
                note = note
            )
            
            firestore.collection("appointments")
                .add(mapOf(
                    "businessId" to appointment.businessId,
                    "businessName" to appointment.businessName,
                    "customerId" to appointment.customerId,
                    "customerName" to appointment.customerName,
                    "dateTime" to appointment.dateTime.toTimestamp(),
                    "status" to appointment.status.name,
                    "note" to appointment.note
                ))
                .await()
                .id
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getAppointmentRequests(businessId: String): List<Appointment> {
        return try {
            firestore.collection("appointments")
                .whereEqualTo("businessId", businessId)
                .whereEqualTo("status", AppointmentStatus.PENDING.name)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.data?.let { data ->
                        Appointment(
                            id = doc.id,
                            businessId = data["businessId"] as? String ?: "",
                            businessName = data["businessName"] as? String ?: "Bilinmeyen İşletme",
                            customerId = data["customerId"] as? String ?: "",
                            customerName = data["customerName"] as? String ?: "İsimsiz Müşteri",
                            dateTime = (data["dateTime"] as? Timestamp)?.toLocalDateTime() ?: LocalDateTime.now(),
                            status = AppointmentStatus.valueOf(data["status"] as? String ?: AppointmentStatus.PENDING.name),
                            note = data["note"] as? String ?: ""
                        )
                    }
                }
                .sortedBy { it.dateTime }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createBlockedAppointment(
        businessId: String,
        businessName: String, 
        dateTime: LocalDateTime
    ): String {
        return try {
            val appointment = Appointment(
                businessId = businessId,
                businessName = businessName,
                customerId = "",  // İşletme tarafından kapatıldığı için müşteri yok
                customerName = "", 
                dateTime = dateTime,
                status = AppointmentStatus.BLOCKED
            )
            
            firestore.collection("appointments")
                .add(mapOf(
                    "businessId" to appointment.businessId,
                    "businessName" to appointment.businessName,
                    "customerId" to appointment.customerId,
                    "customerName" to appointment.customerName,
                    "dateTime" to appointment.dateTime.toTimestamp(),
                    "status" to appointment.status.name
                ))
                .await()
                .id
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deleteAppointment(appointmentId: String) {
        try {
            firestore.collection("appointments")
                .document(appointmentId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getAppointmentById(appointmentId: String): Appointment? {
        return try {
            val docSnapshot = firestore.collection("appointments")
                .document(appointmentId)
                .get()
                .await()
            
            if (docSnapshot.exists()) {
                val data = docSnapshot.data
                if (data != null) {
                    Appointment(
                        id = appointmentId,
                        businessId = data["businessId"] as? String ?: "",
                        businessName = data["businessName"] as? String ?: "",
                        customerId = data["customerId"] as? String ?: "",
                        customerName = data["customerName"] as? String ?: "",
                        dateTime = (data["dateTime"] as? Timestamp)?.toLocalDateTime() ?: LocalDateTime.now(),
                        status = AppointmentStatus.valueOf(data["status"] as? String ?: AppointmentStatus.PENDING.name),
                        note = data["note"] as? String ?: ""
                    )
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveCustomerData(userId: String, customerData: Map<String, Any>) {
        try {
            Log.d("FirebaseService", "Müşteri verisi kaydediliyor: $customerData")
            
            // Kullanıcı oturumunu kontrol et
            val currentUser = auth.currentUser
            if (currentUser == null) {
                throw Exception("Oturum süresi dolmuş (currentUser null)")
            }
            
            // Kullanıcı ID'sini kontrol et
            if (currentUser.uid != userId) {
                throw Exception("Yetkilendirme hatası: Mevcut kullanıcı ID (${currentUser.uid}) ile istenen ID ($userId) eşleşmiyor")
            }
            
            // Firebase token ID'yi kontrol et (opsiyonel)
            val tokenResult = currentUser.getIdToken(false).await()
            Log.d("FirebaseService", "Token: ${tokenResult.token?.take(15)}...")
            
            firestore.collection("customers")
                .document(userId)
                .set(customerData)
                .await()
            
            Log.d("FirebaseService", "Müşteri verisi başarıyla kaydedildi")
        } catch (e: Exception) {
            Log.e("FirebaseService", "Müşteri verisi kaydedilemedi: ${e.message}", e)
            throw e
        }
    }

    suspend fun getAppointmentsForBusiness(businessId: String): List<Appointment> {
        try {
            val appointmentsRef = Firebase.firestore.collection("appointments")
                .whereEqualTo("businessId", businessId)
            
            val querySnapshot = appointmentsRef.get().await()
            return querySnapshot.documents.mapNotNull { document ->
                document.toObject(Appointment::class.java)?.apply {
                    id = document.id
                }
            }
        } catch (e: Exception) {
            println("İşletme randevuları alınırken hata: ${e.message}")
            return emptyList()
        }
    }
} 