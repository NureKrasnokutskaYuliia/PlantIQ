package com.plantiq.data.model

import com.google.gson.annotations.SerializedName

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
