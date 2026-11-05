package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.DailyHabit
import com.example.data.model.KazaCounter
import com.example.data.model.Note

@Database(
    entities = [Note::class, DailyHabit::class, KazaCounter::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun dailyHabitDao(): DailyHabitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kiblegah_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
