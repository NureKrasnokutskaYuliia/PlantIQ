package com.plantiq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.plantiq.data.model.TokenManager
import com.plantiq.data.api.ApiClient
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.firstOrNull
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.plantiq.notifications.*
import io.ktor.client.statement.bodyAsText

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            println("Notification permission granted")
        } else {
            println("Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.createChannel(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val context = LocalContext.current
            val tokenManager = remember { TokenManager(context) }
            
            val startDestination = produceState<String?>(initialValue = null) {
                tokenManager.tokenFlow.collect { token ->
                    val lastTime = tokenManager.lastActivityTimeFlow.firstOrNull() ?: 0L
                    val currentTime = System.currentTimeMillis()
                    
                    val timeoutMillis = 1 * 60 * 60 * 1000L 

                    if (token.isNullOrEmpty()) {
                        value = "login"
                    } else if (lastTime != 0L && (currentTime - lastTime) > timeoutMillis) {
                        tokenManager.clearSession()
                        value = "login"
                    } else {
                        ApiClient.token = token
                        tokenManager.updateLastActivityTime()
                        value = "plants_list"
                    }
                }
            }

            LaunchedEffect(startDestination.value) {
                if (startDestination.value == "plants_list") {
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val fcmToken = task.result
                            lifecycleScope.launch {
                                
                                try {
                                    val response = ApiClient.api.updateDeviceToken(fcmToken)
                                    if (response.status.value in 200..299) {
                                        println("FCM: Token successfully sent to server")
                                    } else {
                                        val errorBody = response.bodyAsText() 
                                        println("FCM: Server rejected token. Status: ${response.status}")
                                        println("FCM REASON: $errorBody") 
                                    }
                                } catch (e: Exception) {
                                    println("FCM Error: Could not send token to server. ${e.message}")
                                }
                            }
                        }
                    }
                }
            }

            if (startDestination.value != null) {
                App(startDestination = startDestination.value!!)
            } else {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.wrapContentSize())
                }
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}