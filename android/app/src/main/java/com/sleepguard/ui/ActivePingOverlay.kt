package com.sleepguard.ui

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Vibrator
import android.os.VibrationEffect
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.sleepguard.R
import kotlinx.coroutines.*

class ActivePingOverlay(private val context: Context, private val onDismissed: (Boolean) -> Unit) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: View? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var countdownJob: Job? = null

    fun show() {
        if (overlayView != null) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        )

        overlayView = LayoutInflater.from(context).inflate(R.layout.layout_active_ping, null)
        
        val btnIAmAwake = overlayView?.findViewById<Button>(R.id.btn_i_am_awake)
        val tvCountdown = overlayView?.findViewById<TextView>(R.id.tv_countdown)

        btnIAmAwake?.setOnClickListener {
            dismiss(true)
        }

        windowManager.addView(overlayView, params)
        startVibration()
        startCountdown(tvCountdown)
    }

    private fun startVibration() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1))
        } else {
            vibrator.vibrate(longArrayOf(0, 500, 200, 500), -1)
        }
    }

    private fun startCountdown(textView: TextView?) {
        countdownJob = scope.launch {
            for (i in 15 downTo 1) {
                textView?.text = "Closing media in ${i}s..."
                delay(1000)
            }
            dismiss(false) // Auto-dismiss as "Sleep Confirmed"
        }
    }

    private fun dismiss(isAwake: Boolean) {
        countdownJob?.cancel()
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
        }
        onDismissed(isAwake)
    }
}
