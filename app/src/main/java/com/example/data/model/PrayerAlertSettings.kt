package com.example.data.model

import com.example.ui.util.AlertSound

data class PrayerAlertConfig(
    val prayerType: PrayerType,
    val isEnabled: Boolean = true,
    val alertSound: AlertSound = AlertSound.EZAN_HICAZ,
    val earlyWarningMinutes: Int = 0 // 0 = exact time, 15 = 15 min before, 30 = 30 min before
)

data class GlobalAlertSettings(
    val isMasterAudioEnabled: Boolean = true,
    val defaultSound: AlertSound = AlertSound.EZAN_HICAZ,
    val earlyNotificationMinutes: Int = 15,
    val configs: Map<PrayerType, PrayerAlertConfig> = PrayerType.entries.associateWith {
        PrayerAlertConfig(prayerType = it, isEnabled = true, alertSound = AlertSound.EZAN_HICAZ)
    }
)
