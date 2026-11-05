package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val category: String = "Genel",
    val createdAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val colorIndex: Int = 0
)

val NoteCategories = listOf(
    "Tümü",
    "Dua",
    "Ayet Notu",
    "Hadis Notu",
    "Vaaz & Sohbet",
    "Fıkıh",
    "Hatırlatıcı",
    "Şahsi Tefekkür"
)
