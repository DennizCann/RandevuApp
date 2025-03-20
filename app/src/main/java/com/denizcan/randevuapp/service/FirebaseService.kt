package com.denizcan.randevuapp.service

import com.denizcan.randevuapp.model.User
import com.denizcan.randevuapp.model.Appointment
import com.denizcan.randevuapp.model.AppointmentStatus
import com.denizcan.randevuapp.model.WorkingHours
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.threeten.bp.LocalDate
import com.google.firebase.Timestamp
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.Instant

class FirebaseService {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun signIn(email: String, password: String) = 
        auth.signInWithEmailAndPassword(email, password).await()

    suspend fun signUp(email: String, password: String) =
        auth.createUserWithEmailAndPassword(email, password).await()

    suspend fun saveUserData(user: User) {
        when (user) {
            is User.Customer -> {
                firestore.collection("customers").document(user.id).set(user).await()
            }
            is User.Business -> {
                firestore.collection("businesses").document(user.id).set(user).await()
            }
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
            val collection = if (userType == "customer") "customers" else "businesses"
            val document = firestore.collection(collection).document(userId).get().await()
            
            when (userType) {
                "customer" -> document.toObject(User.Customer::class.java)
                "business" -> document.toObject(User.Business::class.java)
                else -> null
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
            firestore.collection("businesses")
                .document(businessId)
                .update(
                    mapOf(
                        "workingDays" to workingDays,
                        "workingHours" to workingHours,
                    )
                )
                .await()
        } catch (e: Exception) {
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
            val startOfDay = date.atStartOfDay()
            val endOfDay = date.plusDays(1).atStartOfDay()

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
                                status = AppointmentStatus.valueOf(data["status"] as? String ?: AppointmentStatus.PENDING.name)
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
                            status = AppointmentStatus.valueOf(data["status"] as? String ?: AppointmentStatus.PENDING.name)
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
        dateTime: LocalDateTime
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
                status = AppointmentStatus.PENDING
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
                            status = AppointmentStatus.valueOf(data["status"] as? String ?: AppointmentStatus.PENDING.name)
                        )
                    }
                }
                .sortedBy { it.dateTime }
        } catch (e: Exception) {
            emptyList()
        }
    }
} 