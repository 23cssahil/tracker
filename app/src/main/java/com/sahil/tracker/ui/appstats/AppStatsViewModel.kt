package com.sahil.tracker.ui.appstats

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

class AppStatsViewModel(app: Application) : AndroidViewModel(app) {
    private val repository: TrackerRepository
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _appStats = MutableStateFlow<List<TypingEvent>>(emptyList())
    val appStats: StateFlow<List<TypingEvent>> = _appStats.asStateFlow()

    private val _isAllTime = MutableStateFlow(false)
    val isAllTime: StateFlow<Boolean> = _isAllTime.asStateFlow()

    val todayDate: String = dateFormat.format(Date())

    init {
        val db = TrackerDatabase.getDatabase(app)
        repository = TrackerRepository(db.typingEventDao(), db.noteDao())
        loadTodayStats()
    }

    fun loadTodayStats() {
        _isAllTime.value = false
        viewModelScope.launch {
            repository.getAppWiseSummary(todayDate).collect {
                _appStats.value = it
            }
        }
    }

    fun loadAllTimeStats() {
        _isAllTime.value = true
        viewModelScope.launch {
            repository.getAllTimeAppSummary().collect {
                _appStats.value = it
            }
        }
    }
}
