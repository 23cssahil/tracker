package com.sahil.tracker.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sahil.tracker.data.models.TypingEvent

@Composable
fun DashboardScreen(vm: DashboardViewModel = viewModel()) {
    val words by vm.dailyWords.collectAsState()
    val chars by vm.dailyChars.collectAsState()
    val history by vm.history.collectAsState()
    val topApps by vm.topApps.collectAsState()

    val animatedWords by animateFloatAsState(
        targetValue = words.toFloat(),
        animationSpec = tween(1000),
        label = "words"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Today's Activity",
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

        // Hero card - words typed today
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF6C63FF),
                                Color(0xFF9C59F5)
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = animatedWords.toInt().toString(),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "words typed today",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "$chars characters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Stats row
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Avg/Hour",
                    value = if (history.isNotEmpty()) "${words / 16}" else "0",
                    color = Color(0xFF00BFA5)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Streak",
                    value = "${history.size} days",
                    color = Color(0xFFFF6D00)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Apps",
                    value = "${topApps.size}+",
                    color = Color(0xFF2979FF)
                )
            }
        }

        // Top apps today
        if (topApps.isNotEmpty()) {
            item {
                Text(
                    text = "Top Apps Today",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            items(topApps) { app ->
                AppSummaryRow(app = app, totalWords = words)
            }
        }

        // 30-day history chart
        if (history.isNotEmpty()) {
            item {
                Text(
                    text = "30-Day History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                HistoryBarChart(history = history)
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = color)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AppSummaryRow(app: TypingEvent, totalWords: Int) {
    val fraction = if (totalWords > 0) app.wordCount.toFloat() / totalWords else 0f
    val animFraction by animateFloatAsState(targetValue = fraction, animationSpec = tween(800), label = "bar")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = app.appName, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = "${app.wordCount} words", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { animFraction },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF6C63FF),
                trackColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
fun HistoryBarChart(history: List<TypingEvent>) {
    val maxWords = history.maxOfOrNull { it.wordCount } ?: 1
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .height(80.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            history.takeLast(14).forEach { event ->
                val h = ((event.wordCount.toFloat() / maxWords) * 80).dp
                val isToday = event.dateString == history.firstOrNull()?.dateString
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height(h.coerceAtLeast(4.dp))
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            if (isToday) Color(0xFF6C63FF)
                            else Color(0xFF6C63FF).copy(alpha = 0.35f)
                        )
                )
            }
        }
    }
}
