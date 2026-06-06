package com.plantiq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantiq.data.model.ForgotPasswordResponseDto
import com.plantiq.data.remote.ApiClient
import io.ktor.client.call.body
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Loading : ForgotPasswordState()
    data class CodeSent(val email: String, val code: String) : ForgotPasswordState()
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
            _forgotState.value = ForgotPasswordState.Error("Введіть email")
            return
        }
        viewModelScope.launch {
            _forgotState.value = ForgotPasswordState.Loading
            try {
                val response = ApiClient.api.forgotPassword(email)
                if (response.status.value == 200) {
                    val body = response.body<ForgotPasswordResponseDto>()
                    _forgotState.value = ForgotPasswordState.CodeSent(email, body.code)
                } else if (response.status.value == 404) {
                    _forgotState.value = ForgotPasswordState.Error("Користувача з такою поштою не знайдено.")
                } else {
                    _forgotState.value = ForgotPasswordState.Error("Помилка сервера. Спробуйте пізніше.")
                }
            } catch (e: Exception) {
                _forgotState.value = ForgotPasswordState.Error("Немає зв'язку з сервером.")
            }
        }
    }

    fun resetPassword(email: String, code: String, newPassword: String, confirmPassword: String) {
        if (newPassword != confirmPassword) {
            _resetState.value = ResetPasswordState.Error("Паролі не співпадають")
            return
        }
        if (newPassword.length < 6) {
            _resetState.value = ResetPasswordState.Error("Мінімум 6 символів")
            return
        }
        viewModelScope.launch {
            _resetState.value = ResetPasswordState.Loading
            try {
                val response = ApiClient.api.resetPassword(email, code, newPassword)
                if (response.status.value == 204) {
                    _resetState.value = ResetPasswordState.Success
                } else {
                    _resetState.value = ResetPasswordState.Error("Невірний або застарілий код підтвердження.")
                }
            } catch (e: Exception) {
                _resetState.value = ResetPasswordState.Error("Немає зв'язку з сервером.")
            }
        }
    }

    fun resetForgotState() { _forgotState.value = ForgotPasswordState.Idle }
    fun resetResetState() { _resetState.value = ResetPasswordState.Idle }
}
