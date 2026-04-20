package com.plantiq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantiq.data.api.ApiClient
import com.plantiq.data.model.LoginRequestDto
import com.plantiq.data.model.UserResponseDto
import com.plantiq.data.model.LoginResponseDto
import io.ktor.client.call.body
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: UserResponseDto) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = ApiClient.api.login(LoginRequestDto(email, pass))
                if (response.status.value in 200..299) {
                    val body: LoginResponseDto = response.body()
                    ApiClient.token = body.token
                    _authState.value = AuthState.Success(body.user)
                } else {
                    _authState.value = AuthState.Error("Користувача з такою поштою та паролем не існує")
                }
            } catch (e: Exception) {
                println("Auth Error (API): ${e.message}")
                e.printStackTrace()

                val errorMsg = if (e.localizedMessage?.contains("timeout", ignoreCase = true) == true) {
                    "Сервер прокидається після сплячки, зачекайте ще кілька секунд..."
                } else {
                    "Немає зв'язку з інтернетом або сервером."
                }
                _authState.value = AuthState.Error(errorMsg)
            }
        }
    }
}
