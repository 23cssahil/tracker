package com.sahil.tracker.ui.timelog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sahil.tracker.data.models.TypingEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TimeLogScreen(vm: TimeLogViewModel = viewModel()) {
    val hourlyData by vm.hourlyData.collectAsState()
    val recentEvents by vm.recentEvents.collectAsState()
    val maxWords = hourlyData.maxOfOrNull { it.wordCount } ?: 1

    // Build a full 24-hour map
    val hourMap = hourlyData.associate { it.hour to it.wordCount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Time Activity",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = vm.todayDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 24-hour heatmap
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "24-Hour Heatmap",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // First row: 0-11
                    HourHeatmapRow(range = 0..11, hourMap = hourMap, maxWords = maxWords)
                    Spacer(modifier = Modifier.height(8.dp))
                    // Second row: 12-23
                    HourHeatmapRow(range = 12..23, hourMap = hourMap, maxWords = maxWords)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("12am", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("6am", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("12pm", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("6pm", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("12am", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Hourly bar chart
        if (hourlyData.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Hourly Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            (0..23).forEach { hour ->
                                val count = hourMap[hour] ?: 0
                                val barH = if (maxWords > 0) ((count.toFloat() / maxWords) * 100).dp else 4.dp
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .width(8.dp)
                                            .height(barH.coerceAtLeast(4.dp))
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(
                                                if (count > 0) Color(0xFF6C63FF)
                                                else MaterialTheme.colorScheme.surface
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent events log
        if (recentEvents.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Events",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            items(recentEvents.take(30)) { event ->
                RecentEventRow(event = event)
            }
        }
    }
}

@Composable
fun HourHeatmapRow(range: IntRange, hourMap: Map<Int, Int>, maxWords: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        range.forEach { hour ->
            val count = hourMap[hour] ?: 0
            val intensity = if (maxWords > 0) count.toFloat() / maxWords else 0f
            val color = Color(0xFF6C63FF).copy(alpha = (0.1f + intensity * 0.9f).coerceIn(0.05f, 1f))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (count > 0) color else MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                if (count > 0) {
                    Text(
                        text = "${hour % 12}",
                        fontSize = 7.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun RecentEventRow(event: TypingEvent) {
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(event.timestamp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = event.appName, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                Text(text = timeStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = "+${event.wordCount} words",
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6C63FF),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
