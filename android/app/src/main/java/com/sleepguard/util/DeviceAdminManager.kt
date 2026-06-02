package com.sleepguard.util

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.sleepguard.service.SleepGuardAdminReceiver

class DeviceAdminManager(private val context: Context) {
    private val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val adminComponent = ComponentName(context, SleepGuardAdminReceiver::class.java)

    fun isAdminActive(): Boolean {
        return devicePolicyManager.isAdminActive(adminComponent)
    }

    fun requestAdminPermission() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Sleep Guard needs this to lock the screen automatically.")
        context.startActivity(intent)
    }

    fun lockScreen() {
        if (isAdminActive()) {
            devicePolicyManager.lockNow()
        }
    }
}
