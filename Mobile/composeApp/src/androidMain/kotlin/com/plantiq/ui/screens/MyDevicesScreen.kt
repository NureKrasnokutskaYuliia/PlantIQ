package com.plantiq.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plantiq.data.model.TokenManager
import com.plantiq.data.api.ApiClient
import com.plantiq.data.model.DeviceResponseDto
import io.ktor.client.call.body
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

sealed class DevicesListState {
    object Loading : DevicesListState()
    data class Success(val devices: List<DeviceResponseDto>) : DevicesListState()
    data class Error(val message: String) : DevicesListState()
}

class DevicesListViewModel : ViewModel() {
    private val _state = MutableStateFlow<DevicesListState>(DevicesListState.Loading)
    val state: StateFlow<DevicesListState> = _state.asStateFlow()

    private var currentUserId: Int? = null

    fun load(userId: Int) {
        currentUserId = userId
        viewModelScope.launch {
            _state.value = DevicesListState.Loading
            try {
                val response = ApiClient.api.getDevices()
                if (response.status.value in 200..299) {
                    val all = response.body<List<DeviceResponseDto>>()
                    _state.value = DevicesListState.Success(all.filter { it.userId == userId })
                } else {
                    _state.value = DevicesListState.Error("Не вдалося завантажити пристрої.")
                }
            } catch (e: Exception) {
                _state.value = DevicesListState.Error("Помилка з'єднання.")
            }
        }
    }

    fun delete(deviceId: Int) {
        viewModelScope.launch {
            try {
                ApiClient.api.deleteDevice(deviceId)
                // Refresh list
                currentUserId?.let { load(it) }
            } catch (_: Exception) {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDevicesScreen(
    onAddDevice: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: DevicesListViewModel = viewModel()
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val userId by tokenManager.userIdFlow.collectAsState(initial = null)

    LaunchedEffect(userId) {
        userId?.let { viewModel.load(it) }
    }

    val state by viewModel.state.collectAsState()
    var deviceToDelete by remember { mutableStateOf<DeviceResponseDto?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мої пристрої") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddDevice,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Додати пристрій", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is DevicesListState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is DevicesListState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center), textAlign = TextAlign.Center)
                is DevicesListState.Success -> {
                    if (s.devices.isEmpty()) {
                        Text(
                            "Зареєструйте свій перший пристрій",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(s.devices) { device ->
                                DeviceCard(device = device, onDelete = { deviceToDelete = device })
                            }
                        }
                    }
                }
            }
        }
    }

    deviceToDelete?.let { device ->
        AlertDialog(
            onDismissRequest = { deviceToDelete = null },
            title = { Text("Видалити пристрій") },
            text = { Text("Видалити «${device.name}»? Пов'язані дані рослини залишаться, але пристрій буде відключено.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.delete(device.deviceId)
                        deviceToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Видалити") }
            },
            dismissButton = {
                TextButton(onClick = { deviceToDelete = null }) { Text("Скасувати") }
            }
        )
    }
}

@Composable
fun DeviceCard(device: DeviceResponseDto, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.PhoneAndroid,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(device.name, style = MaterialTheme.typography.titleMedium)
                if (!device.model.isNullOrBlank()) {
                    Text("Модель: ${device.model}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (!device.serialNumber.isNullOrBlank()) {
                    Text("S/N: ${device.serialNumber}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    text = "Статус: ${device.status?.uppercase() ?: "UNKNOWN"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (device.status?.lowercase() == "active") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Видалити", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
