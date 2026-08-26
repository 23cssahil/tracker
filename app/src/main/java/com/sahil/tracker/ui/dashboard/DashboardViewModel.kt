package com.sahil.tracker.ui.dashboard

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

class DashboardViewModel(app: Application) : AndroidViewModel(app) {

    private val repository: TrackerRepository
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDate: String = dateFormat.format(Date())

    private val _dailyWords = MutableStateFlow(0)
    val dailyWords: StateFlow<Int> = _dailyWords.asStateFlow()

    private val _dailyChars = MutableStateFlow(0)
    val dailyChars: StateFlow<Int> = _dailyChars.asStateFlow()

    private val _history = MutableStateFlow<List<TypingEvent>>(emptyList())
    val history: StateFlow<List<TypingEvent>> = _history.asStateFlow()

    private val _topApps = MutableStateFlow<List<TypingEvent>>(emptyList())
    val topApps: StateFlow<List<TypingEvent>> = _topApps.asStateFlow()

    init {
        val db = TrackerDatabase.getDatabase(app)
        repository = TrackerRepository(db.typingEventDao(), db.noteDao())
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            repository.getDailyWordCount(todayDate).collect { count ->
                _dailyWords.value = count ?: 0
            }
        }
        viewModelScope.launch {
            repository.getDailyCharCount(todayDate).collect { count ->
                _dailyChars.value = count ?: 0
            }
        }
        viewModelScope.launch {
            repository.getDailyHistory().collect { list ->
                _history.value = list
            }
        }
        viewModelScope.launch {
            repository.getAppWiseSummary(todayDate).collect { list ->
                _topApps.value = list.take(3)
            }
        }
    }
}
