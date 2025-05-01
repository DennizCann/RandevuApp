package com.denizcan.randevuapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denizcan.randevuapp.model.User
import com.denizcan.randevuapp.model.Appointment
import com.denizcan.randevuapp.model.AppointmentStatus
import com.denizcan.randevuapp.service.FirebaseService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CustomerHomeViewModel : ViewModel() {
    private val firebaseService = FirebaseService()
    private val auth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow<CustomerHomeState>(CustomerHomeState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _appointmentsState = MutableStateFlow<AppointmentsState>(AppointmentsState.Loading)
    val appointmentsState = _appointmentsState.asStateFlow()

    sealed class CustomerHomeState {
        object Loading : CustomerHomeState()
        data class Success(
            val customer: User.Customer
        ) : CustomerHomeState()
        data class Error(val message: String) : CustomerHomeState()
    }

    sealed class AppointmentsState {
        object Loading : AppointmentsState()
        data class Success(
            val appointments: List<Appointment>
        ) : AppointmentsState()
        data class Error(val message: String) : AppointmentsState()
    }

    fun loadCustomerData(customerId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = CustomerHomeState.Loading
                val customer = firebaseService.getUserData(customerId, "customer") as? User.Customer
                if (customer != null) {
                    _uiState.value = CustomerHomeState.Success(customer)
                } else {
                    _uiState.value = CustomerHomeState.Error("Müşteri bilgileri yüklenemedi")
                }
            } catch (e: Exception) {
                _uiState.value = CustomerHomeState.Error(e.message ?: "Bir hata oluştu")
            }
        }
    }

    fun loadCustomerAppointments(customerId: String) {
        viewModelScope.launch {
            try {
                _appointmentsState.value = AppointmentsState.Loading
                val appointments = firebaseService.getCustomerAppointments(customerId)
                _appointmentsState.value = AppointmentsState.Success(appointments)
            } catch (e: Exception) {
                _appointmentsState.value = AppointmentsState.Error(e.message ?: "Randevular yüklenemedi")
            }
        }
    }

    fun cancelAppointment(appointmentId: String) {
        viewModelScope.launch {
            try {
                _appointmentsState.value = AppointmentsState.Loading
                
                // Randevu durumunu değiştirmek yerine tamamen sil
                firebaseService.deleteAppointment(appointmentId)
                
                // Güncel randevu listesini yeniden yükle
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    loadCustomerAppointments(currentUser.uid)
                }
            } catch (e: Exception) {
                _appointmentsState.value = AppointmentsState.Error("Randevu iptal edilirken hata oluştu: ${e.message}")
            }
        }
    }
} 