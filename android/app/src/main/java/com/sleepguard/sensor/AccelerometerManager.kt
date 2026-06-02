package com.sleepguard.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.sleepguard.util.HighPassFilter
import com.sleepguard.util.MovementDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AccelerometerData(val x: Float, val y: Float, val z: Float)

class AccelerometerManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val filterX = HighPassFilter(1f, 10f)
    private val filterY = HighPassFilter(1f, 10f)
    private val filterZ = HighPassFilter(1f, 10f)

    private val _data = MutableStateFlow(AccelerometerData(0f, 0f, 0f))
    val data = _data.asStateFlow()

    private val _isMovementDetected = MutableStateFlow(false)
    val isMovementDetected = _isMovementDetected.asStateFlow()

    fun start(threshold: Float = 0.5f) {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val fx = filterX.filter(event.values[0])
            val fy = filterY.filter(event.values[1])
            val fz = filterZ.filter(event.values[2])

            _data.value = AccelerometerData(fx, fy, fz)

            if (MovementDetector.isSpike(fx, 0.5f) || 
                MovementDetector.isSpike(fy, 0.5f) || 
                MovementDetector.isSpike(fz, 0.5f)) {
                _isMovementDetected.value = true
                // Reset flag after a short delay (logic usually handled in UI or Service)
            } else {
                _isMovementDetected.value = false
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
