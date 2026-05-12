package com.plantiq.data.model

import com.google.gson.annotations.SerializedName

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
