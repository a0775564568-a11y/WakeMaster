package com.example.alarm

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.WakeMasterApp
import com.example.data.database.AppDatabase
import com.example.data.model.AlarmEntity
import com.example.ui.ringing.AlarmRingingActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AlarmRingingService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var audioManager: AudioManager? = null
    private var originalAlarmVolume: Int? = null

    private var gradualVolumeJob: Job? = null
    private var autoTimeoutJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private var currentAlarm: AlarmEntity? = null

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_ALARM
        val alarmId = intent?.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L) ?: -1L

        when (action) {
            ACTION_START_ALARM -> {
                handleStartAlarm(alarmId)
            }
            ACTION_STOP_ALARM -> {
                stopAlarmRinging()
                stopSelf()
            }
            ACTION_SNOOZE_ALARM -> {
                val snoozeMinutes = intent?.getIntExtra(EXTRA_SNOOZE_MINUTES, 5) ?: 5
                if (alarmId > 0) {
                    AlarmScheduler(this).scheduleSnooze(alarmId, snoozeMinutes)
                }
                stopAlarmRinging()
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun handleStartAlarm(alarmId: Long) {
        serviceScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val alarm = if (alarmId > 0) db.alarmDao().getAlarmById(alarmId) else null
            currentAlarm = alarm ?: AlarmEntity(
                id = if (alarmId > 0) alarmId else 9999L,
                hour = 8,
                minute = 0,
                label = "שעון מעורר פעיל"
            )

            // 1. Show Foreground Notification with Full Screen Intent
            val notification = buildAlarmNotification(currentAlarm!!)
            startForeground(NOTIFICATION_ID, notification)

            // 2. Launch full-screen ringing activity directly
            launchRingingActivity(currentAlarm!!)

            // 3. Start audio playback and vibration
            startRingtone(currentAlarm!!)
            startVibration(currentAlarm!!)

            // 4. Start auto-timeout watchdog
            val maxMinutes = currentAlarm!!.maxRingingMinutes.coerceIn(1, 60)
            autoTimeoutJob?.cancel()
            autoTimeoutJob = serviceScope.launch {
                delay(maxMinutes * 60 * 1000L)
                Log.d(TAG, "Alarm reached maximum duration ($maxMinutes min). Auto stopping.")
                stopAlarmRinging()
                stopSelf()
            }

            // 5. If it's a one-time alarm (daysOfWeek is empty), turn it off in DB
            if (alarm != null && alarm.daysOfWeek.isEmpty()) {
                db.alarmDao().setEnabled(alarm.id, false)
            }
        }
    }

    private fun buildAlarmNotification(alarm: AlarmEntity): Notification {
        val fullScreenIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarm.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarm.id.toInt() + 200000,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, WakeMasterApp.ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.wakemaster_launcher_icon_1792398928259)
            .setContentTitle("WakeMaster: ${alarm.label}")
            .setContentText("השעה ${alarm.formatTime()} - לחץ לביצוע משימת ההשכמה!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .build()
    }

    private fun launchRingingActivity(alarm: AlarmEntity) {
        val activityIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarm.id)
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            )
        }
        try {
            startActivity(activityIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting AlarmRingingActivity: ${e.message}")
        }
    }

    private fun startRingtone(alarm: AlarmEntity) {
        try {
            // Save current system alarm volume to restore later
            audioManager?.let { am ->
                if (originalAlarmVolume == null) {
                    originalAlarmVolume = am.getStreamVolume(AudioManager.STREAM_ALARM)
                }
                val maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                // Set target stream volume according to user setting
                val targetStreamVol = (maxVolume * alarm.volume).toInt().coerceIn(1, maxVolume)
                try {
                    am.setStreamVolume(AudioManager.STREAM_ALARM, targetStreamVol, 0)
                } catch (e: SecurityException) {
                    Log.w(TAG, "Could not set stream volume directly: ${e.message}")
                }
            }

            val ringtoneUri = if (alarm.ringtoneUri == "DEFAULT" || alarm.ringtoneUri.isBlank()) {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            } else {
                Uri.parse(alarm.ringtoneUri)
            }

            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(applicationContext, ringtoneUri)
                isLooping = true
                prepare()
            }

            if (alarm.gradualVolume) {
                // Crescendo: Start at 5% and ramp up to 100% over 30 seconds
                mediaPlayer?.setVolume(0.05f, 0.05f)
                mediaPlayer?.start()

                gradualVolumeJob?.cancel()
                gradualVolumeJob = serviceScope.launch {
                    val steps = 30
                    for (i in 1..steps) {
                        delay(1000)
                        if (!isActive) break
                        val currentLevel = 0.05f + (0.95f * (i.toFloat() / steps.toFloat()))
                        mediaPlayer?.setVolume(currentLevel, currentLevel)
                    }
                }
            } else {
                mediaPlayer?.setVolume(1.0f, 1.0f)
                mediaPlayer?.start()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error playing ringtone: ${e.message}", e)
        }
    }

    private fun startVibration(alarm: AlarmEntity) {
        if (!alarm.vibrate) return
        val vib = vibrator ?: return

        try {
            val pattern: LongArray = when (alarm.vibrationPattern) {
                "HEAVY" -> longArrayOf(0, 1000, 200, 1000, 200, 1000)
                "PULSE" -> longArrayOf(0, 300, 200, 300, 200, 300, 500)
                "ESCALATING" -> longArrayOf(0, 200, 300, 400, 200, 800, 100)
                else -> longArrayOf(0, 600, 400, 600, 400, 800) // STANDARD
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(pattern, 0)
                vib.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting vibration: ${e.message}")
        }
    }

    private fun stopAlarmRinging() {
        gradualVolumeJob?.cancel()
        autoTimeoutJob?.cancel()

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping media player: ${e.message}")
        }

        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping vibrator: ${e.message}")
        }

        // Restore original audio volume if we modified it
        try {
            originalAlarmVolume?.let { original ->
                audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, original, 0)
            }
        } catch (_: Exception) {}

        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() {
        stopAlarmRinging()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "AlarmRingingService"
        const val NOTIFICATION_ID = 4004
        const val ACTION_START_ALARM = "com.example.wakemaster.START_ALARM"
        const val ACTION_STOP_ALARM = "com.example.wakemaster.STOP_ALARM"
        const val ACTION_SNOOZE_ALARM = "com.example.wakemaster.SNOOZE_ALARM"
        const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"
    }
}
