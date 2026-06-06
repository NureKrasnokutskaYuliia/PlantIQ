package com.plantiq.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plantiq.viewmodel.WateringState
import com.plantiq.viewmodel.WateringViewModel
import com.plantiq.data.model.WateringScheduleResponseDto
import com.plantiq.data.model.UpdateWateringScheduleDto
import com.plantiq.data.model.CreateWateringScheduleDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WateringScreen(
    plantName: String,
    plantId: Int,
    onNavigateBack: () -> Unit,
    viewModel: WateringViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(plantId) {
        viewModel.loadData(plantId)
    }

    var showDialog by remember { mutableStateOf(false) }
    var editingSchedule by remember { mutableStateOf<WateringScheduleResponseDto?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Watering: $plantName") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Manual Trigger", style = MaterialTheme.typography.titleMedium)
                        Text("Water now", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = { viewModel.waterNow(plantId) }) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Water")
                    }
                }
            }

            Text("Watering Schedules", style = MaterialTheme.typography.titleMedium)

            when (val s = state) {
                is WateringState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                is WateringState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                is WateringState.Success -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(s.schedules) { schedule ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    editingSchedule = schedule
                                    showDialog = true
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("${schedule.startTime} | ${schedule.amountMl} ml", style = MaterialTheme.typography.bodyLarge)
                                        val daysStr = if (schedule.daysOfWeek.size == 7) "Every day"
                                                     else schedule.daysOfWeek.sorted().joinToString(", ") { dayNumToName(it) }
                                        Text("$daysStr | ${schedule.repeatCount} time(s)", style = MaterialTheme.typography.bodySmall)
                                        Text("Every ${schedule.intervalHours} h", style = MaterialTheme.typography.bodySmall)
                                    }
                                    IconButton(onClick = { viewModel.deleteSchedule(plantId, schedule.scheduleId) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }

                        if (s.schedules.isEmpty()) {
                            item { Text("No active schedules", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
                        }

                        item { Spacer(Modifier.height(16.dp)) }
                        item { Text("Watering History", style = MaterialTheme.typography.titleMedium) }

                        items(s.history) { event ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(event.timestamp.take(16).replace("T", " "), style = MaterialTheme.typography.labelSmall)
                                    Text(if (event.mode == "Auto") "Automatic" else "Manual", style = MaterialTheme.typography.bodySmall)
                                }
                                Text("${event.amountMl} ml", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
            Button(
                onClick = { 
                    editingSchedule = null
                    showDialog = true 
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Add Schedule")
            }
        }
    }

    if (showDialog) {
        WateringScheduleDialog(
            schedule = editingSchedule,
            onDismiss = { showDialog = false },
            onConfirm = { startTime, interval, amount, days, repeats ->
                if (editingSchedule != null) {
                    viewModel.updateSchedule(
                        plantId,
                        editingSchedule!!.scheduleId,
                        UpdateWateringScheduleDto(
                            startTime = startTime,
                            intervalHours = interval,
                            amountMl = amount,
                            enabled = true,
                            daysOfWeek = days,
                            repeatCount = repeats
                        )
                    )
                } else {
                    viewModel.addSchedule(
                        CreateWateringScheduleDto(
                            plantId = plantId,
                            startTime = startTime,
                            intervalHours = interval,
                            amountMl = amount,
                            enabled = true,
                            daysOfWeek = days,
                            repeatCount = repeats
                        )
                    )
                }
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WateringScheduleDialog(
    schedule: WateringScheduleResponseDto?,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Int, List<Int>, Int) -> Unit
) {
    var startTime by remember { mutableStateOf(schedule?.startTime ?: "08:00:00") }
    var intervalStr by remember { mutableStateOf(schedule?.intervalHours?.toString() ?: "24") }
    var amountStr by remember { mutableStateOf(schedule?.amountMl?.toString() ?: "200") }
    var repeatCountStr by remember { mutableStateOf(schedule?.repeatCount?.toString() ?: "1") }
    var selectedDays by remember { mutableStateOf(schedule?.daysOfWeek?.toSet() ?: setOf(1, 2, 3, 4, 5, 6, 7)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (schedule != null) "Edit Schedule" else "New Schedule") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Start time (HH:MM:SS)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = intervalStr,
                        onValueChange = { intervalStr = it },
                        label = { Text("Interval (h)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = repeatCountStr,
                        onValueChange = { repeatCountStr = it },
                        label = { Text("Repeats") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount (ml)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("Days of week", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (1..7).forEach { dayNum ->
                        FilterChip(
                            selected = selectedDays.contains(dayNum),
                            onClick = {
                                selectedDays = if (selectedDays.contains(dayNum)) {
                                    selectedDays - dayNum
                                } else {
                                    selectedDays + dayNum
                                }
                            },
                            label = { Text(dayNumToName(dayNum)) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val interval = intervalStr.toIntOrNull() ?: 24
                val amount = amountStr.toIntOrNull() ?: 200
                val repeats = repeatCountStr.toIntOrNull() ?: 1
                onConfirm(startTime, interval, amount, selectedDays.toList(), repeats)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun dayNumToName(dayNum: Int): String {
    return when (dayNum) {
        1 -> "Mon"
        2 -> "Tue"
        3 -> "Wed"
        4 -> "Thu"
        5 -> "Fri"
        6 -> "Sat"
        7 -> "Sun"
        else -> ""
    }
}
