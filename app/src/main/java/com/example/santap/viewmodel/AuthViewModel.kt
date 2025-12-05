package com.example.santap.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.santap.data.AuthRepository
import com.example.santap.data.User
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    var currentUser by mutableStateOf<User?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // register & login
    fun register(
        name: String,
        phone: String,
        email: String,
        password: String,
        role: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = repository.register(email, password, name, phone, role)
            isLoading = false

            result.onSuccess {
                login(email, password) { onSuccess() }
            }.onFailure {
                errorMessage = it.message
            }
        }
    }

    // login dan menyimpan user
    fun login(
        email: String,
        password: String,
        onSuccess: (role: String) -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = repository.login(email, password)
            isLoading = false

            result.onSuccess { user ->
                currentUser = user
                onSuccess(user.role)
            }.onFailure {
                errorMessage = it.message
            }
        }
    }

    // menghapus sesi user
    fun logout() {
        repository.logout()
        currentUser = null
    }
}
