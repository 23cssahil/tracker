package com.sahil.tracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sahil.tracker.data.models.Note
import com.sahil.tracker.data.models.TypingEvent

@Database(
    entities = [TypingEvent::class, Note::class],
    version = 2,
    exportSchema = false
)
abstract class TrackerDatabase : RoomDatabase() {

    abstract fun typingEventDao(): TypingEventDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: TrackerDatabase? = null

        fun getDatabase(context: Context): TrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TrackerDatabase::class.java,
                    "tracker_database"
                ).fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
