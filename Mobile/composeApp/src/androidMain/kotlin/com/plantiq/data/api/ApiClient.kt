package com.plantiq.data.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.gson.gson

object ApiClient {
    private const val BASE_URL = "https://plantiq-mkp3.onrender.com/" 

    var token: String? = null

    val client: HttpClient by lazy {
        HttpClient(OkHttp) {
            engine {
                config {
                    connectTimeout(2, java.util.concurrent.TimeUnit.MINUTES)
                    readTimeout(2, java.util.concurrent.TimeUnit.MINUTES)
                    writeTimeout(2, java.util.concurrent.TimeUnit.MINUTES)
                }
            }
            install(ContentNegotiation) {
                gson()
            }
            install(Logging) {
                level = LogLevel.BODY
            }
            defaultRequest {
                url(BASE_URL)
                token?.let {
                    header("Authorization", "Bearer $it")
                }
            }
        }
    }

    val api: PlantIqApi by lazy {
        PlantIqApi(client)
    }
}
