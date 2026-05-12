package com.plantiq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantiq.data.remote.ApiClient
import com.plantiq.data.model.CreatePlantDto
import com.plantiq.data.model.DeviceResponseDto
import com.plantiq.data.model.PlantResponseDto
import com.plantiq.data.model.UpdatePlantDto
import io.ktor.client.call.body
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PlantEditState {
    object Idle : PlantEditState()
    object Loading : PlantEditState()
    object Saved : PlantEditState()
    data class Error(val message: String) : PlantEditState()
}

class PlantEditViewModel : ViewModel() {
    private val _editState = MutableStateFlow<PlantEditState>(PlantEditState.Idle)
    val editState: StateFlow<PlantEditState> = _editState.asStateFlow()

    private val _availableDevices = MutableStateFlow<List<DeviceResponseDto>>(emptyList())
    val availableDevices: StateFlow<List<DeviceResponseDto>> = _availableDevices.asStateFlow()

    private val _isLoadingDevices = MutableStateFlow(true)
    val isLoadingDevices: StateFlow<Boolean> = _isLoadingDevices.asStateFlow()

    private val _speciesList = MutableStateFlow<List<com.plantiq.data.model.PlantSpeciesResponseDto>>(emptyList())
    val speciesList: StateFlow<List<com.plantiq.data.model.PlantSpeciesResponseDto>> = _speciesList.asStateFlow()

    init {
        loadSpecies()
    }

    private fun loadSpecies() {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.getPlantSpecies()
                if (response.status.value in 200..299) {
                    _speciesList.value = response.body()
                    println("PlantEdit: Loaded ${_speciesList.value.size} species")
                } else {
                    println("PlantEdit: Failed to load species. Status: ${response.status}")
                }
            } catch (e: Exception) {
                println("PlantEdit Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // Load devices, keeping only those that belong to currentUser AND are not linked to other plants
    fun loadAvailableDevices(currentUserId: Int, currentPlantId: Int? = null) {
        viewModelScope.launch {
            _isLoadingDevices.value = true
            try {
                // Fetch both in parallel
                val devicesDeferred = async { ApiClient.api.getDevices() }
                val plantsDeferred  = async { ApiClient.api.getUserPlants() }

                val devicesResponse = devicesDeferred.await()
                val plantsResponse  = plantsDeferred.await()

                val allDevices = if (devicesResponse.status.value in 200..299)
                    devicesResponse.body<List<DeviceResponseDto>>()
                else emptyList()

                val allPlants = if (plantsResponse.status.value in 200..299)
                    plantsResponse.body<List<PlantResponseDto>>()
                else emptyList()

                // Keep only devices that belong to THIS user
                val myDevices = allDevices.filter { it.userId == currentUserId }

                // Collect deviceIds already used by OTHER plants of this user
                val usedDeviceIds = allPlants
                    .filter { it.plantId != (currentPlantId ?: -1) }
                    .mapNotNull { it.deviceId }
                    .toSet()

                // Free = owned by user AND not linked to any other plant
                _availableDevices.value = myDevices.filter { it.deviceId !in usedDeviceIds }
            } catch (e: Exception) {
                _availableDevices.value = emptyList()
            } finally {
                _isLoadingDevices.value = false
            }
        }
    }

    fun createPlant(
        name: String, 
        species: String, 
        notes: String, 
        deviceId: Int?,
        moistureMin: Double?,
        moistureMax: Double?,
        lightMin: Double?,
        lightMax: Double?,
        wateringMode: String
    ) {
        if (name.isBlank()) {
            _editState.value = PlantEditState.Error("Введіть назву рослини")
            return
        }
        
        // Simple validation: Min should not be greater than Max
        if (moistureMin != null && moistureMax != null && moistureMin > moistureMax) {
            _editState.value = PlantEditState.Error("Мін. вологість не може бути більшою за макс.")
            return
        }
        if (lightMin != null && lightMax != null && lightMin > lightMax) {
            _editState.value = PlantEditState.Error("Мін. освітленість не може бути більшою за макс.")
            return
        }

        viewModelScope.launch {
            _editState.value = PlantEditState.Loading
            try {
                val request = CreatePlantDto(
                    name = name.trim(),
                    deviceId = deviceId,
                    species = species.trim().ifBlank { null },
                    optimalMoistureMin = moistureMin,
                    optimalMoistureMax = moistureMax,
                    optimalLightMin = lightMin,
                    optimalLightMax = lightMax,
                    notes = notes.trim().ifBlank { null },
                    wateringMode = wateringMode
                )
                val response = ApiClient.api.createPlant(request)
                if (response.status.value in 200..299) {
                    _editState.value = PlantEditState.Saved
                } else if (response.status.value == 400) {
                    _editState.value = PlantEditState.Error("Цей пристрій вже підключено до іншої рослини")
                } else {
                    _editState.value = PlantEditState.Error("Не вдалося зберегти. Спробуйте пізніше.")
                }
            } catch (e: Exception) {
                _editState.value = PlantEditState.Error("Помилка з'єднання. Перевірте інтернет.")
            }
        }
    }

    fun updatePlant(
        plantId: Int, 
        name: String, 
        species: String, 
        notes: String, 
        deviceId: Int?, 
        isActive: Boolean,
        moistureMin: Double?,
        moistureMax: Double?,
        lightMin: Double?,
        lightMax: Double?,
        wateringMode: String
    ) {
        if (name.isBlank()) {
            _editState.value = PlantEditState.Error("Введіть назву рослини")
            return
        }

        if (moistureMin != null && moistureMax != null && moistureMin > moistureMax) {
            _editState.value = PlantEditState.Error("Мін. вологість не може бути більшою за макс.")
            return
        }
        if (lightMin != null && lightMax != null && lightMin > lightMax) {
            _editState.value = PlantEditState.Error("Мін. освітленість не може бути більшою за макс.")
            return
        }

        viewModelScope.launch {
            _editState.value = PlantEditState.Loading
            try {
                val request = UpdatePlantDto(
                    name = name.trim(),
                    deviceId = deviceId,
                    species = species.trim().ifBlank { null },
                    optimalMoistureMin = moistureMin,
                    optimalMoistureMax = moistureMax,
                    optimalLightMin = lightMin,
                    optimalLightMax = lightMax,
                    notes = notes.trim().ifBlank { null },
                    isActive = isActive,
                    wateringMode = wateringMode
                )
                val response = ApiClient.api.updatePlant(plantId, request)
                if (response.status.value in 200..299) {
                    _editState.value = PlantEditState.Saved
                } else if (response.status.value == 400) {
                    _editState.value = PlantEditState.Error("Цей пристрій вже підключено до іншої рослини")
                } else {
                    _editState.value = PlantEditState.Error("Не вдалося оновити. Спробуйте пізніше.")
                }
            } catch (e: Exception) {
                _editState.value = PlantEditState.Error("Помилка з'єднання. Перевірте інтернет.")
            }
        }
    }
}
