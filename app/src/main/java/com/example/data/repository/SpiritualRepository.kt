package com.example.data.repository

import com.example.data.model.Ayah
import com.example.data.model.CuratedAyahs
import com.example.data.model.CuratedHadiths
import com.example.data.model.Hadith
import java.util.Calendar

class SpiritualRepository {

    fun getDailyAyah(calendar: Calendar = Calendar.getInstance()): Ayah {
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val index = dayOfYear % CuratedAyahs.size
        return CuratedAyahs[index]
    }

    fun getDailyHadith(calendar: Calendar = Calendar.getInstance()): Hadith {
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val index = (dayOfYear + 3) % CuratedHadiths.size
        return CuratedHadiths[index]
    }

    fun getAllAyahs(): List<Ayah> = CuratedAyahs

    fun getAllHadiths(): List<Hadith> = CuratedHadiths
}
