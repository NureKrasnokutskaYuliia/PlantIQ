package com.plantiq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantiq.data.remote.ApiClient
import com.plantiq.data.model.CreateWateringScheduleDto
import com.plantiq.data.model.WateringEventResponseDto
import com.plantiq.data.model.WateringScheduleResponseDto
import io.ktor.client.call.body
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WateringState {
    object Loading : WateringState()
    data class Success(
        val schedules: List<WateringScheduleResponseDto>,
        val history: List<WateringEventResponseDto>
    ) : WateringState()
    data class Error(val message: String) : WateringState()
}

class WateringViewModel : ViewModel() {
    private val _state = MutableStateFlow<WateringState>(WateringState.Loading)
    val state: StateFlow<WateringState> = _state.asStateFlow()

    fun loadData(plantId: Int) {
        viewModelScope.launch {
            _state.value = WateringState.Loading
            try {
                val schedulesResponse = ApiClient.api.getWateringSchedules(plantId)
                val historyResponse = ApiClient.api.getWateringEvents(plantId)

                if (schedulesResponse.status.value in 200..299 && historyResponse.status.value in 200..299) {
                    _state.value = WateringState.Success(
                        schedules = schedulesResponse.body(),
                        history = historyResponse.body<List<WateringEventResponseDto>>().reversed()
                    )
                } else {
                    _state.value = WateringState.Error("Не вдалося завантажити дані поливу")
                }
            } catch (e: Exception) {
                _state.value = WateringState.Error("Помилка: ${e.message}")
            }
        }
    }

    fun addSchedule(dto: CreateWateringScheduleDto) {
        viewModelScope.launch {
            try {
                ApiClient.api.createWateringSchedule(dto)
                loadData(dto.plantId)
            } catch (_: Exception) {}
        }
    }

    fun updateSchedule(plantId: Int, scheduleId: Int, dto: com.plantiq.data.model.UpdateWateringScheduleDto) {
        viewModelScope.launch {
            try {
                ApiClient.api.updateWateringSchedule(scheduleId, dto)
                loadData(plantId)
            } catch (_: Exception) {}
        }
    }

    fun deleteSchedule(plantId: Int, scheduleId: Int) {
        viewModelScope.launch {
            try {
                ApiClient.api.deleteWateringSchedule(scheduleId)
                loadData(plantId)
            } catch (_: Exception) {}
        }
    }

    fun waterNow(plantId: Int) {
        viewModelScope.launch {
            try {
                val event = WateringEventResponseDto(
                    eventId = 0,
                    plantId = plantId,
                    timestamp = "",
                    amountMl = 100,
                    mode = "Manual",
                    status = "Completed"
                )
                ApiClient.api.createWateringEvent(event)
                loadData(plantId)
            } catch (_: Exception) {}
        }
    }
}
