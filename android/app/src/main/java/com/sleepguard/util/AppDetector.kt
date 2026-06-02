package com.sleepguard.util

import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build

class AppDetector(private val context: Context) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    fun getForegroundApp(): String? {
        val time = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            time - 1000 * 60,
            time
        )

        if (stats != null && stats.isNotEmpty()) {
            var lastApp: String? = null
            var lastTime = 0L
            
            for (usageStat in stats) {
                if (usageStat.lastTimeUsed > lastTime) {
                    lastApp = usageStat.packageName
                    lastTime = usageStat.lastTimeUsed
                }
            }
            return lastApp
        }
        return null
    }

    fun isTargetAppForeground(targetApps: Set<String>): Boolean {
        val currentApp = getForegroundApp()
        return targetApps.contains(currentApp)
    }
}
