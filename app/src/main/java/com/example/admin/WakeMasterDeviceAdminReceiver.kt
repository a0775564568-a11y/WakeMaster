package com.example.admin

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class WakeMasterDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.d(TAG, "Device Admin enabled for WakeMaster")
        Toast.makeText(context, "הרשאת מנהל מכשיר הופעלה בהצלחה", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.d(TAG, "Device Admin disabled for WakeMaster")
        Toast.makeText(context, "הרשאת מנהל מכשיר בוטלה", Toast.LENGTH_SHORT).show()
    }

    override fun onLockTaskModeEntering(context: Context, intent: Intent, pkg: String) {
        super.onLockTaskModeEntering(context, intent, pkg)
        Log.d(TAG, "Entered Lock Task Mode for package $pkg")
    }

    override fun onLockTaskModeExiting(context: Context, intent: Intent) {
        super.onLockTaskModeExiting(context, intent)
        Log.d(TAG, "Exited Lock Task Mode")
    }

    companion object {
        private const val TAG = "WakeMasterDeviceAdmin"
    }
}
