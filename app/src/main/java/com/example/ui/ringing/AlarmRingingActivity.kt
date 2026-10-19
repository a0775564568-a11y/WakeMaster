package com.example.ui.ringing

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin.DeviceOwnerHelper
import com.example.alarm.AlarmRingingService
import com.example.alarm.AlarmScheduler
import com.example.data.database.AppDatabase
import com.example.data.model.AlarmEntity
import com.example.missions.MissionGenerators
import com.example.missions.MissionState
import com.example.ui.missions_ui.MathMissionScreen
import com.example.ui.missions_ui.MemoryMissionScreen
import com.example.ui.missions_ui.PuzzleMissionScreen
import com.example.ui.missions_ui.SequenceMissionScreen
import com.example.ui.missions_ui.ShapesMissionScreen
import com.example.ui.missions_ui.TypingMissionScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmRingingActivity : ComponentActivity() {

    private var alarmId: Long = -1L
    private var isLockTaskActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Turn on screen and show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)

        // Disable standard system back button to prevent accidental dismiss (Level A Lock)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Toast.makeText(
                    this@AlarmRingingActivity,
                    "יש להשלים את משימת ההשכמה לכיבוי השעון",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AlarmRingingContent(
                        alarmId = alarmId,
                        onStartLockTask = { tryEnterLockTask() },
                        onMissionCompleted = { finishAlarmAndDismiss() },
                        onSnooze = { minutes -> handleSnooze(minutes) },
                        onEmergencyStop = { handleEmergencyStop() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure screen stays on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun tryEnterLockTask() {
        if (DeviceOwnerHelper.isDeviceOwner(this)) {
            try {
                val am = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                if (am?.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE) {
                    startLockTask()
                    isLockTaskActive = true
                }
            } catch (e: Exception) {
                // Not whitelisted or permission denied
            }
        }
    }

    private fun safeExitLockTask() {
        if (isLockTaskActive) {
            try {
                stopLockTask()
                isLockTaskActive = false
            } catch (_: Exception) {}
        }
    }

    private fun finishAlarmAndDismiss() {
        safeExitLockTask()
        // Stop foreground service
        val stopIntent = Intent(this, AlarmRingingService::class.java).apply {
            action = AlarmRingingService.ACTION_STOP_ALARM
        }
        startService(stopIntent)
        finishAndRemoveTask()
    }

    private fun handleSnooze(minutes: Int) {
        safeExitLockTask()
        val snoozeIntent = Intent(this, AlarmRingingService::class.java).apply {
            action = AlarmRingingService.ACTION_SNOOZE_ALARM
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmRingingService.EXTRA_SNOOZE_MINUTES, minutes)
        }
        startService(snoozeIntent)
        finishAndRemoveTask()
    }

    private fun handleEmergencyStop() {
        Toast.makeText(this, "עצירת חירום בוצעה", Toast.LENGTH_LONG).show()
        finishAlarmAndDismiss()
    }

    override fun onDestroy() {
        safeExitLockTask()
        super.onDestroy()
    }
}

@Composable
fun AlarmRingingContent(
    alarmId: Long,
    onStartLockTask: () -> Unit,
    onMissionCompleted: () -> Unit,
    onSnooze: (Int) -> Unit,
    onEmergencyStop: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var alarm by remember { mutableStateOf<AlarmEntity?>(null) }
    var currentMissionIndex by remember { mutableIntStateOf(0) }
    var totalMissions by remember { mutableIntStateOf(1) }
    var activeMissionState by remember { mutableStateOf<MissionState?>(null) }
    var isMissionSolvedAnimation by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Real-time clock display
    var currentTimeString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        while (true) {
            currentTimeString = timeFormat.format(Date())
            delay(1000)
        }
    }

    // Load alarm configuration
    LaunchedEffect(alarmId) {
        val db = AppDatabase.getDatabase(context)
        val loaded = if (alarmId > 0) db.alarmDao().getAlarmById(alarmId) else null
        val effective = loaded ?: AlarmEntity(
            id = if (alarmId > 0) alarmId else 9999L,
            hour = 7,
            minute = 0,
            label = "שעון מעורר",
            missionType = "MATH",
            missionDifficulty = "MEDIUM",
            missionCount = 1,
            lockTaskMode = true
        )
        alarm = effective
        totalMissions = effective.missionCount.coerceIn(1, 5)

        if (effective.lockTaskMode) {
            onStartLockTask()
        }

        activeMissionState = generateMission(effective.missionType, effective.missionDifficulty)
    }

    fun onSingleMissionFinished() {
        if (currentMissionIndex + 1 < totalMissions) {
            // Next mission in sequence
            isMissionSolvedAnimation = true
            scope.launch {
                delay(600)
                currentMissionIndex++
                val eff = alarm ?: return@launch
                activeMissionState = generateMission(eff.missionType, eff.missionDifficulty)
                isMissionSolvedAnimation = false
            }
        } else {
            // All missions completed!
            isMissionSolvedAnimation = true
            scope.launch {
                delay(800)
                onMissionCompleted()
            }
        }
    }

    val currentAlarm = alarm

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Alarm Clock Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = "שעון מעורר",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Column {
                    Text(
                        text = currentAlarm?.label ?: "WakeMaster",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (currentAlarm?.lockTaskMode == true) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "נעילה פעילה",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = "מצב נעילת משימה פעיל",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Large Digital Clock
            Text(
                text = currentTimeString.ifBlank { currentAlarm?.formatTime() ?: "--:--" },
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 68.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Missions step counter
            if (totalMissions > 1) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "משימה ${currentMissionIndex + 1} מתוך $totalMissions",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Interactive Mission Section
            AnimatedVisibility(
                visible = !isMissionSolvedAnimation,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    when (val state = activeMissionState) {
                        is MissionState.Math -> {
                            MathMissionScreen(state = state, onCompleted = { onSingleMissionFinished() })
                        }
                        is MissionState.Puzzle -> {
                            PuzzleMissionScreen(state = state, onCompleted = { onSingleMissionFinished() })
                        }
                        is MissionState.Memory -> {
                            MemoryMissionScreen(state = state, onCompleted = { onSingleMissionFinished() })
                        }
                        is MissionState.Sequence -> {
                            SequenceMissionScreen(state = state, onCompleted = { onSingleMissionFinished() })
                        }
                        is MissionState.Shapes -> {
                            ShapesMissionScreen(state = state, onCompleted = { onSingleMissionFinished() })
                        }
                        is MissionState.Typing -> {
                            TypingMissionScreen(state = state, onCompleted = { onSingleMissionFinished() })
                        }
                        null -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("טוען משימת השכמה...")
                            }
                        }
                    }
                }
            }

            if (isMissionSolvedAnimation) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "הושלם",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (currentMissionIndex + 1 >= totalMissions) "כל הכבוד! בוקר טוב!" else "מצוין! עובר למשימה הבאה...",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Snooze Button (if enabled)
            if (currentAlarm?.snoozeEnabled == true) {
                OutlinedButton(
                    onClick = { onSnooze(currentAlarm.snoozeMinutes) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(52.dp)
                        .testTag("snooze_alarm_button")
                ) {
                    Icon(imageVector = Icons.Default.Snooze, contentDescription = "נודניק")
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "נודניק (${currentAlarm.snoozeMinutes} דקות)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Emergency Stop (עצירת חירום) - with 5-second long press
            if (currentAlarm?.emergencyStopAllowed != false) {
                EmergencyStopButton(onConfirmed = onEmergencyStop)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun EmergencyStopButton(onConfirmed: () -> Unit) {
    var holdProgress by remember { mutableFloatStateOf(0f) }
    var isHolding by remember { mutableStateOf(false) }

    LaunchedEffect(isHolding) {
        if (isHolding) {
            val step = 0.05f
            while (isHolding && holdProgress < 1f) {
                delay(150)
                holdProgress += step
            }
            if (holdProgress >= 1f) {
                onConfirmed()
            }
        } else {
            holdProgress = 0f
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(0.88f)
            .clip(RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isHolding = true
                        tryAwaitRelease()
                        isHolding = false
                    }
                )
            }
            .testTag("emergency_stop_container"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "עצירת חירום",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "עצירת חירום (לחץ והחזק 3 שניות)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            if (holdProgress > 0f) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { holdProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

fun generateMission(missionType: String, difficulty: String): MissionState {
    val effectiveType = if (missionType == "RANDOM") {
        listOf("MATH", "PUZZLE", "MEMORY", "SEQUENCE", "SHAPES", "TYPING").random()
    } else {
        missionType
    }

    return when (effectiveType) {
        "MATH" -> MissionGenerators.generateMath(difficulty)
        "PUZZLE" -> MissionGenerators.generatePuzzle(difficulty)
        "MEMORY" -> MissionGenerators.generateMemory(difficulty)
        "SEQUENCE" -> MissionGenerators.generateSequence(difficulty)
        "SHAPES" -> MissionGenerators.generateShapes(difficulty)
        "TYPING" -> MissionGenerators.generateTyping(difficulty)
        else -> MissionGenerators.generateMath(difficulty)
    }
}
