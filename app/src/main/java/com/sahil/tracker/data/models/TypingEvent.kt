package com.sahil.tracker.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "typing_events")
data class TypingEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appPackage: String,
    val appName: String,
    val wordCount: Int,
    val charCount: Int,
    val timestamp: Long,
    val hour: Int,
    val dateString: String
)
