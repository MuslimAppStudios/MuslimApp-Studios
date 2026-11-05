package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_habits")
data class DailyHabit(
    @PrimaryKey
    val dateKey: String, // e.g. "2026-11-05"
    val sabahNamaz: Boolean = false,
    val ogleNamaz: Boolean = false,
    val ikindiNamaz: Boolean = false,
    val aksamNamaz: Boolean = false,
    val yatsiNamaz: Boolean = false,
    val vitirNamaz: Boolean = false,
    val quranPagesRead: Int = 0,
    val totalDhikr: Int = 0,
    val personalReflection: String = ""
)

@Entity(tableName = "kaza_counter")
data class KazaCounter(
    @PrimaryKey
    val id: Int = 1,
    val sabah: Int = 0,
    val ogle: Int = 0,
    val ikindi: Int = 0,
    val aksam: Int = 0,
    val yatsi: Int = 0,
    val vitir: Int = 0,
    val oruc: Int = 0
)
