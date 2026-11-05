package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.viewmodel.KiblegahViewModel

@Composable
fun DailyTrackerScreen(
    viewModel: KiblegahViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val todayHabit by viewModel.todayHabit.collectAsStateWithLifecycle()
    val kazaCounter by viewModel.kazaCounter.collectAsStateWithLifecycle()

    val dhikrCount by viewModel.dhikrCount.collectAsStateWithLifecycle()
    val dhikrTarget by viewModel.dhikrTarget.collectAsStateWithLifecycle()
    val activeDhikrName by viewModel.activeDhikrName.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()

    var showResetDhikrDialog by remember { mutableStateOf(false) }
    var isKazaExpanded by remember { mutableStateOf(false) }

    // Haptic feedback trigger
    fun triggerVibration() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(35)
            }
        } catch (_: Exception) {}
    }

    if (showResetDhikrDialog) {
        AlertDialog(
            onDismissRequest = { showResetDhikrDialog = false },
            title = { Text("Zikir Sayacını Sıfırla") },
            text = { Text("Mevcut zikir sayısını sıfırlamak istediğinize emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetDhikr()
                        showResetDhikrDialog = false
                    },
                    modifier = Modifier.testTag("confirm_reset_dhikr")
                ) {
                    Text("Sıfırla", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDhikrDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 28.dp)
            .testTag("daily_tracker_screen")
    ) {
        // --- Daily Progress Summary Card ---
        val completedPrayersCount = listOf(
            todayHabit.sabahNamaz,
            todayHabit.ogleNamaz,
            todayHabit.ikindiNamaz,
            todayHabit.aksamNamaz,
            todayHabit.yatsiNamaz
        ).count { it }

        val progressPercentage = (completedPrayersCount / 5f)

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Bugünkü İbadet Çetelesi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "5 Vakit Namaz Takibi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (completedPrayersCount == 5) EmeraldPrimary else MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "$completedPrayersCount / 5 Vakit",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (completedPrayersCount == 5) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { progressPercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = EmeraldPrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // --- 5 Vakit Namaz Kontrol Listesi ---
        Text(
            text = "Günlük Namazlar",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PrayerCheckCard(
                title = "Sabah Namazı",
                desc = "2 Rekat Sünnet, 2 Rekat Farz",
                isChecked = todayHabit.sabahNamaz,
                onCheckedChange = { viewModel.togglePrayerCheck("sabah") },
                testTag = "prayer_check_sabah"
            )
            PrayerCheckCard(
                title = "Öğle Namazı",
                desc = "4 İlk Sünnet, 4 Farz, 2 Son Sünnet",
                isChecked = todayHabit.ogleNamaz,
                onCheckedChange = { viewModel.togglePrayerCheck("ogle") },
                testTag = "prayer_check_ogle"
            )
            PrayerCheckCard(
                title = "İkindi Namazı",
                desc = "4 Gayr-i Müekkede Sünnet, 4 Farz",
                isChecked = todayHabit.ikindiNamaz,
                onCheckedChange = { viewModel.togglePrayerCheck("ikindi") },
                testTag = "prayer_check_ikindi"
            )
            PrayerCheckCard(
                title = "Akşam Namazı",
                desc = "3 Farz, 2 Sünnet",
                isChecked = todayHabit.aksamNamaz,
                onCheckedChange = { viewModel.togglePrayerCheck("aksam") },
                testTag = "prayer_check_aksam"
            )
            PrayerCheckCard(
                title = "Yatsı Namazı",
                desc = "4 İlk Sünnet, 4 Farz, 2 Son Sünnet",
                isChecked = todayHabit.yatsiNamaz,
                onCheckedChange = { viewModel.togglePrayerCheck("yatsi") },
                testTag = "prayer_check_yatsi"
            )
            PrayerCheckCard(
                title = "Vitir Vacib Namazı",
                desc = "3 Rekat (Kunut Duaları ile)",
                isChecked = todayHabit.vitirNamaz,
                onCheckedChange = { viewModel.togglePrayerCheck("vitir") },
                testTag = "prayer_check_vitir"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Kur'an-ı Kerim Okuma Takibi ---
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = EmeraldPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kur'an-ı Kerim Okuma",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF7DD)
                    ) {
                        Text(
                            text = "${todayHabit.quranPagesRead} Sayfa",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldDark,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Bugün okuduğunuz sayfa adedini kaydedin:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.setQuranPages((todayHabit.quranPagesRead - 1).coerceAtLeast(0)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("quran_minus_page")
                    ) {
                        Text("-1")
                    }

                    FilledTonalButton(
                        onClick = { viewModel.setQuranPages(todayHabit.quranPagesRead + 1) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("quran_plus_1_page")
                    ) {
                        Text("+1 Sayfa")
                    }

                    FilledTonalButton(
                        onClick = { viewModel.setQuranPages(todayHabit.quranPagesRead + 5) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("quran_plus_5_pages")
                    ) {
                        Text("+5 Sayfa")
                    }

                    FilledTonalButton(
                        onClick = { viewModel.setQuranPages(todayHabit.quranPagesRead + 20) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("quran_plus_20_pages")
                    ) {
                        Text("+1 Cüz")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Dijital Zikirmatik / Tesbihat ---
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("zikirmatik_card")
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
                    Column {
                        Text(
                            text = "Dijital Zikirmatik",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Bugünkü Toplam: ${todayHabit.totalDhikr} zikir",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { showResetDhikrDialog = true },
                        modifier = Modifier.testTag("reset_dhikr_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sıfırla",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Preset Chips Row
                val presets = listOf(
                    "Sübhanallâh" to 33,
                    "Elhamdülillâh" to 33,
                    "Allâhu Ekber" to 33,
                    "Lâ ilâhe illallâh" to 100,
                    "Estağfirullâh" to 100,
                    "Salavat-ı Şerife" to 100,
                    "Serbest Zikir" to 0
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.take(3).forEach { (name, target) ->
                        val isSelected = activeDhikrName == name
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setDhikrPreset(name, target) },
                            label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.drop(3).take(3).forEach { (name, target) ->
                        val isSelected = activeDhikrName == name
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setDhikrPreset(name, target) },
                            label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Active Dhikr Display
                Text(
                    text = activeDhikrName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )

                Text(
                    text = if (dhikrTarget > 0) "Hedef: $dhikrTarget" else "Serbest Mod",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Big Tactile Tap Circle
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    EmeraldPrimary,
                                    Color(0xFF0A3F28)
                                )
                            )
                        )
                        .border(
                            width = 4.dp,
                            color = GoldAccent,
                            shape = CircleShape
                        )
                        .clickable {
                            triggerVibration()
                            val reached = viewModel.incrementDhikr()
                            if (reached) {
                                triggerVibration()
                            }
                        }
                        .testTag("dhikr_tap_pad"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$dhikrCount",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "DOKUN",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Kaza Namazı Defteri (Expandable) ---
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("kaza_counter_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isKazaExpanded = !isKazaExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HistoryEdu,
                            contentDescription = null,
                            tint = EmeraldPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Kaza Namazı Defteri",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Geçmiş borç namazları takip edin",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = { isKazaExpanded = !isKazaExpanded }) {
                        Icon(
                            imageVector = if (isKazaExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Genişlet/Daralt"
                        )
                    }
                }

                AnimatedVisibility(visible = isKazaExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KazaRowItem("Sabah", kazaCounter.sabah, onMinus = { viewModel.updateKaza("sabah", -1) }, onPlus = { viewModel.updateKaza("sabah", +1) })
                        KazaRowItem("Öğle", kazaCounter.ogle, onMinus = { viewModel.updateKaza("ogle", -1) }, onPlus = { viewModel.updateKaza("ogle", +1) })
                        KazaRowItem("İkindi", kazaCounter.ikindi, onMinus = { viewModel.updateKaza("ikindi", -1) }, onPlus = { viewModel.updateKaza("ikindi", +1) })
                        KazaRowItem("Akşam", kazaCounter.aksam, onMinus = { viewModel.updateKaza("aksam", -1) }, onPlus = { viewModel.updateKaza("aksam", +1) })
                        KazaRowItem("Yatsı", kazaCounter.yatsi, onMinus = { viewModel.updateKaza("yatsi", -1) }, onPlus = { viewModel.updateKaza("yatsi", +1) })
                        KazaRowItem("Vitir", kazaCounter.vitir, onMinus = { viewModel.updateKaza("vitir", -1) }, onPlus = { viewModel.updateKaza("vitir", +1) })
                        KazaRowItem("Oruç (Gün)", kazaCounter.oruc, onMinus = { viewModel.updateKaza("oruc", -1) }, onPlus = { viewModel.updateKaza("oruc", +1) })
                    }
                }
            }
        }
    }
}

@Composable
fun PrayerCheckCard(
    title: String,
    desc: String,
    isChecked: Boolean,
    onCheckedChange: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isChecked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = if (isChecked) androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary) else androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isChecked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Checkbox(
                checked = isChecked,
                onCheckedChange = { onCheckedChange() },
                colors = CheckboxDefaults.colors(
                    checkedColor = EmeraldPrimary,
                    checkmarkColor = Color.White
                )
            )
        }
    }
}

@Composable
fun KazaRowItem(
    title: String,
    count: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedIconButton(
                    onClick = onMinus,
                    enabled = count > 0,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Azalt", modifier = Modifier.size(16.dp))
                }

                Text(
                    text = "$count",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(48.dp)
                )

                FilledTonalIconButton(
                    onClick = onPlus,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Artır", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
