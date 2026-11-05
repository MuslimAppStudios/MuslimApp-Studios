package com.example.ui.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext
import kotlin.math.sin

enum class AlertSound(val displayName: String, val description: String) {
    EZAN_HICAZ("Hicaz Makamı Ezanı", "Öğle ve yatsı vakitlerinin huşu dolu ezanı"),
    EZAN_SABA("Saba Makamı Ezanı", "Sabah seher vaktinin hüzünlü ve dokunaklı ezanı"),
    EZAN_RAST("Rast Makamı Ezanı", "İkindi vaktinin canlı ve vakur ezan nağmeleri"),
    EZAN_SEGAH("Segah Makamı Ezanı", "Akşam namazının duygulu ve seri ezanı"),
    EZAN_USSAK("Uşşak Makamı Ezanı", "Geleneksel kadim Türk ezan makamı"),
    BIP_BIP("Bip Bip Uyarı Sesi", "Kısa ve net çift bip bildirim sesi"),
    TITRESIM("Yalnızca Titreşim", "Sessiz mod, çift darbeli titreşim")
}

class AudioAlertHelper(private val context: Context) {

    private var currentJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun playPreview(sound: AlertSound, onFinish: () -> Unit = {}) {
        stop()
        currentJob = scope.launch {
            try {
                when (sound) {
                    AlertSound.EZAN_HICAZ -> playSynthesizedAdhan(
                        listOf(293.66, 311.13, 369.99, 392.00, 440.00, 369.99, 311.13, 293.66),
                        durationMs = 400
                    )
                    AlertSound.EZAN_SABA -> playSynthesizedAdhan(
                        listOf(293.66, 320.00, 349.23, 370.00, 440.00, 349.23, 293.66),
                        durationMs = 460
                    )
                    AlertSound.EZAN_RAST -> playSynthesizedAdhan(
                        listOf(261.63, 293.66, 329.63, 349.23, 392.00, 440.00, 392.00, 349.23),
                        durationMs = 380
                    )
                    AlertSound.EZAN_SEGAH -> playSynthesizedAdhan(
                        listOf(330.00, 349.23, 392.00, 440.00, 392.00, 349.23, 330.00),
                        durationMs = 360
                    )
                    AlertSound.EZAN_USSAK -> playSynthesizedAdhan(
                        listOf(293.66, 329.63, 349.23, 392.00, 440.00, 392.00, 293.66),
                        durationMs = 400
                    )
                    AlertSound.BIP_BIP -> playSynthesizedDoubleBeep()
                    AlertSound.TITRESIM -> triggerVibration()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    onFinish()
                }
            }
        }
    }

    fun stop() {
        currentJob?.cancel()
        currentJob = null
    }

    private suspend fun playSynthesizedAdhan(frequencies: List<Double>, durationMs: Long) {
        val sampleRate = 44100
        val numSamplesPerNote = (sampleRate * (durationMs / 1000.0)).toInt()

        frequencies.forEach { freq ->
            if (!coroutineContext.isActive) return
            val buffer = ShortArray(numSamplesPerNote)
            for (i in 0 until numSamplesPerNote) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / numSamplesPerNote
                // Smooth bell/vocal curve
                val envelope = if (progress < 0.15) progress / 0.15 else (1.0 - progress) * 0.9 + 0.1

                val sample = (sin(2.0 * Math.PI * freq * t) * 0.7 +
                        sin(4.0 * Math.PI * freq * t) * 0.2 +
                        sin(6.0 * Math.PI * freq * t) * 0.1) * envelope * Short.MAX_VALUE * 0.85
                buffer[i] = sample.toInt().toShort()
            }
            playPcmBuffer(buffer, sampleRate)
            delay(50)
        }
    }

    private suspend fun playSynthesizedDoubleBeep() {
        val sampleRate = 44100
        repeat(2) {
            if (!coroutineContext.isActive) return
            val durationMs = 160L
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val buffer = ShortArray(numSamples)
            val freq = 950.0 // Clear high alert frequency

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / numSamples
                val envelope = if (progress < 0.08) progress / 0.08 else (1.0 - progress)
                val sample = sin(2.0 * Math.PI * freq * t) * envelope * Short.MAX_VALUE * 0.85
                buffer[i] = sample.toInt().toShort()
            }
            playPcmBuffer(buffer, sampleRate)
            delay(90)
        }
    }

    private fun triggerVibration() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 250, 120, 300), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(300)
            }
        } catch (_: Exception) {}
    }

    private fun playPcmBuffer(buffer: ShortArray, sampleRate: Int) {
        var track: AudioTrack? = null
        try {
            track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            Thread.sleep((buffer.size * 1000L) / sampleRate)
        } catch (_: Exception) {
        } finally {
            try {
                track?.stop()
                track?.release()
            } catch (_: Exception) {}
        }
    }
}
