package com.plantiq.data.model

import com.google.gson.annotations.SerializedName

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
