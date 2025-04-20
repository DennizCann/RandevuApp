package com.denizcan.randevuapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denizcan.randevuapp.service.FirebaseService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val firebaseService = FirebaseService()
    private val auth = FirebaseAuth.getInstance()
    
    private val _homeState = MutableStateFlow<HomeState>(HomeState.Initial)
    val homeState = _homeState.asStateFlow()

    sealed class HomeState {
        object Initial : HomeState()
        object Loading : HomeState()
        object Success : HomeState()
        data class Error(val message: String) : HomeState()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun signOut() {
        try {
            // Önce mevcut oturumu kontrol et
            val currentUser = auth.currentUser
            Log.d("HomeViewModel", "Çıkış yapan kullanıcı: ${currentUser?.email}")
            
            // Firestore/SharedPreferences temizliği (varsa)
            // ...
            
            // Firebase çıkış işlemi
            auth.signOut()
            
            // Çıkış sonrası doğrulama
            val afterSignOut = auth.currentUser
            if (afterSignOut == null) {
                Log.d("HomeViewModel", "Kullanıcı başarıyla çıkış yaptı")
                _homeState.value = HomeState.Success
            } else {
                Log.e("HomeViewModel", "Çıkış yapılamadı, kullanıcı hala oturumda")
                _homeState.value = HomeState.Error("Çıkış yapılamadı")
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Çıkış yapılırken hata oluştu: ${e.message}")
            _homeState.value = HomeState.Error(e.message ?: "Çıkış yapılamadı")
        }
    }
} 