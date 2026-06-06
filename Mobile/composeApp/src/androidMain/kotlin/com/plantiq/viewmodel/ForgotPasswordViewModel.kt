package com.plantiq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantiq.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Loading : ForgotPasswordState()
    data class CodeSent(val email: String) : ForgotPasswordState()
    data class Error(val message: String) : ForgotPasswordState()
}

sealed class ResetPasswordState {
    object Idle : ResetPasswordState()
    object Loading : ResetPasswordState()
    object Success : ResetPasswordState()
    data class Error(val message: String) : ResetPasswordState()
}

class ForgotPasswordViewModel : ViewModel() {

    private val _forgotState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val forgotState: StateFlow<ForgotPasswordState> = _forgotState.asStateFlow()

    private val _resetState = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Idle)
    val resetState: StateFlow<ResetPasswordState> = _resetState.asStateFlow()

    fun sendResetCode(email: String) {
        if (email.isBlank()) {
            _forgotState.value = ForgotPasswordState.Error("Please enter your email")
            return
        }
        viewModelScope.launch {
            _forgotState.value = ForgotPasswordState.Loading
            try {
                val response = ApiClient.api.forgotPassword(email)
                when (response.status.value) {
                    200 -> _forgotState.value = ForgotPasswordState.CodeSent(email)
                    404 -> _forgotState.value = ForgotPasswordState.Error("No account found with that email address.")
                    else -> _forgotState.value = ForgotPasswordState.Error("Server error. Please try again later.")
                }
            } catch (e: Exception) {
                _forgotState.value = ForgotPasswordState.Error("No connection to server.")
            }
        }
    }

    fun resetPassword(email: String, code: String, newPassword: String, confirmPassword: String) {
        if (newPassword != confirmPassword) {
            _resetState.value = ResetPasswordState.Error("Passwords do not match")
            return
        }
        if (newPassword.length < 6) {
            _resetState.value = ResetPasswordState.Error("Minimum 6 characters required")
            return
        }
        viewModelScope.launch {
            _resetState.value = ResetPasswordState.Loading
            try {
                val response = ApiClient.api.resetPassword(email, code, newPassword)
                if (response.status.value == 204) {
                    _resetState.value = ResetPasswordState.Success
                } else {
                    _resetState.value = ResetPasswordState.Error("Invalid or expired verification code.")
                }
            } catch (e: Exception) {
                _resetState.value = ResetPasswordState.Error("No connection to server.")
            }
        }
    }

    fun resetForgotState() { _forgotState.value = ForgotPasswordState.Idle }
    fun resetResetState() { _resetState.value = ResetPasswordState.Idle }
}
