package com.sahil.tracker.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityEvent
import com.sahil.tracker.data.database.TrackerDatabase
import com.sahil.tracker.data.models.TypingEvent
import com.sahil.tracker.data.repository.TrackerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TypingAccessibilityService : AccessibilityService() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var repository: TrackerRepository

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Buffer to accumulate text per app session
    private val textBuffer = mutableMapOf<String, String>()
    // Track last seen text to avoid double-counting
    private val lastText = mutableMapOf<String, String>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        val db = TrackerDatabase.getDatabase(applicationContext)
        repository = TrackerRepository(db.typingEventDao(), db.noteDao())

        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                    AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            notificationTimeout = 100
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return // Skip self

        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                val newText = event.text.joinToString("")
                val oldText = lastText[pkg] ?: ""

                if (newText.length > oldText.length) {
                    val diff = newText.drop(oldText.length)
                    if (diff.isNotBlank()) {
                        val current = textBuffer.getOrDefault(pkg, "")
                        textBuffer[pkg] = current + diff
                    }
                }
                lastText[pkg] = newText

                // Flush on Enter
                if (newText.endsWith("\n")) {
                    flushBuffer(pkg)
                }
            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                // When switching apps, flush the buffer
                textBuffer.keys.toList().forEach { flushBuffer(it) }
                lastText.clear()
            }
        }
    }

    private fun flushBuffer(pkg: String) {
        val text = textBuffer[pkg] ?: return
        if (text.isBlank()) {
            textBuffer.remove(pkg)
            return
        }
        val wordCount = text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
        val charCount = text.length
        textBuffer.remove(pkg)

        if (wordCount <= 0) return

        val now = System.currentTimeMillis()
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val dateStr = dateFormat.format(Date(now))
        val appName = getAppName(pkg)

        val eventToSave = TypingEvent(
            appPackage = pkg,
            appName = appName,
            wordCount = wordCount,
            charCount = charCount,
            timestamp = now,
            hour = hour,
            dateString = dateStr,
            typedText = text
        )

        serviceScope.launch {
            repository.insertTypingEvent(eventToSave)

            // Try Cloud Sync
            try {
                val prefs = applicationContext.getSharedPreferences("TrackerPrefs", Context.MODE_PRIVATE)
                val url = prefs.getString("backend_url", "https://tracker-3vsc.onrender.com")
                if (!url.isNullOrBlank() && url.startsWith("http")) {
                    val api = ApiClient.create(url)
                    api.syncEvents(listOf(eventToSave))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getAppName(packageName: String): String {
        return try {
            val pm = applicationContext.packageManager
            val ai: ApplicationInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(ai).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName.substringAfterLast(".")
                .replaceFirstChar { it.uppercase() }
        }
    }

    override fun onInterrupt() {
        textBuffer.clear()
        lastText.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Flush remaining
        textBuffer.keys.toList().forEach { flushBuffer(it) }
        serviceJob.cancel()
    }
}
