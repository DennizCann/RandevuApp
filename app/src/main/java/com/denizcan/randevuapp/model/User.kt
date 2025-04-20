package com.denizcan.randevuapp.model

sealed class User {
    data class Customer(
        val id: String = "",
        val email: String = "",
        val fullName: String = "",
        val phone: String = "",
        val type: String = "customer"
    ) : User()

    data class Business(
        val id: String = "",
        val email: String = "",
        val businessName: String = "",
        val address: String = "",
        val phone: String = "",
        val sector: String = "",
        val type: String = "business",
        val workingDays: List<String> = emptyList(),
        val workingHours: WorkingHours = WorkingHours()
    ) : User()

    data class WorkingHours(
        val opening: String = "09:00",
        val closing: String = "18:00",
        val slotDuration: Int = 60 // dakika cinsinden
    )
} 