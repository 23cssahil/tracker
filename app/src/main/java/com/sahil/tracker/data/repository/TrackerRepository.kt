package com.sahil.tracker.data.repository

import com.sahil.tracker.data.database.NoteDao
import com.sahil.tracker.data.database.TypingEventDao
import com.sahil.tracker.data.models.Note
import com.sahil.tracker.data.models.TypingEvent
import kotlinx.coroutines.flow.Flow

class TrackerRepository(
    private val typingEventDao: TypingEventDao,
    private val noteDao: NoteDao
) {
    // ---- Typing Events ----
    suspend fun insertTypingEvent(event: TypingEvent) = typingEventDao.insert(event)

    fun getDailyWordCount(date: String): Flow<Int?> = typingEventDao.getDailyWordCount(date)
    fun getDailyCharCount(date: String): Flow<Int?> = typingEventDao.getDailyCharCount(date)
    fun getAppWiseSummary(date: String): Flow<List<TypingEvent>> = typingEventDao.getAppWiseSummary(date)
    fun getAllTimeAppSummary(): Flow<List<TypingEvent>> = typingEventDao.getAllTimeAppSummary()
    fun getHourlyActivity(date: String): Flow<List<TypingEvent>> = typingEventDao.getHourlyActivity(date)
    fun getRecentEvents(date: String): Flow<List<TypingEvent>> = typingEventDao.getRecentEvents(date)
    fun getEventsForApp(pkg: String): Flow<List<TypingEvent>> = typingEventDao.getEventsForApp(pkg)
    fun getDailyHistory(): Flow<List<TypingEvent>> = typingEventDao.getDailyHistory()
    suspend fun deleteOldEvents(cutoffMillis: Long) = typingEventDao.deleteOldEvents(cutoffMillis)

    // ---- Notes ----
    suspend fun insertNote(note: Note): Long = noteDao.insert(note)
    suspend fun updateNote(note: Note) = noteDao.update(note)
    suspend fun deleteNote(note: Note) = noteDao.delete(note)
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()
    suspend fun getNoteById(id: Long): Note? = noteDao.getNoteById(id)
    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)
}
