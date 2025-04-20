package com.denizcan.randevuapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denizcan.randevuapp.model.User
import com.denizcan.randevuapp.service.FirebaseService
import com.google.firebase.auth.FirebaseAuth
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
                
                // Kullanıcının oturum açık olduğunu kontrol et
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    _userInfoState.value = UserInfoState.Error("Oturum süresi dolmuş. Lütfen tekrar giriş yapın.")
                    return@launch
                }
                
                // Kullanıcı ID'sinin doğru olduğunu kontrol et
                if (currentUser.uid != userId) {
                    _userInfoState.value = UserInfoState.Error("Yetkilendirme hatası. Lütfen tekrar giriş yapın.")
                    return@launch
                }
                
                val email = currentUser.email ?: ""
                
                val customerData = hashMapOf(
                    "id" to userId,
                    "email" to email,
                    "fullName" to fullName,
                    "phone" to phone,
                    "type" to "customer"
                )
                
                firebaseService.saveCustomerData(userId, customerData)
                _userInfoState.value = UserInfoState.Success
            } catch (e: Exception) {
                Log.e("UserInfoViewModel", "Müşteri bilgileri kaydedilemedi: ${e.message}", e)
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

    fun resetState() {
        _userInfoState.value = UserInfoState.Initial
    }
} 