package com.denizcan.randevuapp.model

data class WorkingHours(
    val startTime: String = "09:00",
    val endTime: String = "17:00",
    val timeSlotDuration: Int = 30 // dakika cinsinden
) 