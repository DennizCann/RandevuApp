package com.denizcan.randevuapp.model

enum class AppointmentStatus {
    PENDING,     // Müşteri tarafından oluşturuldu, işletme onayı bekliyor
    CONFIRMED,   // İşletme tarafından onaylandı
    CANCELLED,   // İptal edildi
    COMPLETED,   // Tamamlandı
    BLOCKED      // İşletme tarafından bloke edildi
} 