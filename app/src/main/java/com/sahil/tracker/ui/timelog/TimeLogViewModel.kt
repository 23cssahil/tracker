package com.sahil.tracker.ui.timelog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sahil.tracker.data.database.TrackerDatabase
import com.sahil.tracker.data.models.TypingEvent
import com.sahil.tracker.data.repository.TrackerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimeLogViewModel(app: Application) : AndroidViewModel(app) {
    private val repository: TrackerRepository
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _hourlyData = MutableStateFlow<List<TypingEvent>>(emptyList())
    val hourlyData: StateFlow<List<TypingEvent>> = _hourlyData.asStateFlow()

    private val _recentEvents = MutableStateFlow<List<TypingEvent>>(emptyList())
    val recentEvents: StateFlow<List<TypingEvent>> = _recentEvents.asStateFlow()

    val todayDate: String = dateFormat.format(Date())

    init {
        val db = TrackerDatabase.getDatabase(app)
        repository = TrackerRepository(db.typingEventDao(), db.noteDao())
        viewModelScope.launch {
            repository.getHourlyActivity(todayDate).collect {
                _hourlyData.value = it
            }
        }
        viewModelScope.launch {
            repository.getRecentEvents(todayDate).collect {
                _recentEvents.value = it
            }
        }
    }
}
