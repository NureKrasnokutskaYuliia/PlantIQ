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

data class RegisterRequestDto(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("role") val role: String = "Owner" 
)

data class UpdateUserDto(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,     
    @SerializedName("isActive") val isActive: Boolean
)

data class UpdateFcmTokenDto(
    @SerializedName("token") val token: String
)
