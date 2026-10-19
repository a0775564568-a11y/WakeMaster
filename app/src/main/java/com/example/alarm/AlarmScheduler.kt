package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.MainActivity
import com.example.data.database.AppDatabase
import com.example.data.model.AlarmEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(alarm: AlarmEntity) {
        if (!alarm.isEnabled) {
            cancelAlarm(alarm)
            return
        }

        val triggerTime = calculateNextTriggerMillis(alarm.hour, alarm.minute, alarm.daysOfWeek)

        val intent = Intent(context, WakeMasterAlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_ALARM
            putExtra(EXTRA_ALARM_ID, alarm.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // PendingIntent to show MainActivity when user clicks the alarm clock icon in lockscreen/status bar
        val showIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            alarm.id.toInt() + 100000,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            }
            Log.d(TAG, "Alarm scheduled for ID ${alarm.id} at $triggerTime")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while scheduling exact alarm: ${e.message}")
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to schedule alarm fallback: ${e2.message}")
            }
        }
    }

    fun scheduleSnooze(alarmId: Long, snoozeMinutes: Int) {
        val triggerTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)

        val intent = Intent(context, WakeMasterAlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_ALARM
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_IS_SNOOZE, true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            alarmId.toInt() + 100000,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (_: Exception) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(alarm: AlarmEntity) {
        val intent = Intent(context, WakeMasterAlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_ALARM
            putExtra(EXTRA_ALARM_ID, alarm.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun rescheduleAllActiveAlarms() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val activeAlarms = db.alarmDao().getActiveAlarmsList()
            for (alarm in activeAlarms) {
                scheduleAlarm(alarm)
            }
        }
    }

    companion object {
        const val TAG = "AlarmScheduler"
        const val ACTION_TRIGGER_ALARM = "com.example.wakemaster.TRIGGER_ALARM"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_IS_SNOOZE = "extra_is_snooze"

        fun calculateNextTriggerMillis(hour: Int, minute: Int, daysOfWeek: List<Int>): Long {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (daysOfWeek.isEmpty()) {
                // One-time alarm
                if (target.timeInMillis <= now.timeInMillis) {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                }
                return target.timeInMillis
            }

            // Recurring alarm on specific days of week
            // Note: Calendar.SUNDAY = 1, MONDAY = 2 ... SATURDAY = 7
            for (offset in 0..7) {
                val candidate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, offset)
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val candidateDayOfWeek = candidate.get(Calendar.DAY_OF_WEEK)
                if (daysOfWeek.contains(candidateDayOfWeek)) {
                    if (candidate.timeInMillis > now.timeInMillis) {
                        return candidate.timeInMillis
                    }
                }
            }

            // Fallback: 1 day later
            target.add(Calendar.DAY_OF_YEAR, 1)
            return target.timeInMillis
        }

        fun getTimeUntilNextAlarmHebrew(targetMillis: Long): String {
            val diff = targetMillis - System.currentTimeMillis()
            if (diff <= 0) return "עכשיו"
            val totalMinutes = diff / (1000 * 60)
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            return when {
                hours > 0 && minutes > 0 -> "בעוד $hours שעות ו-$minutes דקות"
                hours > 0 -> "בעוד $hours שעות"
                minutes > 0 -> "בעוד $minutes דקות"
                else -> "בעוד פחות מדקה"
            }
        }
    }
}
