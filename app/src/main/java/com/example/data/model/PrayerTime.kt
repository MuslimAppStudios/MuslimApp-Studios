package com.example.data.model

enum class PrayerType(val turkishName: String, val meaning: String) {
    IMSAK("İmsak", "Sabah namazı vakti girişi, oruç başlama anı"),
    GUNES("Güneş", "Güneş doğuşu, kerahet vakti başlangıcı"),
    OGLE("Öğle", "Zeval vaktinden sonra öğle namazı vakti"),
    IKINDI("İkindi", "Asr vakti, gölgelerin uzama anı"),
    AKSAM("Akşam", "Güneş batışı, iftar vakti"),
    YATSI("Yatsı", "Şafak kayboluşu, yatsı ve vitir vakti")
}

data class PrayerTimeItem(
    val type: PrayerType,
    val name: String,
    val timeString: String,
    val hour: Int,
    val minute: Int,
    val isPassed: Boolean = false,
    val isCurrent: Boolean = false,
    val isNext: Boolean = false
)

data class NextPrayerInfo(
    val prayer: PrayerTimeItem,
    val remainingHours: Int,
    val remainingMinutes: Int,
    val remainingSeconds: Int,
    val totalRemainingSeconds: Long,
    val formattedCountdown: String,
    val titleLabel: String = ""
)

data class DailyPrayerTimes(
    val city: City,
    val gregorianDate: String,
    val hijriDate: String,
    val prayers: List<PrayerTimeItem>,
    val nextPrayerInfo: NextPrayerInfo?,
    val qiblaAngle: Double,
    val distanceToKaabaKm: Double
)
