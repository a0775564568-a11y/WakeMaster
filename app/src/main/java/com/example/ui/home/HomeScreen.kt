package com.example.ui.home

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm.AlarmRingingService
import com.example.alarm.AlarmScheduler
import com.example.data.model.AlarmEntity
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    alarms: List<AlarmEntity>,
    onToggleAlarm: (AlarmEntity, Boolean) -> Unit,
    onEditAlarm: (AlarmEntity) -> Unit,
    onDeleteAlarm: (AlarmEntity) -> Unit,
    onCreateAlarm: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPractice: () -> Unit,
    onOpenWizard: () -> Unit
) {
    val context = LocalContext.current

    // Find the next upcoming alarm
    val nextAlarmInfo = remember(alarms) {
        val active = alarms.filter { it.isEnabled }
        if (active.isEmpty()) null
        else {
            val closest = active.map { alarm ->
                val nextMillis = AlarmScheduler.calculateNextTriggerMillis(
                    alarm.hour, alarm.minute, alarm.daysOfWeek
                )
                alarm to nextMillis
            }.minByOrNull { it.second }
            closest
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = "WakeMaster",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "WakeMaster",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenPractice,
                        modifier = Modifier.testTag("practice_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "תרגול משימות",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "הגדרות",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateAlarm,
                icon = { Icon(Icons.Default.Add, contentDescription = "הוסף שעון") },
                text = { Text("שעון חדש", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("create_alarm_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Next upcoming alarm card
                NextAlarmCard(nextAlarmInfo = nextAlarmInfo)
            }

            item {
                // Quick Test Actions Card
                QuickActionsCard(
                    onTestNow = {
                        // Triggers a 5-second test alarm
                        val scheduler = AlarmScheduler(context)
                        scheduler.scheduleSnooze(alarmId = 999L, snoozeMinutes = 0)
                        val testIntent = Intent(context, AlarmRingingService::class.java).apply {
                            action = AlarmRingingService.ACTION_START_ALARM
                            putExtra(AlarmScheduler.EXTRA_ALARM_ID, 999L)
                        }
                        try {
                            context.startService(testIntent)
                        } catch (_: Exception) {}
                    },
                    onOpenPractice = onOpenPractice
                )
            }

            if (alarms.isEmpty()) {
                item {
                    EmptyAlarmsState(onCreateAlarm = onCreateAlarm)
                }
            } else {
                items(alarms, key = { it.id }) { alarm ->
                    AlarmItemCard(
                        alarm = alarm,
                        onToggle = { isChecked -> onToggleAlarm(alarm, isChecked) },
                        onEdit = { onEditAlarm(alarm) },
                        onDelete = { onDeleteAlarm(alarm) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun NextAlarmCard(nextAlarmInfo: Pair<AlarmEntity, Long>?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AlarmOn,
                    contentDescription = "שעון פעיל",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = if (nextAlarmInfo != null) "השעון הבא" else "אין שעונים פעילים",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = if (nextAlarmInfo != null) {
                        AlarmScheduler.getTimeUntilNextAlarmHebrew(nextAlarmInfo.second)
                    } else {
                        "הפעל שעון מעורר לקבלת השכמה מובטחת"
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (nextAlarmInfo != null) {
                    Text(
                        text = "${nextAlarmInfo.first.label} • ${nextAlarmInfo.first.formatTime()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsCard(
    onTestNow: () -> Unit,
    onOpenPractice: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onTestNow,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("test_alarm_button")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "בדוק צלצול", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("בדוק צלצול עכשיו", style = MaterialTheme.typography.labelMedium)
            }

            OutlinedButton(
                onClick = onOpenPractice,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("practice_missions_chip")
            ) {
                Icon(imageVector = Icons.Default.Psychology, contentDescription = "תרגול משימות", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("תרגול משימות", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun AlarmItemCard(
    alarm: AlarmEntity,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onEdit() }
            .testTag("alarm_card_${alarm.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isEnabled) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (alarm.isEnabled) 3.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = alarm.formatTime(),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (alarm.isEnabled) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        text = alarm.label.ifBlank { "שעון מעורר" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (alarm.isEnabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }

                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("alarm_switch_${alarm.id}")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges row: Days, Mission Type, Difficulty, Lock Task
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Days badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = alarm.getDaysSummaryHebrew(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Mission badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${alarm.getMissionNameHebrew()} (${alarm.getDifficultyHebrew()})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Lock Task Mode icon badge
                if (alarm.lockTaskMode) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "נעילת משימה",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "נעילה",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_alarm_${alarm.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "מחק שעון",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyAlarmsState(onCreateAlarm: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "אין עדיין שעונים מעוררים",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "לחץ על הכפתור למטה כדי להגדיר שעון עם משימת השכמה",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
