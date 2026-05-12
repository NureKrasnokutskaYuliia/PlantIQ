package com.plantiq.data.model

import com.google.gson.annotations.SerializedName

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
