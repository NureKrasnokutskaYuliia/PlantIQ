package com.plantiq.data.model

import com.google.gson.annotations.SerializedName

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
