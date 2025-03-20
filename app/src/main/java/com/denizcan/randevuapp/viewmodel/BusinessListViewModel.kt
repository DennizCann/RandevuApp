package com.denizcan.randevuapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denizcan.randevuapp.model.User
import com.denizcan.randevuapp.service.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BusinessListViewModel : ViewModel() {
    private val firebaseService = FirebaseService()
    
    private val _businessListState = MutableStateFlow<BusinessListState>(BusinessListState.Loading)
    val businessListState = _businessListState.asStateFlow()

    sealed class BusinessListState {
        object Loading : BusinessListState()
        data class Success(
            val businesses: List<User.Business>,
            val sectors: List<String>
        ) : BusinessListState()
        data class Error(val message: String) : BusinessListState()
    }

    init {
        loadBusinesses()
    }

    private fun loadBusinesses() {
        viewModelScope.launch {
            try {
                _businessListState.value = BusinessListState.Loading
                val businesses = firebaseService.getBusinesses()
                val sectors = businesses.map { it.sector }.distinct()
                _businessListState.value = BusinessListState.Success(businesses, sectors)
            } catch (e: Exception) {
                _businessListState.value = BusinessListState.Error(e.message ?: "İşletmeler yüklenemedi")
            }
        }
    }
} 