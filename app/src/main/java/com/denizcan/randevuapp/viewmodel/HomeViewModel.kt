package com.denizcan.randevuapp.viewmodel

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
        viewModelScope.launch {
            try {
                _homeState.value = HomeState.Loading
                auth.signOut()
                _homeState.value = HomeState.Success
            } catch (e: Exception) {
                _homeState.value = HomeState.Error(e.message ?: "Çıkış yapılamadı")
            }
        }
    }
} 