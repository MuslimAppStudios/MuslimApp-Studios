package com.example.data.repository

import com.example.data.model.City
import com.example.data.model.Turkey81Provinces
import com.example.data.model.WeatherCodeMapper
import com.example.data.model.WeatherInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.math.sin

class WeatherRepository {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val liveWeatherCache = ConcurrentHashMap<String, WeatherInfo>()

    suspend fun getLiveWeather(city: City): WeatherInfo = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.open-meteo.com/v1/forecast?latitude=${city.latitude}&longitude=${city.longitude}&current=temperature_2m,relative_humidity_2m,is_day,weather_code,wind_speed_10m"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "KiblegahApp/1.0")
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyString = response.body?.string()
                if (!bodyString.isNullOrBlank()) {
                    val json = JSONObject(bodyString)
                    val current = json.getJSONObject("current")

                    val temp = current.optDouble("temperature_2m", 18.0)
                    val humidity = current.optInt("relative_humidity_2m", 50)
                    val isDay = current.optInt("is_day", 1) == 1
                    val weatherCode = current.optInt("weather_code", 0)
                    val windSpeed = current.optDouble("wind_speed_10m", 10.0)

                    val info = WeatherInfo(
                        temperature = temp,
                        condition = WeatherCodeMapper.getCondition(weatherCode),
                        weatherCode = weatherCode,
                        windSpeed = windSpeed,
                        humidity = humidity,
                        isDay = isDay,
                        isLive = true,
                        cityName = city.name
                    )
                    liveWeatherCache[city.id] = info
                    return@withContext info
                }
            }
        } catch (_: Exception) {
            // Fall through to offline calculation
        }

        val offline = calculateOfflineWeather(city)
        return@withContext liveWeatherCache[city.id] ?: offline
    }

    fun getAll81Weather(): List<Pair<City, WeatherInfo>> {
        return Turkey81Provinces.map { city ->
            val cached = liveWeatherCache[city.id]
            if (cached != null) {
                city to cached
            } else {
                city to calculateOfflineWeather(city)
            }
        }
    }

    fun calculateOfflineWeather(city: City): WeatherInfo {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val month = cal.get(Calendar.MONTH) // 0-11
        val isDay = hour in 6..19

        // Base seasonal temp in Turkey
        val seasonalBase = when (month) {
            11, 0, 1 -> 7.0 // Winter
            2, 3, 4 -> 15.0 // Spring
            5, 6, 7 -> 27.0 // Summer
            else -> 17.0 // Autumn
        }

        // Regional microclimate adjustment based on geographic elevation & maritime factors
        val regionModifier = when (city.region) {
            "Akdeniz" -> 4.5
            "Güneydoğu Anadolu" -> 3.5
            "Ege" -> 3.0
            "Marmara" -> 1.0
            "Karadeniz" -> 0.0
            "İç Anadolu" -> -1.5
            "Doğu Anadolu" -> -5.5
            else -> 0.0
        }

        val latEffect = (41.5 - city.latitude) * 0.9
        val diurnalEffect = 4.5 * sin(Math.toRadians((hour - 8) * 15.0))
        val estTemp = ((seasonalBase + regionModifier + latEffect + diurnalEffect) * 10).roundToInt() / 10.0

        val estHumidity = when (city.region) {
            "Karadeniz" -> 72
            "Marmara" -> 64
            "Akdeniz", "Ege" -> 58
            "Güneydoğu Anadolu" -> 38
            else -> 48
        }

        val estWind = if (city.region in listOf("Marmara", "Ege", "Karadeniz")) 16.0 else 10.0

        return WeatherInfo(
            temperature = estTemp,
            condition = if (isDay) "Açık / Güneşli" else "Açık Gece",
            weatherCode = 0,
            windSpeed = estWind,
            humidity = estHumidity,
            isDay = isDay,
            isLive = false,
            cityName = city.name
        )
    }
}
