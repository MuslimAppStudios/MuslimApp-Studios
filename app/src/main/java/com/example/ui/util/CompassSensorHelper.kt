package com.example.ui.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

class CompassSensorHelper(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _azimuth = MutableStateFlow(0f)
    val azimuth: StateFlow<Float> = _azimuth.asStateFlow()

    private val _isSensorAvailable = MutableStateFlow(false)
    val isSensorAvailable: StateFlow<Boolean> = _isSensorAvailable.asStateFlow()

    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null
    private var lastAzimuth = 0f

    fun startListening() {
        if (sensorManager == null) return

        if (rotationSensor != null) {
            val registered = sensorManager.registerListener(
                this,
                rotationSensor,
                SensorManager.SENSOR_DELAY_UI
            )
            _isSensorAvailable.value = registered
        } else if (accelerometer != null && magnetometer != null) {
            val regAcc = sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            val regMag = sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
            _isSensorAvailable.value = regAcc && regMag
        } else {
            _isSensorAvailable.value = false
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            var degree = Math.toDegrees(orientation[0].toDouble()).toFloat()
            if (degree < 0) degree += 360f
            updateSmoothAzimuth(degree)
        } else {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                gravity = lowPass(event.values.clone(), gravity)
            }
            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic = lowPass(event.values.clone(), geomagnetic)
            }
            if (gravity != null && geomagnetic != null) {
                val r = FloatArray(9)
                val i = FloatArray(9)
                if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(r, orientation)
                    var degree = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    if (degree < 0) degree += 360f
                    updateSmoothAzimuth(degree)
                }
            }
        }
    }

    private fun updateSmoothAzimuth(target: Float) {
        // Smooth transition
        val diff = ((target - lastAzimuth + 540) % 360) - 180
        val smoothed = (lastAzimuth + diff * 0.2f + 360) % 360
        lastAzimuth = smoothed
        _azimuth.value = (smoothed * 10).roundToInt() / 10f
    }

    private fun lowPass(input: FloatArray, output: FloatArray?): FloatArray {
        if (output == null) return input
        val alpha = 0.25f
        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
        return output
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
