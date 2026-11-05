package com.example.data.util

import com.example.data.model.City
import com.example.data.model.DailyPrayerTimes
import com.example.data.model.NextPrayerInfo
import com.example.data.model.PrayerTimeItem
import com.example.data.model.PrayerType
import java.util.Calendar
import java.util.Locale
import kotlin.math.*

object PrayerTimeCalculator {

    private const val KAABA_LAT = 21.422487
    private const val KAABA_LNG = 39.826206

    /**
     * Calculates astronomical prayer times according to standard Diyanet / MWL parameters.
     * Transitions seamlessly through the 6 daily phases:
     * İmsak -> Güneş -> Öğle -> İkindi -> Akşam -> Yatsı -> İmsak (ertesi gün)
     */
    fun calculate(city: City, calendar: Calendar = Calendar.getInstance()): DailyPrayerTimes {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hourNow = calendar.get(Calendar.HOUR_OF_DAY)
        val minNow = calendar.get(Calendar.MINUTE)
        val secNow = calendar.get(Calendar.SECOND)
        val currentSecondsOfDay = hourNow * 3600 + minNow * 60 + secNow

        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        // Solar coordinates
        val b = 2.0 * Math.PI * (dayOfYear - 81) / 365.0
        val eot = 9.87 * sin(2 * b) - 7.53 * cos(b) - 1.5 * sin(b) // Equation of time in minutes
        val declination = 23.45 * sin(Math.toRadians((360.0 / 365.0) * (dayOfYear - 81))) // in degrees

        val latRad = Math.toRadians(city.latitude)
        val decRad = Math.toRadians(declination)

        // Solar noon in hours (UTC + city.timeZoneOffsetHours)
        val noon = 12.0 + city.timeZoneOffsetHours - (city.longitude / 15.0) - (eot / 60.0)

        // Sunrise & Sunset angle (-0.833 degrees for refraction & sun disc)
        val sunAngle = -0.8333
        val h0 = hourAngle(sunAngle, latRad, decRad)
        val sunriseHours = noon - (h0 / 15.0)
        val sunsetHours = noon + (h0 / 15.0)

        // Fajr / Imsak (18.0 degrees below horizon)
        val fajrAngle = -18.0
        val hFajr = hourAngle(fajrAngle, latRad, decRad)
        val fajrHours = noon - (hFajr / 15.0)

        // Asr (Standard shadow length ratio = 1)
        val asrAltitude = Math.toDegrees(atan(1.0 / (1.0 + tan(abs(latRad - decRad)))))
        val hAsr = hourAngle(asrAltitude, latRad, decRad)
        val asrHours = noon + (hAsr / 15.0)

        // Isha / Yatsı (17.0 degrees below horizon)
        val ishaAngle = -17.0
        val hIsha = hourAngle(ishaAngle, latRad, decRad)
        val ishaHours = noon + (hIsha / 15.0)

        val timesMap = listOf(
            PrayerType.IMSAK to fajrHours,
            PrayerType.GUNES to sunriseHours,
            PrayerType.OGLE to noon,
            PrayerType.IKINDI to asrHours,
            PrayerType.AKSAM to sunsetHours,
            PrayerType.YATSI to ishaHours
        )

        val rawItems = mutableListOf<PrayerTimeItem>()
        for ((type, rawHour) in timesMap) {
            val clampedHour = (rawHour + 24.0) % 24.0
            val h = clampedHour.toInt()
            val m = ((clampedHour - h) * 60).toInt()
            rawItems.add(
                PrayerTimeItem(
                    type = type,
                    name = type.turkishName,
                    timeString = String.format(Locale.getDefault(), "%02d:%02d", h, m),
                    hour = h,
                    minute = m
                )
            )
        }

        // Determine which prayer time is NEXT:
        // We compare exact second-of-day:
        var nextIndex = -1
        for (i in rawItems.indices) {
            val itemSecs = rawItems[i].hour * 3600 + rawItems[i].minute * 60
            if (itemSecs > currentSecondsOfDay) {
                nextIndex = i
                break
            }
        }

        // Next index breakdown:
        // nextIndex == 0: It is night before today's İmsak -> Next is İmsak (0), Current is Yatsı (5)
        // nextIndex == 1: İmsak passed, next is Güneş (1) -> Current is İmsak (0)
        // nextIndex == 2: Güneş passed, next is Öğle (2) -> Current is Güneş (1)
        // nextIndex == 3: Öğle passed, next is İkindi (3) -> Current is Öğle (2)
        // nextIndex == 4: İkindi passed, next is Akşam (4) -> Current is İkindi (3)
        // nextIndex == 5: Akşam passed, next is Yatsı (5) -> Current is Akşam (4)
        // nextIndex == -1: Yatsı passed! Next is tomorrow's İmsak (0) -> Current is Yatsı (5)

        val finalizedPrayers = rawItems.mapIndexed { index, item ->
            val isNext = if (nextIndex == -1) index == 0 else index == nextIndex
            val isCurrent = when (nextIndex) {
                -1 -> index == rawItems.lastIndex // Yatsı
                0 -> index == rawItems.lastIndex // Yatsı of previous night
                else -> index == (nextIndex - 1)
            }
            val isPassed = when (nextIndex) {
                -1 -> index < rawItems.lastIndex // All except Yatsı passed
                0 -> false // None passed yet today
                else -> index < (nextIndex - 1)
            }
            item.copy(isNext = isNext, isCurrent = isCurrent, isPassed = isPassed)
        }

        val nextPrayerItem = if (nextIndex == -1) finalizedPrayers[0] else finalizedPrayers[nextIndex]

        // Calculate remaining seconds exactly:
        val remainingSeconds = if (nextIndex != -1) {
            val targetSecs = nextPrayerItem.hour * 3600 + nextPrayerItem.minute * 60
            (targetSecs - currentSecondsOfDay).toLong().coerceAtLeast(0L)
        } else {
            // After Yatsı: wait until tomorrow's İmsak
            val secsUntilMidnight = (24 * 3600 - currentSecondsOfDay)
            val tomorrowImsakSecs = finalizedPrayers[0].hour * 3600 + finalizedPrayers[0].minute * 60
            (secsUntilMidnight + tomorrowImsakSecs).toLong().coerceAtLeast(0L)
        }

        val remH = (remainingSeconds / 3600).toInt()
        val remM = ((remainingSeconds % 3600) / 60).toInt()
        val remS = (remainingSeconds % 60).toInt()

        val customLabel = when (nextPrayerItem.type) {
            PrayerType.IMSAK -> "İmsak Vaktine Kalan Süre"
            PrayerType.GUNES -> "Güneş Doğuşuna Kalan Süre"
            PrayerType.OGLE -> "Öğle Vaktine Kalan Süre"
            PrayerType.IKINDI -> "İkindi Vaktine Kalan Süre"
            PrayerType.AKSAM -> "Akşam / İftar Vaktine Kalan Süre"
            PrayerType.YATSI -> "Yatsı Vaktine Kalan Süre"
        }

        val nextInfo = NextPrayerInfo(
            prayer = nextPrayerItem,
            remainingHours = remH,
            remainingMinutes = remM,
            remainingSeconds = remS,
            totalRemainingSeconds = remainingSeconds,
            formattedCountdown = String.format(Locale.getDefault(), "%02d:%02d:%02d", remH, remM, remS),
            titleLabel = customLabel
        )

        // Gregorian Date
        val monthsTr = listOf(
            "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
            "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
        )
        val daysTr = listOf(
            "Pazar", "Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi"
        )
        val dayOfWeek = daysTr[calendar.get(Calendar.DAY_OF_WEEK) - 1]
        val gregorianFormatted = "$day ${monthsTr[month - 1]} $year, $dayOfWeek"

        // Hijri date approximation
        val hijriFormatted = calculateHijriDate(calendar)

        // Qibla angle and distance
        val qiblaAngle = calculateQiblaAngle(city.latitude, city.longitude)
        val distance = calculateDistanceKm(city.latitude, city.longitude, KAABA_LAT, KAABA_LNG)

        return DailyPrayerTimes(
            city = city,
            gregorianDate = gregorianFormatted,
            hijriDate = hijriFormatted,
            prayers = finalizedPrayers,
            nextPrayerInfo = nextInfo,
            qiblaAngle = qiblaAngle,
            distanceToKaabaKm = distance
        )
    }

    private fun hourAngle(altitude: Double, latRad: Double, decRad: Double): Double {
        val altRad = Math.toRadians(altitude)
        val cosH = (sin(altRad) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
        val clamped = cosH.coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(clamped))
    }

    fun calculateQiblaAngle(lat: Double, lng: Double): Double {
        val phi1 = Math.toRadians(lat)
        val phi2 = Math.toRadians(KAABA_LAT)
        val deltaLambda = Math.toRadians(KAABA_LNG - lng)

        val y = sin(deltaLambda)
        val x = cos(phi1) * tan(phi2) - sin(phi1) * cos(deltaLambda)
        var qibla = Math.toDegrees(atan2(y, x))
        qibla = (qibla + 360.0) % 360.0
        return qibla
    }

    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    private fun calculateHijriDate(calendar: Calendar): String {
        var d = calendar.get(Calendar.DAY_OF_MONTH)
        var m = calendar.get(Calendar.MONTH)
        var y = calendar.get(Calendar.YEAR)

        if (m < 2) {
            y -= 1
            m += 12
        }

        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + d + b - 1524.5

        val z = jd - 1948440.0
        val cyc = floor(z / 10631.0)
        val rem = z - 10631.0 * cyc
        val j = floor((rem - 1) / 354.36667)
        val hijriYear = (30 * cyc + j).toInt() + 1
        var daysInYear = floor(rem - floor(j * 354.36667)).toInt()

        val hijriMonths = listOf(
            "Muharrem", "Safer", "Rebîülevvel", "Rebîülâhir",
            "Cemâziyelevvel", "Cemâziyelâhir", "Recep", "Şaban",
            "Ramazan", "Şevval", "Zilkade", "Zilhicce"
        )

        var hijriMonth = 0
        while (hijriMonth < 12) {
            val daysInMonth = if (hijriMonth % 2 == 0) 30 else 29
            if (daysInYear <= daysInMonth) {
                break
            }
            daysInYear -= daysInMonth
            hijriMonth++
        }
        val hijriDay = daysInYear.coerceAtLeast(1)
        val monthName = hijriMonths[hijriMonth.coerceIn(0, 11)]

        return "$hijriDay $monthName $hijriYear H"
    }
}
