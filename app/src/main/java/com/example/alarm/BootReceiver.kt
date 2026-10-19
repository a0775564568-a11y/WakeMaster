package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "Received system broadcast: $action. Rescheduling active alarms...")
        val scheduler = AlarmScheduler(context)
        scheduler.rescheduleAllActiveAlarms()
    }

    companion object {
        private const val TAG = "WakeMasterBootReceiver"
    }
}
