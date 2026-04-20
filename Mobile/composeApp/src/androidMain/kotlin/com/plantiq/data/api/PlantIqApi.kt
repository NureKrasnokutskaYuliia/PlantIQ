package com.plantiq.data.api

import com.plantiq.data.model.CreateDeviceDto
import com.plantiq.data.model.LoginRequestDto
import com.plantiq.data.model.LoginResponseDto
import com.plantiq.data.model.PlantResponseDto
import com.plantiq.data.model.CreatePlantDto
import com.plantiq.data.model.CreateSensorDataDto
import com.plantiq.data.model.UpdatePlantDto
import com.plantiq.data.model.RegisterRequestDto
import com.plantiq.data.model.UpdateUserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

import io.ktor.client.request.put
import io.ktor.client.request.patch
import com.plantiq.data.model.UpdateFcmTokenDto
import io.ktor.client.request.delete

class PlantIqApi(private val client: HttpClient) {
    suspend fun updateProfile(userId: Int, request: UpdateUserDto): HttpResponse {
        return client.put("api/Users/$userId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun deleteAccount(userId: Int): HttpResponse {
        return client.delete("api/Users/$userId")
    }

    suspend fun register(request: RegisterRequestDto): HttpResponse {
        return client.post("api/Users") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun login(request: LoginRequestDto): HttpResponse {
        return client.post("api/Users/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun getUserPlants(): HttpResponse {
        return client.get("api/Plants/User")
    }

    suspend fun getAllPlants(): HttpResponse {
        return client.get("api/Plants/User")
    }

    suspend fun createPlant(request: CreatePlantDto): HttpResponse {
        return client.post("api/Plants") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun updatePlant(plantId: Int, request: UpdatePlantDto): HttpResponse {
        return client.put("api/Plants/$plantId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun deletePlant(plantId: Int): HttpResponse {
        return client.delete("api/Plants/$plantId")
    }

    suspend fun getDevices(): HttpResponse {
        return client.get("api/Devices")
    }

    suspend fun createDevice(request: CreateDeviceDto): HttpResponse {
        return client.post("api/Devices") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun deleteDevice(deviceId: Int): HttpResponse {
        return client.delete("api/Devices/$deviceId")
    }

    suspend fun getSensorData(plantId: Int, limit: Int = 1): HttpResponse {
        return client.get("api/SensorData/Plant/$plantId?limit=$limit")
    }

    suspend fun getNotifications(userId: Int, unreadOnly: Boolean = false): HttpResponse {
        return client.get("api/Notifications/User/$userId?unreadOnly=$unreadOnly")
    }

    suspend fun markNotificationRead(notificationId: Int): HttpResponse {
        return client.put("api/Notifications/$notificationId/read")
    }

    suspend fun markAllNotificationsRead(userId: Int): HttpResponse {
        return client.put("api/Notifications/User/$userId/read-all")
    }

    suspend fun createSensorData(request: CreateSensorDataDto): HttpResponse {
        return client.post("api/SensorData") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun getWateringSchedules(plantId: Int): HttpResponse {
        return client.get("api/WateringSchedules/Plant/$plantId")
    }

    suspend fun createWateringSchedule(request: com.plantiq.data.model.CreateWateringScheduleDto): HttpResponse {
        return client.post("api/WateringSchedules") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun deleteWateringSchedule(scheduleId: Int): HttpResponse {
        return client.delete("api/WateringSchedules/$scheduleId")
    }

    suspend fun updateWateringSchedule(scheduleId: Int, request: com.plantiq.data.model.UpdateWateringScheduleDto): HttpResponse {
        return client.put("api/WateringSchedules/$scheduleId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun getWateringEvents(plantId: Int): HttpResponse {
        return client.get("api/WateringEvents/Plant/$plantId")
    }

    suspend fun createWateringEvent(request: com.plantiq.data.model.WateringEventResponseDto): HttpResponse {
        return client.post("api/WateringEvents") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun getPlantSpecies(): HttpResponse {
        return client.get("api/PlantSpecies")
    }

    suspend fun createPlantSpecies(request: com.plantiq.data.model.CreatePlantSpeciesDto): HttpResponse {
        return client.post("api/PlantSpecies") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun updateDeviceToken(fcmToken: String): HttpResponse {
        return client.put("api/Users/UpdateDeviceToken") { 
            contentType(ContentType.Application.Json)
            setBody(UpdateFcmTokenDto(fcmToken)) 
        }
    }
}
