package com.denizcan.randevuapp.model

enum class AppointmentStatus {
    PENDING,    // Onay bekliyor
    CONFIRMED,  // Onaylandı
    CANCELLED,  // İptal edildi
    BLOCKED     // İşletme tarafından kapatılmış
} 