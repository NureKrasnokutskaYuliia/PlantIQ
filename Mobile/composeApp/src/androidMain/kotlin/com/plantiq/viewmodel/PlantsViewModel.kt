package com.plantiq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantiq.data.api.ApiClient
import com.plantiq.data.model.PlantResponseDto
import io.ktor.client.call.body
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PlantsState {
    object Loading : PlantsState()
    data class Success(val plants: List<PlantResponseDto>) : PlantsState()
    data class Error(val message: String) : PlantsState()
}

class PlantsViewModel : ViewModel() {

    private val _plantsState = MutableStateFlow<PlantsState>(PlantsState.Loading)
    val plantsState: StateFlow<PlantsState> = _plantsState.asStateFlow()

    fun loadPlants() {
        viewModelScope.launch {
            _plantsState.value = PlantsState.Loading
            try {
                val response = ApiClient.api.getUserPlants()
                if (response.status.value in 200..299) {
                    val body: List<PlantResponseDto> = response.body()
                    _plantsState.value = PlantsState.Success(body)
                } else {
                    _plantsState.value = PlantsState.Error("Failed to fetch plants: ${response.status.value}")
                }
            } catch (e: Exception) {
                _plantsState.value = PlantsState.Error("Error: ${e.localizedMessage}")
            }
        }
    }
}
