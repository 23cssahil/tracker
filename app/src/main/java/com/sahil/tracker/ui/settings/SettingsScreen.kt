package com.sahil.tracker.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sahil.tracker.service.TypingAccessibilityService

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var isServiceEnabled by remember { mutableStateOf(false) }

    // Check if accessibility service is enabled
    LaunchedEffect(Unit) {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""
        isServiceEnabled = enabledServices.contains(
            "${context.packageName}/${TypingAccessibilityService::class.java.name}"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Service status card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isServiceEnabled) Color(0xFF00BFA5).copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            if (isServiceEnabled) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isServiceEnabled) Color(0xFF00BFA5) else MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Typing Tracker Service",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isServiceEnabled) "Active — tracking your typing"
                        else "Inactive — tap to enable",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isServiceEnabled,
                    onCheckedChange = {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF6C63FF))
                )
            }
        }

        // Info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("How it works", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                InfoRow(emoji = "🔒", text = "All data stays on your phone — 100% private")
                InfoRow(emoji = "📊", text = "Counts words as you type in any app")
                InfoRow(emoji = "🔋", text = "Minimal battery impact (lightweight service)")
                InfoRow(emoji = "⚡", text = "Requires Accessibility permission to detect keystrokes")
            }
        }

        // How to enable
        if (!isServiceEnabled) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF6C63FF).copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Enable Steps", fontWeight = FontWeight.SemiBold, color = Color(0xFF6C63FF))
                    Text("1. Tap the switch above", style = MaterialTheme.typography.bodySmall)
                    Text("2. Find 'Typing Tracker' in Accessibility", style = MaterialTheme.typography.bodySmall)
                    Text("3. Toggle it ON", style = MaterialTheme.typography.bodySmall)
                    Text("4. Come back to the app", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Typing Tracker v1.0 • Personal Use Only",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun InfoRow(emoji: String, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
        Text(text = emoji, style = MaterialTheme.typography.bodyMedium)
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
