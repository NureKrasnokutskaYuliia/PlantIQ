package com.plantiq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantiq.data.api.ApiClient
import com.plantiq.data.model.RegisterRequestDto
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import io.ktor.client.call.body
import com.plantiq.data.model.LoginRequestDto
import com.plantiq.data.model.LoginResponseDto
import com.plantiq.data.model.UserResponseDto

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val user: UserResponseDto, val token: String) : RegisterState()
    data class Error(val message: String) : RegisterState()
}

class RegisterViewModel : ViewModel() {
    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    fun register(name: String, email: String, pass: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            try {
                val response = ApiClient.api.register(RegisterRequestDto(name, email, pass))
                if (response.status.value in 200..299) {
                    // Registration successful! Let's auto-login!
                    val loginResponse = ApiClient.api.login(LoginRequestDto(email, pass))
                    if (loginResponse.status.value in 200..299) {
                        val body: LoginResponseDto = loginResponse.body()
                        _registerState.value = RegisterState.Success(body.user, body.token)
                    } else {
                        _registerState.value = RegisterState.Error("Реєстрація успішна, але автоматичний вхід не вдався. Зайдіть вручну.")
                    }
                } else if (response.status.value == 400 || response.status.value == 409) {
                    _registerState.value = RegisterState.Error("Така пошта або ім'я вже зареєстровані.")
                } else {
                    _registerState.value = RegisterState.Error("Не вдалося зареєструватись, спробуйте пізніше.")
                }
            } catch (e: Exception) {
                val errorMsg = if (e.localizedMessage?.contains("timeout", ignoreCase = true) == true) {
                    "Перше з'єднання з сервером. Зачекайте хвилинку..."
                } else {
                    "Немає зв'язку з сервером."
                }
                _registerState.value = RegisterState.Error(errorMsg)
            }
        }
    }
}
