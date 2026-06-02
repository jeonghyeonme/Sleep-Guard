package com.sleepguard.util

import android.content.Context
import android.content.SharedPreferences

class PreferenceHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sleep_guard_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TARGET_APPS = "target_apps"
        val DEFAULT_APPS = setOf("com.google.android.youtube", "com.netflix.mediaclient")
    }

    fun getTargetApps(): Set<String> {
        return prefs.getStringSet(KEY_TARGET_APPS, DEFAULT_APPS) ?: DEFAULT_APPS
    }

    fun setTargetApps(apps: Set<String>) {
        prefs.edit().putStringSet(KEY_TARGET_APPS, apps).apply()
    }

    fun addTargetApp(packageName: String) {
        val apps = getTargetApps().toMutableSet()
        apps.add(packageName)
        setTargetApps(apps)
    }

    fun removeTargetApp(packageName: String) {
        val apps = getTargetApps().toMutableSet()
        apps.remove(packageName)
        setTargetApps(apps)
    }
}
