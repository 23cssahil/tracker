package com.sahil.tracker.data.database

import androidx.room.*
import com.sahil.tracker.data.models.TypingEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface TypingEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: TypingEvent)

    @Query("SELECT SUM(wordCount) FROM typing_events WHERE dateString = :date")
    fun getDailyWordCount(date: String): Flow<Int?>

    @Query("SELECT SUM(charCount) FROM typing_events WHERE dateString = :date")
    fun getDailyCharCount(date: String): Flow<Int?>

    @Query("""
        SELECT appPackage, appName,
               SUM(wordCount) as wordCount,
               SUM(charCount) as charCount,
               0 as id, 0 as timestamp, 0 as hour, '' as dateString, '' as typedText
        FROM typing_events
        WHERE dateString = :date
        GROUP BY appPackage
        ORDER BY wordCount DESC
    """)
    fun getAppWiseSummary(date: String): Flow<List<TypingEvent>>

    @Query("""
        SELECT appPackage, appName,
               SUM(wordCount) as wordCount,
               SUM(charCount) as charCount,
               0 as id, 0 as timestamp, 0 as hour, '' as dateString, '' as typedText
        FROM typing_events
        GROUP BY appPackage
        ORDER BY wordCount DESC
    """)
    fun getAllTimeAppSummary(): Flow<List<TypingEvent>>

    @Query("""
        SELECT hour,
               SUM(wordCount) as wordCount,
               SUM(charCount) as charCount,
               0 as id, '' as appPackage, '' as appName, 0 as timestamp, '' as dateString, '' as typedText
        FROM typing_events
        WHERE dateString = :date
        GROUP BY hour
        ORDER BY hour ASC
    """)
    fun getHourlyActivity(date: String): Flow<List<TypingEvent>>

    @Query("SELECT * FROM typing_events WHERE dateString = :date ORDER BY timestamp DESC LIMIT 200")
    fun getRecentEvents(date: String): Flow<List<TypingEvent>>

    @Query("""
        SELECT dateString,
               SUM(wordCount) as wordCount,
               SUM(charCount) as charCount,
               0 as id, '' as appPackage, '' as appName, 0 as timestamp, 0 as hour, '' as typedText
        FROM typing_events
        GROUP BY dateString
        ORDER BY dateString DESC
        LIMIT 30
    """)
    fun getDailyHistory(): Flow<List<TypingEvent>>

    @Query("DELETE FROM typing_events WHERE timestamp < :cutoffMillis")
    suspend fun deleteOldEvents(cutoffMillis: Long)
}
