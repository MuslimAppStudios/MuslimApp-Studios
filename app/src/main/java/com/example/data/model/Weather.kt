package com.example.data.model

data class WeatherInfo(
    val temperature: Double,
    val condition: String,
    val weatherCode: Int,
    val windSpeed: Double,
    val humidity: Int,
    val isDay: Boolean,
    val isLive: Boolean,
    val cityName: String
)

object WeatherCodeMapper {
    fun getCondition(code: Int): String {
        return when (code) {
            0 -> "Açık / Güneşli"
            1 -> "Çoğunlukla Açık"
            2 -> "Parçalı Bulutlu"
            3 -> "Bulutlu / Kapalı"
            45, 48 -> "Sisli"
            51, 53, 55 -> "Hafif Çisenti"
            61, 63, 65 -> "Yağmurlu"
            66, 67 -> "Dondurucu Yağmur"
            71, 73, 75 -> "Kar Yağışlı"
            77 -> "Kar Taneleri"
            80, 81, 82 -> "Sağanak Yağış"
            85, 86 -> "Kar Sağanağı"
            95, 96, 99 -> "Gök Gürültülü Fırtına"
            else -> "Açık"
        }
    }
}
