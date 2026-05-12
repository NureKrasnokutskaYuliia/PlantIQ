package com.plantiq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantiq.data.remote.ApiClient
import com.plantiq.data.model.SensorDataResponseDto
import io.ktor.client.call.body
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AnalyticsState {
    object Loading : AnalyticsState()
    data class Success(val history: List<SensorDataResponseDto>) : AnalyticsState()
    data class Error(val message: String) : AnalyticsState()
}

class AnalyticsViewModel : ViewModel() {
    private val _state = MutableStateFlow<AnalyticsState>(AnalyticsState.Loading)
    val state: StateFlow<AnalyticsState> = _state.asStateFlow()

    fun loadHistory(plantId: Int) {
        viewModelScope.launch {
            _state.value = AnalyticsState.Loading
            try {
                val response = ApiClient.api.getSensorData(plantId, limit = 50)
                if (response.status.value in 200..299) {
                    val list = response.body<List<SensorDataResponseDto>>()
                    _state.value = AnalyticsState.Success(list.reversed()) // Oldest first for charts
                } else {
                    _state.value = AnalyticsState.Error("Не вдалося завантажити історію")
                }
            } catch (e: Exception) {
                _state.value = AnalyticsState.Error("Помилка з'єднання: ${e.message}")
            }
        }
    }
}
