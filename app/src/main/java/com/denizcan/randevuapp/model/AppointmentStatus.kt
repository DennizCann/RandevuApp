package com.denizcan.randevuapp.model

enum class AppointmentStatus {
    PENDING,    // Onay bekliyor
    CONFIRMED,  // Onaylandı
    CANCELLED,  // İptal edildi
    COMPLETED,  // Tamamlandı
    BLOCKED     // İşletme tarafından bloke edilmiş
} 