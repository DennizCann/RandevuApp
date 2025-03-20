package com.denizcan.randevuapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denizcan.randevuapp.model.User
import com.denizcan.randevuapp.service.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserInfoViewModel : ViewModel() {
    private val firebaseService = FirebaseService()
    
    private val _userInfoState = MutableStateFlow<UserInfoState>(UserInfoState.Initial)
    val userInfoState = _userInfoState.asStateFlow()

    sealed class UserInfoState {
        object Initial : UserInfoState()
        object Loading : UserInfoState()
        object Success : UserInfoState()
        data class Error(val message: String) : UserInfoState()
    }

    fun saveCustomerInfo(userId: String, fullName: String, phone: String) {
        viewModelScope.launch {
            try {
                _userInfoState.value = UserInfoState.Loading
                val customer = User.Customer(
                    id = userId,
                    fullName = fullName,
                    phone = phone
                )
                firebaseService.saveUserData(customer)
                _userInfoState.value = UserInfoState.Success
            } catch (e: Exception) {
                _userInfoState.value = UserInfoState.Error(e.message ?: "Bilgiler kaydedilemedi")
            }
        }
    }

    fun saveBusinessInfo(userId: String, businessName: String, address: String, phone: String, sector: String) {
        viewModelScope.launch {
            try {
                _userInfoState.value = UserInfoState.Loading
                val business = User.Business(
                    id = userId,
                    businessName = businessName,
                    address = address,
                    phone = phone,
                    sector = sector
                )
                firebaseService.saveUserData(business)
                _userInfoState.value = UserInfoState.Success
            } catch (e: Exception) {
                _userInfoState.value = UserInfoState.Error(e.message ?: "Bilgiler kaydedilemedi")
            }
        }
    }
} 