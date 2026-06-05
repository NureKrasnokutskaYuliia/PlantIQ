package com.plantiq.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.plantiq.data.local.TokenManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

class PlantIQFMS : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val notificationsEnabled = runBlocking {
            TokenManager(applicationContext).notificationsEnabledFlow.firstOrNull() ?: true
        }

        if (!notificationsEnabled) return

        val title = message.notification?.title ?: message.data["title"] ?: "PlantIQ"
        val body = message.notification?.body ?: message.data["body"] ?: ""

        NotificationHelper.showNotification(
            context = this,
            id = System.currentTimeMillis().toInt(),
            title = title,
            message = body
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("PlantIQ: New FCM Token generated: $token")
    }
}