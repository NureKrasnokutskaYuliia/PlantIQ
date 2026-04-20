package com.plantiq

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.plantiq.data.model.PlantResponseDto
import com.plantiq.ui.screens.AddDeviceScreen
import com.plantiq.ui.screens.LoginScreen
import com.plantiq.ui.screens.MyDevicesScreen
import com.plantiq.ui.screens.AnalyticsScreen
import com.plantiq.ui.screens.WateringScreen
import com.plantiq.ui.screens.PlantsListScreen
import com.google.gson.Gson

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.plantiq.ui.screens.PlantDetailScreen
import com.plantiq.ui.screens.PlantEditScreen

val PlantGreen = Color(0xFF2E7D32)
val PlantLightGreen = Color(0xFF60AD5E)
val PlantDarkGreen = Color(0xFF005005)

val PlantColorPalette = lightColorScheme(
    primary = PlantGreen,
    onPrimary = Color.White,
    primaryContainer = PlantLightGreen,
    onPrimaryContainer = Color.White,
    secondary = PlantDarkGreen,
    onSecondary = Color.White,
    surface = Color(0xFFE8F5E9),
    background = Color(0xFFF1F8E9)
)

@Composable
fun App(startDestination: String = "login") {
    MaterialTheme(colorScheme = PlantColorPalette) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()
            val gson = remember { Gson() }

            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.safeDrawingPadding()
            ) {
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate("plants_list") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onNavigateToRegister = {
                            navController.navigate("register")
                        }
                    )
                }

                composable("register") {
                    com.plantiq.ui.screens.RegisterScreen(
                        onAutoLoginSuccess = {
                            navController.navigate("plants_list") {
                                popUpTo("register") { inclusive = true }
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onNavigateToLogin = {
                            navController.navigate("login") {
                                popUpTo("register") { inclusive = true }
                            }
                        }
                    )
                }

                composable("plants_list") {
                    PlantsListScreen(
                        onNavigateToProfile = { navController.navigate("profile") },
                        onAddPlant = { navController.navigate("plant_edit") },
                        onEditPlant = { plant ->
                            val plantJson = java.net.URLEncoder.encode(gson.toJson(plant), "UTF-8")
                            navController.navigate("plant_edit?plant=$plantJson")
                        },
                        onPlantClick = { plant ->
                            val plantJson = java.net.URLEncoder.encode(gson.toJson(plant), "UTF-8")
                            navController.navigate("plant_detail?plant=$plantJson")
                        }
                    )
                }

                composable("profile") {
                    com.plantiq.ui.screens.ProfileScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToLogin = {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToAddDevice = {
                            navController.navigate("my_devices")
                        }
                    )
                }

                composable("my_devices") {
                    MyDevicesScreen(
                        onAddDevice = { navController.navigate("add_device") },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable("add_device") {
                    AddDeviceScreen(
                        onSaved = { navController.popBackStack() },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable("plant_edit") {
                    PlantEditScreen(
                        existingPlant = null,
                        onSaved = {
                            navController.navigate("plants_list") {
                                popUpTo("plants_list") { inclusive = true }
                            }
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable("plant_edit?plant={plant}") { backStackEntry ->
                    val plantJson = backStackEntry.arguments?.getString("plant")
                    val plant: PlantResponseDto? = try {
                        plantJson?.let {
                            gson.fromJson(java.net.URLDecoder.decode(it, "UTF-8"), PlantResponseDto::class.java)
                        }
                    } catch (_: Exception) { null }

                    PlantEditScreen(
                        existingPlant = plant,
                        onSaved = {
                            navController.navigate("plants_list") {
                                popUpTo("plants_list") { inclusive = true }
                            }
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable("plant_detail?plant={plant}") { backStackEntry ->
                    val plantJson = backStackEntry.arguments?.getString("plant")
                    val plant: PlantResponseDto? = try {
                        plantJson?.let {
                            gson.fromJson(java.net.URLDecoder.decode(it, "UTF-8"), PlantResponseDto::class.java)
                        }
                    } catch (_: Exception) { null }

                    if (plant != null) {
                        PlantDetailScreen(
                            plant = plant,
                            onEdit = {
                                val encoded = java.net.URLEncoder.encode(gson.toJson(plant), "UTF-8")
                                navController.navigate("plant_edit?plant=$encoded")
                            },
                            onNavigateToAnalytics = {
                                navController.navigate("plant_analytics?id=${plant.plantId}&name=${plant.name}")
                            },
                            onNavigateToWatering = {
                                navController.navigate("plant_watering?id=${plant.plantId}&name=${plant.name}")
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable("plant_analytics?id={id}&name={name}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
                    val name = backStackEntry.arguments?.getString("name") ?: "Рослина"
                    AnalyticsScreen(plantName = name, plantId = id, onNavigateBack = { navController.popBackStack() })
                }

                composable("plant_watering?id={id}&name={name}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
                    val name = backStackEntry.arguments?.getString("name") ?: "Рослина"
                    WateringScreen(plantName = name, plantId = id, onNavigateBack = { navController.popBackStack() })
                }
            }
        }
    }
}