package com.plantiq.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plantiq.data.remote.ApiClient
import com.plantiq.data.model.PlantResponseDto
import com.plantiq.data.model.SensorDataResponseDto
import io.ktor.client.call.body
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SensorState {
    object Loading : SensorState()
    object NoData : SensorState()
    data class Success(val data: SensorDataResponseDto) : SensorState()
    data class Error(val message: String) : SensorState()
}

class PlantDetailViewModel : ViewModel() {
    private val _sensorState = MutableStateFlow<SensorState>(SensorState.Loading)
    val sensorState: StateFlow<SensorState> = _sensorState.asStateFlow()

    private val _isTesting = MutableStateFlow(false)
    val isTesting: StateFlow<Boolean> = _isTesting.asStateFlow()

    fun loadLatest(plantId: Int) {
        viewModelScope.launch {
            _sensorState.value = SensorState.Loading
            kotlinx.coroutines.delay(500)
            try {
                val response = ApiClient.api.getSensorData(plantId, limit = 1)
                if (response.status.value in 200..299) {
                    val list = response.body<List<SensorDataResponseDto>>()
                    if (list.isEmpty()) {
                        _sensorState.value = SensorState.NoData
                    } else {
                        _sensorState.value = SensorState.Success(list.first())
                    }
                } else {
                    _sensorState.value = SensorState.Error("Не вдалося отримати дані (${response.status.value})")
                }
            } catch (e: Exception) {
                _sensorState.value = SensorState.Error("Помилка з'єднання: ${e.message}")
            }
        }
    }

    fun testNotifications(plantId: Int, deviceId: Int) {
        viewModelScope.launch {
            _isTesting.value = true
            try {
                val request = com.plantiq.data.model.CreateSensorDataDto(
                    plantId = plantId,
                    deviceId = deviceId,
                    soilMoisture = 5.0,
                    lightIntensity = 5.0,
                    batteryLevel = 10.0
                )
                ApiClient.api.createSensorData(request)
            } catch (_: Exception) {
            } finally {
                _isTesting.value = false
            }
        }
    }
}

// Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    plant: PlantResponseDto,
    onEdit: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToWatering: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PlantDetailViewModel = viewModel()
) {
    val sensorState by viewModel.sensorState.collectAsState()
    val isTesting by viewModel.isTesting.collectAsState()

    LaunchedEffect(plant.plantId) {
        viewModel.loadLatest(plant.plantId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plant.name) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Редагувати")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("🌿 ${plant.name}", style = MaterialTheme.typography.titleLarge)
                    if (!plant.species.isNullOrBlank()) Text("Вид: ${plant.species}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!plant.notes.isNullOrBlank()) Text("Нотатки: ${plant.notes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (plant.deviceId != null) Text("📡 Пристрій підключено", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }

            Text("Керування та Аналітика", style = MaterialTheme.typography.titleMedium)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToAnalytics,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("📈 Аналітика")
                }
                Button(
                    onClick = onNavigateToWatering,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("💧 Полив")
                }
            }

            Text("Дані з датчиків", style = MaterialTheme.typography.titleMedium)

            when (val s = sensorState) {
                is SensorState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                is SensorState.NoData -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Даних ще немає. Переконайтесь, що пристрій підключений та надсилає вимірювання.",
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is SensorState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                is SensorState.Success -> {
                    val d = s.data

                    d.soilMoisture?.let { moisture ->
                        SensorCard(
                            label = "💧 Вологість ґрунту",
                            value = "${moisture.toInt()}%",
                            progress = (moisture / 100.0).toFloat().coerceIn(0f, 1f),
                            optimalMin = plant.optimalMoistureMin?.toFloat()?.div(100f),
                            optimalMax = plant.optimalMoistureMax?.toFloat()?.div(100f)
                        )
                    }

                    d.lightIntensity?.let { light ->
                        SensorCard(
                            label = "☀️ Освітлення",
                            value = "${light.toInt()}%",
                            progress = (light / 100.0).toFloat().coerceIn(0f, 1f),
                            optimalMin = plant.optimalLightMin?.toFloat()?.div(100f),
                            optimalMax = plant.optimalLightMax?.toFloat()?.div(100f)
                        )
                    }

                    d.batteryLevel?.let { battery ->
                        val batteryColor = when {
                            battery < 20 -> MaterialTheme.colorScheme.error
                            battery < 50 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.primary
                        }
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🔋 Заряд батареї", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "${battery.toInt()}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = batteryColor
                                )
                            }
                        }
                    }

                    Text(
                        "Останнє оновлення: ${d.timestamp.take(16).replace("T", " ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.loadLatest(plant.plantId) },
                            modifier = Modifier.weight(1f)
                        ) { Text("🔄 Оновити") }

                        if (plant.deviceId != null) {
                            OutlinedButton(
                                onClick = { viewModel.testNotifications(plant.plantId, plant.deviceId) },
                                modifier = Modifier.weight(1f),
                                enabled = !isTesting
                            ) {
                                if (isTesting) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("🧪 Тест пуш")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SensorCard(label: String, value: String, progress: Float, optimalMin: Float?, optimalMax: Float?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, style = MaterialTheme.typography.bodyLarge)
                Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = when {
                                optimalMin != null && progress < optimalMin -> MaterialTheme.colorScheme.error
                                optimalMax != null && progress > optimalMax -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.primary
                            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
            if (optimalMin != null && optimalMax != null) {
                Text(
                    "Оптимально: ${(optimalMin * 100).toInt()}–${(optimalMax * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
