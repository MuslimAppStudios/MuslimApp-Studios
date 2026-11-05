package com.example

import com.example.data.model.CuratedAyahs
import com.example.data.model.CuratedHadiths
import com.example.data.model.DefaultCities
import com.example.data.model.PrayerType
import com.example.data.model.Turkey81Provinces
import com.example.data.model.WeatherCodeMapper
import com.example.data.util.PrayerTimeCalculator
import com.example.ui.util.AlertSound
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import kotlin.math.roundToInt

class ExampleUnitTest {

    @Test
    fun test81ProvincesExist() {
        assertEquals(81, Turkey81Provinces.size)
        // Check first and last plate
        assertEquals("01", Turkey81Provinces.first().plateCode)
        assertEquals("Adana", Turkey81Provinces.first().name)
        assertEquals("81", Turkey81Provinces.last().plateCode)
        assertEquals("Düzce", Turkey81Provinces.last().name)
    }

    @Test
    fun testPrayerCalculationForIstanbul() {
        val istanbul = DefaultCities.first { it.name == "İstanbul" }
        val prayerTimes = PrayerTimeCalculator.calculate(istanbul)

        assertNotNull(prayerTimes)
        assertEquals(6, prayerTimes.prayers.size)
        assertTrue(prayerTimes.qiblaAngle > 140.0 && prayerTimes.qiblaAngle < 165.0)
        assertTrue(prayerTimes.distanceToKaabaKm > 2000.0)
    }

    @Test
    fun testLivePrayerProgressionSequence() {
        val istanbul = DefaultCities.first { it.name == "İstanbul" }
        val baseTimes = PrayerTimeCalculator.calculate(istanbul)

        val imsak = baseTimes.prayers.first { it.type == PrayerType.IMSAK }
        val gunes = baseTimes.prayers.first { it.type == PrayerType.GUNES }
        val ogle = baseTimes.prayers.first { it.type == PrayerType.OGLE }

        // Test mid-time between imsak and gunes: should be waiting for Güneş
        val midImsakGunesMins = (imsak.hour * 60 + imsak.minute + gunes.hour * 60 + gunes.minute) / 2
        val calMorning = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, midImsakGunesMins / 60)
            set(Calendar.MINUTE, midImsakGunesMins % 60)
            set(Calendar.SECOND, 0)
        }
        val ptMorning = PrayerTimeCalculator.calculate(istanbul, calMorning)
        assertEquals(PrayerType.GUNES, ptMorning.nextPrayerInfo?.prayer?.type)

        // Test mid-time between gunes and ogle: should be waiting for Öğle
        val midGunesOgleMins = (gunes.hour * 60 + gunes.minute + ogle.hour * 60 + ogle.minute) / 2
        val calKusluk = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, midGunesOgleMins / 60)
            set(Calendar.MINUTE, midGunesOgleMins % 60)
            set(Calendar.SECOND, 0)
        }
        val ptKusluk = PrayerTimeCalculator.calculate(istanbul, calKusluk)
        assertEquals(PrayerType.OGLE, ptKusluk.nextPrayerInfo?.prayer?.type)

        // Test night after Yatsı (23:30): should be waiting for tomorrow's İmsak
        val calNight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
        }
        val ptNight = PrayerTimeCalculator.calculate(istanbul, calNight)
        assertEquals(PrayerType.IMSAK, ptNight.nextPrayerInfo?.prayer?.type)
        assertTrue(ptNight.nextPrayerInfo?.totalRemainingSeconds ?: 0L > 0)
    }

    @Test
    fun testOnlyEzanAndBipAlertSounds() {
        // Must contain only Ezan makams, Bip Bip, and Titreşim (no Ney, no Kuş, no Çan)
        val sounds = AlertSound.entries
        assertEquals(7, sounds.size)
        assertTrue(sounds.contains(AlertSound.EZAN_HICAZ))
        assertTrue(sounds.contains(AlertSound.EZAN_SABA))
        assertTrue(sounds.contains(AlertSound.EZAN_RAST))
        assertTrue(sounds.contains(AlertSound.EZAN_SEGAH))
        assertTrue(sounds.contains(AlertSound.EZAN_USSAK))
        assertTrue(sounds.contains(AlertSound.BIP_BIP))
        assertTrue(sounds.contains(AlertSound.TITRESIM))
    }

    @Test
    fun testQiblaAngleCalculation() {
        val qiblaAngle = PrayerTimeCalculator.calculateQiblaAngle(41.0082, 28.9784)
        val rounded = qiblaAngle.roundToInt()
        assertEquals(152, rounded)
    }

    @Test
    fun testWeatherCodeMapper() {
        assertEquals("Açık / Güneşli", WeatherCodeMapper.getCondition(0))
        assertEquals("Parçalı Bulutlu", WeatherCodeMapper.getCondition(2))
        assertEquals("Yağmurlu", WeatherCodeMapper.getCondition(61))
    }

    @Test
    fun testCuratedSpiritualContentNotEmpty() {
        assertTrue(CuratedAyahs.isNotEmpty())
        assertTrue(CuratedHadiths.isNotEmpty())

        CuratedAyahs.forEach { ayah ->
            assertTrue(ayah.surahName.isNotBlank())
            assertTrue(ayah.arabicText.isNotBlank())
            assertTrue(ayah.turkishTranslation.isNotBlank())
        }

        CuratedHadiths.forEach { hadith ->
            assertTrue(hadith.text.isNotBlank())
            assertTrue(hadith.source.isNotBlank())
            assertTrue(hadith.narrator.isNotBlank())
        }
    }
}
