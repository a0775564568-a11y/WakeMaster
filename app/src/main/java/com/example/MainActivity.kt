package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.alarm.AlarmScheduler
import com.example.data.database.AppDatabase
import com.example.data.model.AlarmEntity
import com.example.data.preferences.AppSettings
import com.example.ui.edit.AlarmEditScreen
import com.example.ui.home.HomeScreen
import com.example.ui.practice.PracticeMissionsScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.wizard.SetupWizardScreen
import kotlinx.coroutines.launch

enum class ScreenState {
    HOME,
    EDIT,
    SETTINGS,
    PRACTICE,
    WIZARD
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as WakeMasterApp
        val database = app.database
        val alarmDao = database.alarmDao()
        val appSettings = app.appSettings
        val alarmScheduler = AlarmScheduler(this)

        setContent {
            val scope = rememberCoroutineScope()
            val alarms by alarmDao.getAllAlarms().collectAsStateWithLifecycle(initialValue = emptyList())
            val isWizardDone by appSettings.isSetupWizardCompleted.collectAsStateWithLifecycle(initialValue = true)
            val appThemeSetting by appSettings.appTheme.collectAsStateWithLifecycle(initialValue = "SYSTEM")

            val isDarkTheme = when (appThemeSetting) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            var currentScreen by remember { mutableStateOf(ScreenState.HOME) }
            var editingAlarm by remember { mutableStateOf<AlarmEntity?>(null) }

            // Request POST_NOTIFICATIONS permission on Android 13+
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { /* permission callback handled */ }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            // Launch setup wizard if not done yet
            LaunchedEffect(isWizardDone) {
                if (!isWizardDone && currentScreen == ScreenState.HOME) {
                    currentScreen = ScreenState.WIZARD
                }
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                // RTL Support for entire app interface
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (currentScreen) {
                            ScreenState.HOME -> {
                                HomeScreen(
                                    alarms = alarms,
                                    onToggleAlarm = { alarm, isChecked ->
                                        scope.launch {
                                            val updated = alarm.copy(isEnabled = isChecked)
                                            alarmDao.update(updated)
                                            if (isChecked) {
                                                alarmScheduler.scheduleAlarm(updated)
                                            } else {
                                                alarmScheduler.cancelAlarm(updated)
                                            }
                                        }
                                    },
                                    onEditAlarm = { alarm ->
                                        editingAlarm = alarm
                                        currentScreen = ScreenState.EDIT
                                    },
                                    onDeleteAlarm = { alarm ->
                                        scope.launch {
                                            alarmScheduler.cancelAlarm(alarm)
                                            alarmDao.delete(alarm)
                                        }
                                    },
                                    onCreateAlarm = {
                                        editingAlarm = null
                                        currentScreen = ScreenState.EDIT
                                    },
                                    onOpenSettings = {
                                        currentScreen = ScreenState.SETTINGS
                                    },
                                    onOpenPractice = {
                                        currentScreen = ScreenState.PRACTICE
                                    },
                                    onOpenWizard = {
                                        currentScreen = ScreenState.WIZARD
                                    }
                                )
                            }

                            ScreenState.EDIT -> {
                                BackHandler {
                                    editingAlarm = null
                                    currentScreen = ScreenState.HOME
                                }
                                AlarmEditScreen(
                                    initialAlarm = editingAlarm,
                                    onSave = { savedAlarm ->
                                        scope.launch {
                                            if (savedAlarm.id == 0L) {
                                                val newId = alarmDao.insert(savedAlarm)
                                                val created = savedAlarm.copy(id = newId)
                                                if (created.isEnabled) {
                                                    alarmScheduler.scheduleAlarm(created)
                                                }
                                            } else {
                                                alarmDao.update(savedAlarm)
                                                if (savedAlarm.isEnabled) {
                                                    alarmScheduler.scheduleAlarm(savedAlarm)
                                                } else {
                                                    alarmScheduler.cancelAlarm(savedAlarm)
                                                }
                                            }
                                            editingAlarm = null
                                            currentScreen = ScreenState.HOME
                                        }
                                    },
                                    onCancel = {
                                        editingAlarm = null
                                        currentScreen = ScreenState.HOME
                                    }
                                )
                            }

                            ScreenState.SETTINGS -> {
                                BackHandler {
                                    currentScreen = ScreenState.HOME
                                }
                                SettingsScreen(
                                    appSettings = appSettings,
                                    onBack = { currentScreen = ScreenState.HOME }
                                )
                            }

                            ScreenState.PRACTICE -> {
                                BackHandler {
                                    currentScreen = ScreenState.HOME
                                }
                                PracticeMissionsScreen(
                                    onBack = { currentScreen = ScreenState.HOME }
                                )
                            }

                            ScreenState.WIZARD -> {
                                BackHandler {
                                    scope.launch {
                                        appSettings.setSetupWizardCompleted(true)
                                    }
                                    currentScreen = ScreenState.HOME
                                }
                                SetupWizardScreen(
                                    onFinish = {
                                        scope.launch {
                                            appSettings.setSetupWizardCompleted(true)
                                        }
                                        currentScreen = ScreenState.HOME
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
