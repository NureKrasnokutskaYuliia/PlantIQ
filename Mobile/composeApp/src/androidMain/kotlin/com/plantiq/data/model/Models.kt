package com.plantiq.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponseDto(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: UserResponseDto
)

data class UserResponseDto(
    @SerializedName("userId") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String, 
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("lastLogin") val lastLogin: String?
)

data class UpdateUserDto(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,     
    @SerializedName("isActive") val isActive: Boolean
)

data class RegisterRequestDto(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("role") val role: String = "Owner" 
)

data class UpdateFcmTokenDto(
    @SerializedName("token") val token: String
)

data class PlantResponseDto(
    @SerializedName("plantId") val plantId: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("deviceId") val deviceId: Int?,
    @SerializedName("name") val name: String,
    @SerializedName("species") val species: String?,
    @SerializedName("optimalMoistureMin") val optimalMoistureMin: Double?,
    @SerializedName("optimalMoistureMax") val optimalMoistureMax: Double?,
    @SerializedName("optimalLightMin") val optimalLightMin: Double?,
    @SerializedName("optimalLightMax") val optimalLightMax: Double?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("wateringMode") val wateringMode: String
)

data class CreatePlantDto(
    @SerializedName("name") val name: String,
    @SerializedName("deviceId") val deviceId: Int?,
    @SerializedName("species") val species: String?,
    @SerializedName("optimalMoistureMin") val optimalMoistureMin: Double?,
    @SerializedName("optimalMoistureMax") val optimalMoistureMax: Double?,
    @SerializedName("optimalLightMin") val optimalLightMin: Double?,
    @SerializedName("optimalLightMax") val optimalLightMax: Double?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("wateringMode") val wateringMode: String = "Manual"
)

data class UpdatePlantDto(
    @SerializedName("name") val name: String,
    @SerializedName("deviceId") val deviceId: Int?,
    @SerializedName("species") val species: String?,
    @SerializedName("optimalMoistureMin") val optimalMoistureMin: Double?,
    @SerializedName("optimalMoistureMax") val optimalMoistureMax: Double?,
    @SerializedName("optimalLightMin") val optimalLightMin: Double?,
    @SerializedName("optimalLightMax") val optimalLightMax: Double?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("wateringMode") val wateringMode: String
)

data class DeviceResponseDto(
    @SerializedName("deviceId") val deviceId: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("model") val model: String?,
    @SerializedName("serialNumber") val serialNumber: String?,
    @SerializedName("status") val status: String?
)

data class CreateDeviceDto(
    @SerializedName("name") val name: String,
    @SerializedName("model") val model: String?,
    @SerializedName("serialNumber") val serialNumber: String?
)

data class CreateSensorDataDto(
    @SerializedName("plantId") val plantId: Int,
    @SerializedName("deviceId") val deviceId: Int,
    @SerializedName("soilMoisture") val soilMoisture: Double?,
    @SerializedName("lightIntensity") val lightIntensity: Double?,
    @SerializedName("batteryLevel") val batteryLevel: Double?
)

data class SensorDataResponseDto(
    @SerializedName("dataId") val dataId: Int,
    @SerializedName("plantId") val plantId: Int,
    @SerializedName("deviceId") val deviceId: Int,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("soilMoisture") val soilMoisture: Double?,
    @SerializedName("lightIntensity") val lightIntensity: Double?,
    @SerializedName("batteryLevel") val batteryLevel: Double?
)

data class NotificationResponseDto(
    @SerializedName("notificationId") val notificationId: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("plantId") val plantId: Int?,
    @SerializedName("deviceId") val deviceId: Int?,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("type") val type: Int,
    @SerializedName("message") val message: String,
    @SerializedName("read") val read: Boolean,
    @SerializedName("priority") val priority: Int
)

data class WateringScheduleResponseDto(
    @SerializedName("scheduleId") val scheduleId: Int,
    @SerializedName("plantId") val plantId: Int,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("intervalHours") val intervalHours: Int,
    @SerializedName("amountMl") val amountMl: Int,
    @SerializedName("enabled") val enabled: Boolean,
    @SerializedName("daysOfWeek") val daysOfWeek: List<Int>,
    @SerializedName("repeatCount") val repeatCount: Int,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class CreateWateringScheduleDto(
    @SerializedName("plantId") val plantId: Int,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("intervalHours") val intervalHours: Int,
    @SerializedName("amountMl") val amountMl: Int,
    @SerializedName("enabled") val enabled: Boolean = true,
    @SerializedName("daysOfWeek") val daysOfWeek: List<Int> = emptyList(),
    @SerializedName("repeatCount") val repeatCount: Int = 1
)

data class UpdateWateringScheduleDto(
    @SerializedName("startTime") val startTime: String,
    @SerializedName("intervalHours") val intervalHours: Int,
    @SerializedName("amountMl") val amountMl: Int,
    @SerializedName("enabled") val enabled: Boolean,
    @SerializedName("daysOfWeek") val daysOfWeek: List<Int>,
    @SerializedName("repeatCount") val repeatCount: Int
)

data class WateringEventResponseDto(
    @SerializedName("eventId") val eventId: Int,
    @SerializedName("plantId") val plantId: Int,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("amountMl") val amountMl: Int,
    @SerializedName("mode") val mode: String,
    @SerializedName("status") val status: String
)

data class PlantSpeciesResponseDto(
    @SerializedName("speciesId") val speciesId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("defaultMoistureMin") val defaultMoistureMin: Double?,
    @SerializedName("defaultMoistureMax") val defaultMoistureMax: Double?,
    @SerializedName("defaultLightMin") val defaultLightMin: Double?,
    @SerializedName("defaultLightMax") val defaultLightMax: Double?
)

data class CreatePlantSpeciesDto(
    @SerializedName("name") val name: String,
    @SerializedName("defaultMoistureMin") val moistureMin: Double?,
    @SerializedName("defaultMoistureMax") val moistureMax: Double?,
    @SerializedName("defaultLightMin") val lightMin: Double?,
    @SerializedName("defaultLightMax") val lightMax: Double?
)