package com.denizcan.randevuapp.model

import org.threeten.bp.LocalDateTime

data class Appointment(
    val id: String = "",
    val businessId: String = "",
    val businessName: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val dateTime: LocalDateTime = LocalDateTime.now(),
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val note: String = ""
) 