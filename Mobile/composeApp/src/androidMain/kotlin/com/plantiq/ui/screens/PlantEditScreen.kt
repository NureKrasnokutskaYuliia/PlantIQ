package com.plantiq.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plantiq.data.local.TokenManager
import com.plantiq.data.model.PlantResponseDto
import com.plantiq.viewmodel.PlantEditState
import com.plantiq.viewmodel.PlantEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantEditScreen(
    existingPlant: PlantResponseDto? = null,
    onSaved: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PlantEditViewModel = viewModel()
) {
    val isEdit = existingPlant != null
    val title = if (isEdit) "Редагувати рослину" else "Нова рослина"

    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val currentUserId by tokenManager.userIdFlow.collectAsState(initial = null)

    var name by remember { mutableStateOf(existingPlant?.name ?: "") }
    var species by remember { mutableStateOf(existingPlant?.species ?: "") }
    var notes by remember { mutableStateOf(existingPlant?.notes ?: "") }
    var selectedDeviceId by remember { mutableStateOf<Int?>(existingPlant?.deviceId) }
    var deviceDropdownExpanded by remember { mutableStateOf(false) }

    // Thresholds
    var moistureMin by remember { mutableStateOf(existingPlant?.optimalMoistureMin?.toInt()?.toString() ?: "") }
    var moistureMax by remember { mutableStateOf(existingPlant?.optimalMoistureMax?.toInt()?.toString() ?: "") }
    var lightMin by remember { mutableStateOf(existingPlant?.optimalLightMin?.toInt()?.toString() ?: "") }
    var lightMax by remember { mutableStateOf(existingPlant?.optimalLightMax?.toInt()?.toString() ?: "") }
    var wateringMode by remember { mutableStateOf(existingPlant?.wateringMode ?: "Manual") }

    val editState by viewModel.editState.collectAsState()
    val availableDevices by viewModel.availableDevices.collectAsState()
    val isLoadingDevices by viewModel.isLoadingDevices.collectAsState()
    val speciesList by viewModel.speciesList.collectAsState()
    var speciesDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(currentUserId) {
        val uid = currentUserId ?: return@LaunchedEffect
        viewModel.loadAvailableDevices(currentUserId = uid, currentPlantId = existingPlant?.plantId)
    }

    LaunchedEffect(editState) {
        if (editState is PlantEditState.Saved) {
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Назва рослини *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = name.isBlank() && editState is PlantEditState.Error
            )

            // Searchable Species Dropdown
            ExposedDropdownMenuBox(
                expanded = speciesDropdownExpanded,
                onExpandedChange = { speciesDropdownExpanded = !speciesDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = species,
                    onValueChange = { species = it },
                    label = { Text("Вид (виберіть зі списку або введіть свій)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = speciesDropdownExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                
                // Filtered list
                val filteredSpecies = speciesList.filter { 
                    it.name.contains(species, ignoreCase = true) 
                }

                if (filteredSpecies.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = speciesDropdownExpanded,
                        onDismissRequest = { speciesDropdownExpanded = false }
                    ) {
                        filteredSpecies.forEach { item ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text(item.name)
                                        Text(
                                            "Оптимально: ${item.defaultMoistureMin?.toInt()}% волог., ${item.defaultLightMin?.toInt()}% світла",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    species = item.name
                                    moistureMin = item.defaultMoistureMin?.toInt()?.toString() ?: ""
                                    moistureMax = item.defaultMoistureMax?.toInt()?.toString() ?: ""
                                    lightMin = item.defaultLightMin?.toInt()?.toString() ?: ""
                                    lightMax = item.defaultLightMax?.toInt()?.toString() ?: ""
                                    speciesDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Нотатки (необов'язково)") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            // Thresholds Section
            Text(
                text = "Оптимальні умови (пороги сповіщень)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = moistureMin,
                    onValueChange = { moistureMin = it },
                    label = { Text("Мін. % волог.") },
                    modifier = Modifier.weight(1f),
                    prefix = { Text("% ") }
                )
                OutlinedTextField(
                    value = moistureMax,
                    onValueChange = { moistureMax = it },
                    label = { Text("Макс. % волог.") },
                    modifier = Modifier.weight(1f),
                    prefix = { Text("% ") }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = lightMin,
                    onValueChange = { lightMin = it },
                    label = { Text("Мін. % освіт.") },
                    modifier = Modifier.weight(1f),
                    prefix = { Text("% ") }
                )
                OutlinedTextField(
                    value = lightMax,
                    onValueChange = { lightMax = it },
                    label = { Text("Макс. % освіт.") },
                    modifier = Modifier.weight(1f),
                    prefix = { Text("% ") }
                )
            }

            // Watering Mode Section
            Text(
                text = "Режим поливу",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val modes = listOf("Automatic", "Manual", "Scheduled")
                val modesUk = listOf("Автоматично", "Вручну", "За розкладом")
                modes.forEachIndexed { index, mode ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = (wateringMode == mode),
                            onClick = { wateringMode = mode }
                        )
                        Text(text = modesUk[index], style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Text(
                text = "Підв'язати пристрій",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (isLoadingDevices) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text("Завантаження пристроїв...", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                val currentDeviceName = availableDevices
                    .find { it.deviceId == selectedDeviceId }?.name
                    ?: if (selectedDeviceId != null) "ID: $selectedDeviceId (зайнятий)" else "Без пристрою"

                ExposedDropdownMenuBox(
                    expanded = deviceDropdownExpanded,
                    onExpandedChange = { deviceDropdownExpanded = !deviceDropdownExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = currentDeviceName,
                        onValueChange = {},
                        label = { Text("Пристрій") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = deviceDropdownExpanded,
                        onDismissRequest = { deviceDropdownExpanded = false }
                    ) {
                        // "No device" option
                        DropdownMenuItem(
                            text = { Text("Без пристрою") },
                            onClick = {
                                selectedDeviceId = null
                                deviceDropdownExpanded = false
                            }
                        )
                        for (device in availableDevices) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(device.name, style = MaterialTheme.typography.bodyLarge)
                                        if (!device.model.isNullOrBlank()) {
                                            Text(
                                                device.model,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedDeviceId = device.deviceId
                                    deviceDropdownExpanded = false
                                }
                            )
                        }
                        if (availableDevices.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Вільних пристроїв немає",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = { deviceDropdownExpanded = false },
                                enabled = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (editState is PlantEditState.Error) {
                Text(
                    text = (editState as PlantEditState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (editState is PlantEditState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        val mMin = moistureMin.toDoubleOrNull()
                        val mMax = moistureMax.toDoubleOrNull()
                        val lMin = lightMin.toDoubleOrNull()
                        val lMax = lightMax.toDoubleOrNull()

                        if (isEdit && existingPlant != null) {
                            viewModel.updatePlant(
                                plantId = existingPlant.plantId,
                                name = name,
                                species = species,
                                notes = notes,
                                deviceId = selectedDeviceId,
                                isActive = existingPlant.isActive,
                                moistureMin = mMin,
                                moistureMax = mMax,
                                lightMin = lMin,
                                lightMax = lMax,
                                wateringMode = wateringMode
                            )
                        } else {
                            viewModel.createPlant(
                                name = name, 
                                species = species, 
                                notes = notes, 
                                deviceId = selectedDeviceId,
                                moistureMin = mMin,
                                moistureMax = mMax,
                                lightMin = lMin,
                                lightMax = lMax,
                                wateringMode = wateringMode
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (isEdit) "Зберегти зміни" else "Додати рослину")
                }
            }
        }
    }
}
