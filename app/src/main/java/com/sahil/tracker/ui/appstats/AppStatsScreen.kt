package com.sahil.tracker.ui.appstats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sahil.tracker.data.models.TypingEvent

val appColors = listOf(
    Color(0xFF6C63FF), Color(0xFF00BFA5), Color(0xFFFF6D00),
    Color(0xFF2979FF), Color(0xFFE91E63), Color(0xFF8BC34A),
    Color(0xFFFF5722), Color(0xFF9C27B0), Color(0xFF03A9F4)
)

@Composable
fun AppStatsScreen(vm: AppStatsViewModel = viewModel()) {
    val stats by vm.appStats.collectAsState()
    val isAllTime by vm.isAllTime.collectAsState()
    val totalWords = stats.sumOf { it.wordCount }.coerceAtLeast(1)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "App Activity",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Toggle today / all time
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !isAllTime,
                    onClick = { vm.loadTodayStats() },
                    label = { Text("Today") }
                )
                FilterChip(
                    selected = isAllTime,
                    onClick = { vm.loadAllTimeStats() },
                    label = { Text("All Time") }
                )
            }
        }

        if (stats.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No data yet.\nStart typing to see app stats!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Visual pie-like legend
            item {
                AppDonutLegend(stats = stats, totalWords = totalWords)
            }

            // List
            itemsIndexed(stats) { index, app ->
                AppStatRow(
                    app = app,
                    totalWords = totalWords,
                    color = appColors[index % appColors.size],
                    rank = index + 1
                )
            }
        }
    }
}

@Composable
fun AppDonutLegend(stats: List<TypingEvent>, totalWords: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "$totalWords total words",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            stats.take(5).forEachIndexed { i, app ->
                val fraction = app.wordCount.toFloat() / totalWords
                val pct = (fraction * 100).toInt()
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(appColors[i % appColors.size]))
                    Text(text = app.appName, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(text = "$pct%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun AppStatRow(app: TypingEvent, totalWords: Int, color: Color, rank: Int) {
    val fraction = app.wordCount.toFloat() / totalWords
    val animFraction by animateFloatAsState(targetValue = fraction, animationSpec = tween(800), label = "bar$rank")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "#$rank", fontWeight = FontWeight.Bold, color = color, style = MaterialTheme.typography.labelMedium)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = app.appName, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = app.appPackage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { animFraction },
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                    color = color,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "${app.wordCount}", fontWeight = FontWeight.Bold, color = color)
                Text(text = "words", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
