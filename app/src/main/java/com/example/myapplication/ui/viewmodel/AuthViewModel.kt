package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.AuthResult
import com.example.myapplication.data.repository.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)

    val currentUser: StateFlow<UserSession?> = authRepository.currentUser

    private val _authState = MutableStateFlow<AuthResult?>(null)
    val authState: StateFlow<AuthResult?> = _authState.asStateFlow()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            authRepository.login(email, pass) { result ->
                _authState.value = result
            }
        }
    }

    fun register(name: String, email: String, pass: String) {
        viewModelScope.launch {
            authRepository.register(name, email, pass) { result ->
                _authState.value = result
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _authState.value = null
    }

    fun resetAuthState() {
        _authState.value = null
    }

    fun isFirebaseConfigured(): Boolean = authRepository.isFirebaseConfigured()
}
