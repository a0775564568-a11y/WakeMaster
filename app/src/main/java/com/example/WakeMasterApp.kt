package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.example.data.database.AppDatabase
import com.example.data.preferences.AppSettings

class WakeMasterApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val appSettings: AppSettings by lazy { AppSettings(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createAlarmNotificationChannel()
    }

    private fun createAlarmNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                ALARM_CHANNEL_ID,
                getString(R.string.alarm_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.alarm_channel_description)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 800)
                setSound(Settings.System.DEFAULT_ALARM_ALERT_URI, audioAttributes)
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val ALARM_CHANNEL_ID = "wakemaster_alarm_channel_v1"
        lateinit var instance: WakeMasterApp
            private set
    }
}
