package com.example.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlarmEntity

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AlarmEditScreen(
    initialAlarm: AlarmEntity?,
    onSave: (AlarmEntity) -> Unit,
    onCancel: () -> Unit
) {
    var hour by remember { mutableIntStateOf(initialAlarm?.hour ?: 7) }
    var minute by remember { mutableIntStateOf(initialAlarm?.minute ?: 0) }
    var label by remember { mutableStateOf(initialAlarm?.label ?: "השכמת בוקר") }
    var selectedDays by remember { mutableStateOf(initialAlarm?.daysOfWeek?.toSet() ?: emptySet()) }

    var missionType by remember { mutableStateOf(initialAlarm?.missionType ?: "MATH") }
    var missionDifficulty by remember { mutableStateOf(initialAlarm?.missionDifficulty ?: "MEDIUM") }
    var missionCount by remember { mutableIntStateOf(initialAlarm?.missionCount ?: 1) }

    var volume by remember { mutableFloatStateOf(initialAlarm?.volume ?: 0.9f) }
    var gradualVolume by remember { mutableStateOf(initialAlarm?.gradualVolume ?: true) }
    var vibrate by remember { mutableStateOf(initialAlarm?.vibrate ?: true) }
    var vibrationPattern by remember { mutableStateOf(initialAlarm?.vibrationPattern ?: "STANDARD") }

    var lockTaskMode by remember { mutableStateOf(initialAlarm?.lockTaskMode ?: true) }
    var snoozeEnabled by remember { mutableStateOf(initialAlarm?.snoozeEnabled ?: true) }
    var snoozeMinutes by remember { mutableIntStateOf(initialAlarm?.snoozeMinutes ?: 5) }
    var maxRingingMinutes by remember { mutableIntStateOf(initialAlarm?.maxRingingMinutes ?: 10) }
    var emergencyStopAllowed by remember { mutableStateOf(initialAlarm?.emergencyStopAllowed ?: true) }

    val timePickerState = rememberTimePickerState(
        initialHour = hour,
        initialMinute = minute,
        is24Hour = true
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (initialAlarm == null) "שעון מעורר חדש" else "עריכת שעון",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel, modifier = Modifier.testTag("back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "חזרה"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            val result = (initialAlarm ?: AlarmEntity(hour = 0, minute = 0)).copy(
                                hour = timePickerState.hour,
                                minute = timePickerState.minute,
                                label = label.ifBlank { "שעון מעורר" },
                                daysOfWeek = selectedDays.toList().sorted(),
                                isEnabled = true,
                                missionType = missionType,
                                missionDifficulty = missionDifficulty,
                                missionCount = missionCount,
                                volume = volume,
                                gradualVolume = gradualVolume,
                                vibrate = vibrate,
                                vibrationPattern = vibrationPattern,
                                lockTaskMode = lockTaskMode,
                                snoozeEnabled = snoozeEnabled,
                                snoozeMinutes = snoozeMinutes,
                                maxRingingMinutes = maxRingingMinutes,
                                emergencyStopAllowed = emergencyStopAllowed
                            )
                            onSave(result)
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("save_alarm_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("שמור", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Time Picker Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
                            selectorColor = MaterialTheme.colorScheme.primary,
                            timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }

            // 2. Alarm Label
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("שם השעון") },
                placeholder = { Text("לדוגמה: השכמה לעבודה") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("alarm_label_input"),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            // 3. Recurring Days Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ימים חוזרים",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val days = listOf(
                        1 to "א׳",
                        2 to "ב׳",
                        3 to "ג׳",
                        4 to "ד׳",
                        5 to "ה׳",
                        6 to "ו׳",
                        7 to "ש׳"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        days.forEach { (dayId, dayName) ->
                            val isSelected = selectedDays.contains(dayId)
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable {
                                        selectedDays = if (isSelected) {
                                            selectedDays - dayId
                                        } else {
                                            selectedDays + dayId
                                        }
                                    }
                                    .testTag("day_chip_$dayId"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayName,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Shortcuts row
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { selectedDays = setOf(1, 2, 3, 4, 5) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ימי חול (א׳-ה׳)", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = { selectedDays = setOf(6, 7) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("סופ\"ש (ו׳-ש׳)", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = { selectedDays = emptySet() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(0.9f)
                        ) {
                            Text("חד-פעמי", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // 4. Wake-Up Mission Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "משימת השכמה לכיבוי השעון",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val missions = listOf(
                        "MATH" to "תרגילי חשבון",
                        "PUZZLE" to "פאזל תמונה",
                        "MEMORY" to "משחק זיכרון",
                        "SEQUENCE" to "סדרות מספרים",
                        "SHAPES" to "התאמת צורות",
                        "TYPING" to "הקלדת משפט",
                        "RANDOM" to "אקראי"
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        missions.forEach { (typeKey, typeLabel) ->
                            FilterChip(
                                selected = missionType == typeKey,
                                onClick = { missionType = typeKey },
                                label = { Text(typeLabel) },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Difficulty selector
                    Text(
                        text = "רמת קושי:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val difficulties = listOf(
                            "EASY" to "קל",
                            "MEDIUM" to "בינוני",
                            "HARD" to "מאתגר"
                        )
                        difficulties.forEach { (diffKey, diffLabel) ->
                            FilterChip(
                                selected = missionDifficulty == diffKey,
                                onClick = { missionDifficulty = diffKey },
                                label = { Text(diffLabel, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Number of consecutive missions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "מספר משימות רצופות: $missionCount",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Slider(
                        value = missionCount.toFloat(),
                        onValueChange = { missionCount = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 3,
                        modifier = Modifier.testTag("mission_count_slider")
                    )
                }
            }

            // 5. Mission Lock Mode (מצב נעילת משימה)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (lockTaskMode) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "מצב נעילת משימה",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "מונע יציאה ומחייב פתרון משימה",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = lockTaskMode,
                            onCheckedChange = { lockTaskMode = it },
                            modifier = Modifier.testTag("lock_mode_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ברמה א׳ (מכשיר רגיל): חוסם כפתור חזרה ומציג מעל מסך הנעילה. ברמה ב׳ (מכשיר מנוהל): מפעיל Lock Task Mode מלא.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 6. Sound & Audio Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "עוצמת שמע וצלצול",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "עוצמת צלצול: ${(volume * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Slider(
                        value = volume,
                        onValueChange = { volume = it },
                        valueRange = 0.1f..1.0f,
                        modifier = Modifier.testTag("volume_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "הגברה הדרגתית (Crescendo)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "העלאת העוצמה בהדרגה על פני 30 שניות",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = gradualVolume,
                            onCheckedChange = { gradualVolume = it },
                            modifier = Modifier.testTag("gradual_volume_switch")
                        )
                    }
                }
            }

            // 7. Vibration Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "רטט",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Switch(
                            checked = vibrate,
                            onCheckedChange = { vibrate = it },
                            modifier = Modifier.testTag("vibrate_switch")
                        )
                    }

                    if (vibrate) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "תבנית רטט:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val patterns = listOf(
                                "STANDARD" to "רגיל",
                                "HEAVY" to "חזק",
                                "PULSE" to "פעימות",
                                "ESCALATING" to "מתגבר"
                            )
                            patterns.forEach { (pKey, pLabel) ->
                                FilterChip(
                                    selected = vibrationPattern == pKey,
                                    onClick = { vibrationPattern = pKey },
                                    label = { Text(pLabel) },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 8. Snooze & Safety Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Snooze,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "אפשר נודניק (Snooze)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Switch(
                            checked = snoozeEnabled,
                            onCheckedChange = { snoozeEnabled = it },
                            modifier = Modifier.testTag("snooze_switch")
                        )
                    }

                    if (snoozeEnabled) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "זמן נודניק: $snoozeMinutes דקות",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Slider(
                            value = snoozeMinutes.toFloat(),
                            onValueChange = { snoozeMinutes = it.toInt() },
                            valueRange = 3f..20f,
                            steps = 16
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "משך צלצול מרבי: $maxRingingMinutes דקות",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = maxRingingMinutes.toFloat(),
                        onValueChange = { maxRingingMinutes = it.toInt() },
                        valueRange = 1f..30f,
                        steps = 28
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "אפשר עצירת חירום",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "מאפשר יציאה בטוחה בלחיצה ממושכת של 3 שניות",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = emergencyStopAllowed,
                            onCheckedChange = { emergencyStopAllowed = it }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
