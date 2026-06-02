package com.sleepguard.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sleepguard.MainActivity
import com.sleepguard.R
import com.sleepguard.sensor.AccelerometerManager
import com.sleepguard.util.AppDetector
import com.sleepguard.util.PreferenceHelper
import com.sleepguard.util.MediaControlUtil
import com.sleepguard.util.DeviceAdminManager
import com.sleepguard.ui.ActivePingOverlay
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class SleepGuardService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private lateinit var accelerometerManager: AccelerometerManager
    private lateinit var appDetector: AppDetector
    private lateinit var preferenceHelper: PreferenceHelper
    private lateinit var mediaControlUtil: MediaControlUtil
    private lateinit var deviceAdminManager: DeviceAdminManager
    private var activePingOverlay: ActivePingOverlay? = null

    private var isMonitoring = false
    private var isMonitoringLoopStarted = false
    private var lastMovementTime = System.currentTimeMillis()
    private val INACTIVITY_THRESHOLD = 10 * 1000L // 10 seconds for testing

    companion object {
        const val ACTION_STOP = "com.sleepguard.action.STOP"
    }

    override fun onCreate() {
        super.onCreate()
        accelerometerManager = AccelerometerManager(this)
        appDetector = AppDetector(this)
        preferenceHelper = PreferenceHelper(this)
        mediaControlUtil = MediaControlUtil(this)
        deviceAdminManager = DeviceAdminManager(this)
        
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = createNotification("Sleep Guard is protecting your sleep")
        startForeground(1, notification)

        if (!isMonitoringLoopStarted) {
            startMonitoringLoop()
            isMonitoringLoopStarted = true
        }
        
        return START_STICKY
    }

    private fun startMonitoringLoop() {
        serviceScope.launch {
            launch {
                accelerometerManager.isMovementDetected.collect { isDetected ->
                    if (isDetected) {
                        lastMovementTime = System.currentTimeMillis()
                    }
                }
            }

            while (isActive) {
                val targetApps = preferenceHelper.getTargetApps()
                val isTargetActive = appDetector.isTargetAppForeground(targetApps)

                if (isTargetActive) {
                    if (!isMonitoring) {
                        accelerometerManager.start()
                        isMonitoring = true
                        lastMovementTime = System.currentTimeMillis()
                    }

                    val idleTime = System.currentTimeMillis() - lastMovementTime
                    if (idleTime > INACTIVITY_THRESHOLD && activePingOverlay == null) {
                        withContext(Dispatchers.Main) {
                            showActivePing()
                        }
                    }
                } else if (isMonitoring) {
                    accelerometerManager.stop()
                    isMonitoring = false
                }

                delay(5000)
            }
        }
    }

    private fun showActivePing() {
        activePingOverlay = ActivePingOverlay(this) { isAwake ->
            activePingOverlay = null
            lastMovementTime = System.currentTimeMillis()
            if (!isAwake) {
                mediaControlUtil.pauseMedia()
                deviceAdminManager.lockScreen()
                updateNotification("Media paused & Screen locked")
            }
        }
        activePingOverlay?.show()
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "sleep_guard_channel",
                "Sleep Guard Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(content: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, SleepGuardService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, "sleep_guard_channel")
            .setContentTitle("Sleep Guard")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .build()
    }

    private fun updateNotification(content: String) {
        if (content.contains("paused") || content.contains("locked")) {
            val notification = createNotification(content)
            val manager = getSystemService(NotificationManager::class.java)
            manager.notify(1, notification)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        accelerometerManager.stop()
        activePingOverlay = null
    }
}
