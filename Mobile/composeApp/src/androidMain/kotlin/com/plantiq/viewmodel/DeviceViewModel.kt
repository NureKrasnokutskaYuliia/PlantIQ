package com.plantiq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantiq.data.remote.ApiClient
import com.plantiq.data.model.CreateDeviceDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DeviceAddState {
    object Idle : DeviceAddState()
    object Loading : DeviceAddState()
    object Saved : DeviceAddState()
    data class Error(val message: String) : DeviceAddState()
}

class DeviceViewModel : ViewModel() {
    private val _state = MutableStateFlow<DeviceAddState>(DeviceAddState.Idle)
    val state: StateFlow<DeviceAddState> = _state.asStateFlow()

    fun createDevice(name: String, model: String, serialNumber: String) {
        if (name.isBlank()) {
            _state.value = DeviceAddState.Error("Введіть назву пристрою")
            return
        }
        viewModelScope.launch {
            _state.value = DeviceAddState.Loading
            try {
                val request = CreateDeviceDto(
                    name = name.trim(),
                    model = model.trim().ifBlank { null },
                    serialNumber = serialNumber.trim().ifBlank { null }
                )
                val response = ApiClient.api.createDevice(request)
                if (response.status.value in 200..299) {
                    _state.value = DeviceAddState.Saved
                } else {
                    _state.value = DeviceAddState.Error("Не вдалося зареєструвати пристрій. Спробуйте ще раз.")
                }
            } catch (e: Exception) {
                val msg = if (e.localizedMessage?.contains("timeout", ignoreCase = true) == true)
                    "Немає зв'язку з сервером. Перевірте інтернет."
                else
                    "Помилка з'єднання. Спробуйте пізніше."
                _state.value = DeviceAddState.Error(msg)
            }
        }
    }
}
