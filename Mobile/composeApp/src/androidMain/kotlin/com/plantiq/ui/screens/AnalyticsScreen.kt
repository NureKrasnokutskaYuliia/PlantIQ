package com.plantiq.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plantiq.data.model.SensorDataResponseDto
import com.plantiq.viewmodel.AnalyticsState
import com.plantiq.viewmodel.AnalyticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    plantName: String,
    plantId: Int,
    onNavigateBack: () -> Unit,
    viewModel: AnalyticsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(plantId) {
        viewModel.loadHistory(plantId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Аналітика: $plantName") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            when (val s = state) {
                is AnalyticsState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                is AnalyticsState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                is AnalyticsState.Success -> {
                    if (s.history.isEmpty()) {
                        Text("Немає даних для побудови графіків", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    } else {
                        AnalyticsCard(
                            title = "💧 Вологість ґрунту (%)",
                            data = s.history,
                            getValue = { it.soilMoisture?.toFloat() ?: 0f },
                            color = Color(0xFF2196F3)
                        )

                        AnalyticsCard(
                            title = "☀️ Інтенсивність світла (%)",
                            data = s.history,
                            getValue = { it.lightIntensity?.toFloat() ?: 0f },
                            color = Color(0xFFFFC107)
                        )
                        
                        // Data Table
                        Text("Останні вимірювання", style = MaterialTheme.typography.titleMedium)
                        Card {
                            Column(modifier = Modifier.padding(8.dp)) {
                                s.history.reversed().take(10).forEach { entry ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(entry.timestamp.take(16).replace("T", " "), style = MaterialTheme.typography.bodySmall)
                                        Text("${entry.soilMoisture?.toInt() ?: 0}% | ${entry.lightIntensity?.toInt() ?: 0}%", style = MaterialTheme.typography.labelSmall)
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
fun AnalyticsCard(title: String, data: List<SensorDataResponseDto>, getValue: (SensorDataResponseDto) -> Float, color: Color) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(16.dp))
            LineChart(
                data = data.map(getValue),
                modifier = Modifier.fillMaxWidth().height(150.dp),
                lineColor = color
            )
        }
    }
}

@Composable
fun LineChart(data: List<Float>, modifier: Modifier, lineColor: Color) {
    if (data.isEmpty()) return

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val maxVal = 100f // Since it's %
        val spaceX = width / (data.size - 1).coerceAtLeast(1)

        val path = Path()
        data.forEachIndexed { index, value ->
            val x = index * spaceX
            val y = height - (value / maxVal * height)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx())
        )
        
        // Grid lines (optional but good)
        drawLine(Color.LightGray.copy(alpha = 0.5f), Offset(0f, 0f), Offset(width, 0f))
        drawLine(Color.LightGray.copy(alpha = 0.5f), Offset(0f, height/2), Offset(width, height/2))
        drawLine(Color.LightGray.copy(alpha = 0.5f), Offset(0f, height), Offset(width, height))
    }
}
