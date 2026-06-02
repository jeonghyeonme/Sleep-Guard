package com.sleepguard.util

import kotlin.math.abs

/**
 * High-Pass Filter implementation to detect sharp movements (touch spikes)
 * and ignore slow mattress movements.
 */
class HighPassFilter(cutoffFrequency: Float, sampleRate: Float) {
    private var previousValue: Float = 0f
    private var alpha: Float

    init {
        val rc = 1.0f / (cutoffFrequency * 2 * Math.PI.toFloat())
        val dt = 1.0f / sampleRate
        this.alpha = rc / (rc + dt)
    }

    fun filter(currentValue: Float): Float {
        // DC Offset removal / Simple HPF: y[i] = x[i] - x[i-1]
        val output = currentValue - previousValue
        previousValue = currentValue
        return output
    }
}

object MovementDetector {
    fun isSpike(value: Float, threshold: Float): Boolean {
        return abs(value) > threshold
    }
}
