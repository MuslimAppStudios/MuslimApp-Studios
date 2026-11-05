package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.PrayerTimeItem
import com.example.data.model.PrayerType
import com.example.ui.components.AllProvincesWeatherDialog
import com.example.ui.components.AudioAlertSettingsDialog
import com.example.ui.components.CityPickerDialog
import com.example.ui.components.LiveWeatherCard
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.util.CompassSensorHelper
import com.example.ui.viewmodel.KiblegahViewModel
import kotlin.math.roundToInt

@Composable
fun PrayerTimesScreen(
    viewModel: KiblegahViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prayerTimes by viewModel.prayerTimes.collectAsStateWithLifecycle()
    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val liveWeather by viewModel.liveWeather.collectAsStateWithLifecycle()
    val all81Weather by viewModel.all81Weather.collectAsStateWithLifecycle()
    val isWeatherLoading by viewModel.isWeatherLoading.collectAsStateWithLifecycle()
    val alertSettings by viewModel.alertSettings.collectAsStateWithLifecycle()

    var showCityPicker by remember { mutableStateOf(false) }
    var showAudioAlertsDialog by remember { mutableStateOf(false) }
    var show81WeatherDialog by remember { mutableStateOf(false) }

    // Compass sensor helper
    val compassSensor = remember { CompassSensorHelper(context) }
    DisposableEffect(Unit) {
        compassSensor.startListening()
        onDispose { compassSensor.stopListening() }
    }
    val currentAzimuth by compassSensor.azimuth.collectAsStateWithLifecycle()
    val isSensorAvailable by compassSensor.isSensorAvailable.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()

    if (showCityPicker) {
        CityPickerDialog(
            currentCity = selectedCity,
            onDismiss = { showCityPicker = false },
            onCitySelected = { viewModel.selectCity(it) },
            onOpen81Weather = { show81WeatherDialog = true }
        )
    }

    if (show81WeatherDialog) {
        AllProvincesWeatherDialog(
            provincesWithWeather = all81Weather,
            currentCity = selectedCity,
            onDismiss = { show81WeatherDialog = false },
            onSelectCity = { city ->
                viewModel.selectCity(city)
                show81WeatherDialog = false
            }
        )
    }

    if (showAudioAlertsDialog) {
        AudioAlertSettingsDialog(
            settings = alertSettings,
            onDismiss = { showAudioAlertsDialog = false },
            onToggleMaster = { viewModel.toggleMasterAudio() },
            onSetDefaultSound = { viewModel.setDefaultSound(it) },
            onTogglePrayer = { viewModel.togglePrayerAlert(it) },
            onSetPrayerSound = { prayer, sound -> viewModel.setPrayerAlertSound(prayer, sound) }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 28.dp)
            .testTag("prayer_times_screen")
    ) {
        // --- Hero Mosque Card ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .height(250.dp)
                .clip(RoundedCornerShape(28.dp))
        ) {
            // Background Mosque Illustration
            Image(
                painter = painterResource(id = R.drawable.banner_mosque),
                contentDescription = "Cami Silueti",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient Overlay for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.35f),
                                Color.Black.copy(alpha = 0.82f)
                            )
                        )
                    )
            )

            // Hero Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: City selector with plate badge and Hijri date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .clickable { showCityPicker = true }
                            .testTag("open_city_picker_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            if (selectedCity.plateCode.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = GoldAccent,
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Text(
                                        text = selectedCity.plateCode,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            Text(
                                text = selectedCity.name,
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Text(
                        text = prayerTimes.hijriDate,
                        color = GoldAccent,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Middle/Bottom: Countdown to Next Prayer
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val nextInfo = prayerTimes.nextPrayerInfo
                    val currentPrayer = prayerTimes.prayers.firstOrNull { it.isCurrent }
                    if (nextInfo != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (currentPrayer != null) {
                                Surface(
                                    color = Color.Black.copy(alpha = 0.45f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.8.dp, Color.White.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = "Şu an: ${currentPrayer.name}",
                                        color = Color.White.copy(alpha = 0.9f),
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            Surface(
                                color = EmeraldPrimary.copy(alpha = 0.88f),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.7f))
                            ) {
                                Text(
                                    text = nextInfo.titleLabel.ifBlank { "${nextInfo.prayer.name} Vaktine Kalan Süre" },
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = nextInfo.formattedCountdown,
                            color = Color.White,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            )
                        )

                        Text(
                            text = prayerTimes.gregorianDate,
                            color = Color.White.copy(alpha = 0.88f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // --- Live Weather Card (Hava Durumu Canlı) ---
        LiveWeatherCard(
            city = selectedCity,
            weather = liveWeather,
            isLoading = isWeatherLoading,
            onRefresh = { viewModel.refreshWeather() },
            onView81Provinces = { show81WeatherDialog = true },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // --- Sesli Uyarı & Ezan Sistemi Hızlı Ayar Butonu ---
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (alertSettings.isMasterAudioEnabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (alertSettings.isMasterAudioEnabled) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { showAudioAlertsDialog = true }
                .testTag("open_audio_alert_settings")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (alertSettings.isMasterAudioEnabled) EmeraldPrimary else MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (alertSettings.isMasterAudioEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = null,
                                tint = if (alertSettings.isMasterAudioEnabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Sesli Vakit Uyarısı",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (alertSettings.isMasterAudioEnabled) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            ) {
                                Text(
                                    text = if (alertSettings.isMasterAudioEnabled) "Açık" else "Sessiz",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (alertSettings.isMasterAudioEnabled) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Ses: ${alertSettings.defaultSound.displayName} (Değiştirmek için dokunun)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ses Ayarları",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- Prayer Times Grid (6 Vakit) ---
        Text(
            text = "Günün Vakitleri",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            prayerTimes.prayers.forEach { prayer ->
                PrayerTimeRowCard(prayer = prayer)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Kerahat Vakitleri Bilgi Kartı ---
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Kerahat Vakitleri Hatırlatması",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Güneş doğarken (45 dk), zeval vaktinde (öğleden 30 dk önce) ve güneş batarken nafile namaz kılınması mekruhtur.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Qibla Compass Section (Kıble Pusulası) ---
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("qibla_compass_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = EmeraldPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kıble Pusulası",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${prayerTimes.qiblaAngle.roundToInt()}°",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Compass Dial with animation
                val qiblaAngle = prayerTimes.qiblaAngle.toFloat()
                val needleRotation by animateFloatAsState(
                    targetValue = if (isSensorAvailable) (qiblaAngle - currentAzimuth + 360f) % 360f else qiblaAngle,
                    animationSpec = spring(),
                    label = "compass_needle"
                )

                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .border(
                            width = 3.dp,
                            brush = Brush.sweepGradient(listOf(GoldAccent, EmeraldPrimary, GoldAccent)),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "K",
                        fontWeight = FontWeight.Bold,
                        color = Color.Red,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    )
                    Text(
                        text = "G",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    )
                    Text(
                        text = "D",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = "B",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp)
                    )

                    // Compass Needle & Kaaba indicator pointing towards Qibla
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(needleRotation)
                    ) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Surface(
                            shape = CircleShape,
                            color = GoldAccent,
                            modifier = Modifier.size(32.dp),
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = "Kıble Yönü",
                                    tint = Color(0xFF1E3A2B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(50.dp)
                                .background(GoldAccent.copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.height(28.dp))
                    }

                    Surface(
                        shape = CircleShape,
                        color = EmeraldPrimary,
                        border = androidx.compose.foundation.BorderStroke(2.dp, GoldAccent),
                        modifier = Modifier.size(16.dp)
                    ) {}
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "${selectedCity.name} için Kıble Açısı: ${prayerTimes.qiblaAngle.roundToInt()}°",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Kâbe-i Muazzama'ya Mesafe: ${prayerTimes.distanceToKaabaKm.roundToInt()} km",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!isSensorAvailable) {
                    Text(
                        text = "• Cihaz pusula sensörü sabit görünümde veya emülatör modunda.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PrayerTimeRowCard(prayer: PrayerTimeItem) {
    val icon: ImageVector = when (prayer.type) {
        PrayerType.IMSAK -> Icons.Default.NightsStay
        PrayerType.GUNES -> Icons.Default.WbSunny
        PrayerType.OGLE -> Icons.Default.LightMode
        PrayerType.IKINDI -> Icons.Default.WbTwilight
        PrayerType.AKSAM -> Icons.Default.Brightness4
        PrayerType.YATSI -> Icons.Default.Bedtime
    }

    val cardColor = when {
        prayer.isCurrent -> MaterialTheme.colorScheme.primaryContainer
        prayer.isNext -> Color(0xFFFFF9E6)
        prayer.isPassed -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        prayer.isCurrent -> MaterialTheme.colorScheme.primary
        prayer.isNext -> GoldAccent
        else -> Color.Transparent
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = if (prayer.isCurrent || prayer.isNext) androidx.compose.foundation.BorderStroke(1.5.dp, borderColor) else null,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prayer_card_${prayer.type.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = if (prayer.isCurrent) EmeraldPrimary else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (prayer.isCurrent) Color.White else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = prayer.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (prayer.isCurrent || prayer.isNext) FontWeight.Bold else FontWeight.Medium,
                            color = if (prayer.isPassed && !prayer.isCurrent) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                        if (prayer.isCurrent) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldPrimary
                            ) {
                                Text(
                                    text = "Şu Anki Vakit",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else if (prayer.isNext) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = GoldDark
                            ) {
                                Text(
                                    text = "Sıradaki Vakit",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = prayer.type.meaning,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = prayer.timeString,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (prayer.isCurrent) EmeraldPrimary else if (prayer.isNext) GoldDark else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
