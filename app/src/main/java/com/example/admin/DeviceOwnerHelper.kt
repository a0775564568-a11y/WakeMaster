package com.example.admin

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

object DeviceOwnerHelper {
    private const val TAG = "DeviceOwnerHelper"

    fun getAdminComponent(context: Context): ComponentName {
        return ComponentName(context, WakeMasterDeviceAdminReceiver::class.java)
    }

    fun isDeviceAdmin(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            ?: return false
        return dpm.isAdminActive(getAdminComponent(context))
    }

    fun isDeviceOwner(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            ?: return false
        return dpm.isDeviceOwnerApp(context.packageName)
    }

    fun requestDeviceAdminIntent(context: Context): Intent {
        return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, getAdminComponent(context))
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "הפעלת מנהל מכשיר עבור WakeMaster מאפשרת אינטגרציה עם מדיניות נעילה והגנה מפני הסרה במכשירים מנוהלים."
            )
        }
    }

    /**
     * Level B setup: Configures the app package for LockTask mode so that
     * calling startLockTask() will pin the app without showing the standard system exit prompt.
     */
    fun setupLockTaskPackages(context: Context): Boolean {
        if (!isDeviceOwner(context)) return false
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            ?: return false
        val admin = getAdminComponent(context)
        return try {
            dpm.setLockTaskPackages(admin, arrayOf(context.packageName))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Allow emergency information and system info while in LockTask mode for safety
                dpm.setLockTaskFeatures(
                    admin,
                    DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO or
                    DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS
                )
            }
            Log.d(TAG, "Successfully configured LockTaskPackages for ${context.packageName}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error setting lock task packages: ${e.message}")
            false
        }
    }

    /**
     * Level B setup: Configures uninstall blocking via DevicePolicyManager.setUninstallBlocked.
     * Can only be called by a Device Owner or Profile Owner.
     */
    fun setUninstallBlocked(context: Context, blocked: Boolean): Boolean {
        if (!isDeviceOwner(context)) return false
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            ?: return false
        val admin = getAdminComponent(context)
        return try {
            dpm.setUninstallBlocked(admin, context.packageName, blocked)
            Log.d(TAG, "Set uninstall blocked to $blocked")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error setting uninstall blocked: ${e.message}")
            false
        }
    }

    fun isUninstallBlocked(context: Context): Boolean {
        if (!isDeviceOwner(context)) return false
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            ?: return false
        val admin = getAdminComponent(context)
        return try {
            dpm.isUninstallBlocked(admin, context.packageName)
        } catch (e: Exception) {
            false
        }
    }

    fun getDeviceOwnerSetupAdbCommand(packageName: String): String {
        return "adb shell dpm set-device-owner $packageName/com.example.admin.WakeMasterDeviceAdminReceiver"
    }
}
