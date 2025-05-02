package com.denizcan.randevuapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denizcan.randevuapp.model.User
import com.denizcan.randevuapp.service.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel : ViewModel() {
    private val firebaseService = FirebaseService()
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState = _authState.asStateFlow()

    sealed class AuthState {
        object Initial : AuthState()
        object Loading : AuthState()
        data class Success(
            val userType: String, 
            val userId: String,
            val isNewUser: Boolean
        ) : AuthState()
        data class Error(val message: String) : AuthState()
    }

    fun signIn(email: String, password: String, isBusinessLogin: Boolean) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = firebaseService.signIn(email, password)
                val userType = if (isBusinessLogin) "business" else "customer"
                
                result.user?.let { user ->
                    val userData = firebaseService.getUserData(user.uid, userType)
                    _authState.value = AuthState.Success(
                        userType = userType,
                        userId = user.uid,
                        isNewUser = userData == null
                    )
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Giriş başarısız")
            }
        }
    }

    fun signUp(email: String, password: String, isBusinessLogin: Boolean) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = firebaseService.signUp(email, password)
                val userType = if (isBusinessLogin) "business" else "customer"
                
                result.user?.let { user ->
                    _authState.value = AuthState.Success(
                        userType = userType,
                        userId = user.uid,
                        isNewUser = true
                    )
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Kayıt başarısız")
            }
        }
    }

    fun logout(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                
                // Firebase'den çıkış yap - FirebaseService'de signOut metodu olmadığı için
                // doğrudan FirebaseAuth.getInstance().signOut() kullan
                FirebaseAuth.getInstance().signOut()
                
                // Başarılı çıkış durumunu ayarla
                _authState.value = AuthState.Initial
                
                // İşlem tamamlandığında callback'i çağır
                onComplete()
                
            } catch (e: Exception) {
                // Hata durumunda bile Initial durumuna geri dön
                _authState.value = AuthState.Initial
                
                // Callback'i çağır
                onComplete()
                
                // Hata günlüğüne yaz
                Log.e("AuthViewModel", "Logout failed", e)
            }
        }
    }
} 