package com.plantiq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantiq.data.local.TokenManager
import com.plantiq.data.remote.ApiClient
import com.plantiq.data.model.UpdateUserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    object Success : ProfileState()
    data class Error(val message: String) : ProfileState()
}

class ProfileViewModel(private val tokenManager: TokenManager) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    fun updateProfile(name: String, email: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val userId = tokenManager.userIdFlow.firstOrNull()
                if (userId == null) {
                    _profileState.value = ProfileState.Error("User not found.")
                    return@launch
                }

                val request = UpdateUserDto(name = name, email = email, role = "Owner", isActive = true)
                val response = ApiClient.api.updateProfile(userId, request)

                if (response.status.value in 200..299) {
                    tokenManager.updateProfileData(name, email)
                    _profileState.value = ProfileState.Success
                } else if (response.status.value == 400 || response.status.value == 409) {
                    _profileState.value = ProfileState.Error("This data is already in use by another account.")
                } else {
                    _profileState.value = ProfileState.Error("Failed to update profile. Please try again.")
                }
            } catch (e: Exception) {
                val errorMsg = if (e.localizedMessage?.contains("timeout", ignoreCase = true) == true) {
                    "No server connection (timeout). Check your internet."
                } else {
                    "Connection error. Please try again."
                }
                _profileState.value = ProfileState.Error(errorMsg)
            }
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            tokenManager.clearSession()
            ApiClient.token = null
            onLogoutComplete()
        }
    }

    fun deleteAccount(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val userId = tokenManager.userIdFlow.firstOrNull()
                if (userId != null) {
                    ApiClient.api.deleteAccount(userId)
                }
                logout(onLogoutComplete)
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Failed to delete account. Check your internet connection.")
            }
        }
    }
}
