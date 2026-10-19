package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import androidx.core.content.ContextCompat

class WakeMasterAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
        val isSnooze = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_SNOOZE, false)
        Log.d(TAG, "Alarm triggered! AlarmId: $alarmId, isSnooze: $isSnooze")

        // Acquire a temporary wake lock for 15 seconds to ensure the service can start cleanly
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "WakeMaster:AlarmTriggerWakeLock"
        )
        wakeLock?.acquire(15_000L)

        // Start Foreground Service
        val serviceIntent = Intent(context, AlarmRingingService::class.java).apply {
            action = AlarmRingingService.ACTION_START_ALARM
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_IS_SNOOZE, isSnooze)
        }

        try {
            ContextCompat.startForegroundService(context, serviceIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting AlarmRingingService: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "WakeMasterAlarmReceiver"
    }
}
