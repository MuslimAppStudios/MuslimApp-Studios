package com.example.data.repository

import com.example.data.local.DailyHabitDao
import com.example.data.model.DailyHabit
import com.example.data.model.KazaCounter
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HabitRepository(private val dailyHabitDao: DailyHabitDao) {

    fun getTodayDateKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getTodayHabit(): Flow<DailyHabit?> {
        return dailyHabitDao.getHabitForDate(getTodayDateKey())
    }

    suspend fun saveHabit(habit: DailyHabit) {
        dailyHabitDao.upsertHabit(habit)
    }

    fun getKazaCounter(): Flow<KazaCounter?> {
        return dailyHabitDao.getKazaCounter()
    }

    suspend fun saveKazaCounter(counter: KazaCounter) {
        dailyHabitDao.upsertKazaCounter(counter)
    }
}
