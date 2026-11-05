package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DailyHabit
import com.example.data.model.KazaCounter
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyHabitDao {

    @Query("SELECT * FROM daily_habits WHERE dateKey = :dateKey LIMIT 1")
    fun getHabitForDate(dateKey: String): Flow<DailyHabit?>

    @Query("SELECT * FROM daily_habits ORDER BY dateKey DESC LIMIT :limit")
    fun getRecentHabits(limit: Int = 30): Flow<List<DailyHabit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHabit(habit: DailyHabit)

    @Query("SELECT * FROM kaza_counter WHERE id = 1 LIMIT 1")
    fun getKazaCounter(): Flow<KazaCounter?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertKazaCounter(counter: KazaCounter)
}
