package com.example.ui.settings

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin.DeviceOwnerHelper
import com.example.data.preferences.AppSettings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    appSettings: AppSettings,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentTheme by remember { mutableStateOf("SYSTEM") }
    var isDeviceOwner by remember { mutableStateOf(false) }
    var isDeviceAdmin by remember { mutableStateOf(false) }
    var isUninstallProtectionActive by remember { mutableStateOf(false) }

    // System Permissions status
    var canExactAlarm by remember { mutableStateOf(true) }
    var areNotificationsEnabled by remember { mutableStateOf(true) }
    var isBatteryOptimized by remember { mutableStateOf(false) }

    fun refreshStatus() {
        isDeviceOwner = DeviceOwnerHelper.isDeviceOwner(context)
        isDeviceAdmin = DeviceOwnerHelper.isDeviceAdmin(context)
        isUninstallProtectionActive = DeviceOwnerHelper.isUninstallBlocked(context)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        canExactAlarm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else true

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        areNotificationsEnabled = nm.areNotificationsEnabled()

        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        isBatteryOptimized = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            !pm.isIgnoringBatteryOptimizations(context.packageName)
        } else false
    }

    LaunchedEffect(Unit) {
        refreshStatus()
        scope.launch {
            appSettings.appTheme.collect { currentTheme = it }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("הגדרות WakeMaster", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "חזרה")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Theme Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ערכת נושא",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val themes = listOf(
                            "SYSTEM" to "מערכת",
                            "LIGHT" to "בהיר",
                            "DARK" to "כהה"
                        )
                        themes.forEach { (tKey, tLabel) ->
                            FilterChip(
                                selected = currentTheme == tKey,
                                onClick = {
                                    currentTheme = tKey
                                    scope.launch { appSettings.setAppTheme(tKey) }
                                },
                                label = { Text(tLabel, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            // 2. Permissions Center
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "בדיקת הרשאות מערכת",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Exact Alarms
                    PermissionItemRow(
                        title = "התראות מדויקות (Exact Alarm)",
                        description = "נדרש כדי שהשעון יצלצל בדיוק בדקה שנקבעה",
                        isGranted = canExactAlarm,
                        onAction = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Notifications
                    PermissionItemRow(
                        title = "התראות (Notifications)",
                        description = "נדרש להצגת מסך השעון וההתרעה",
                        isGranted = areNotificationsEnabled,
                        onAction = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Battery Optimization
                    PermissionItemRow(
                        title = "החרגה מאופטימיזציית סוללה",
                        description = "מונע ממערכת ההפעלה להרדים את השעון במצב Doze",
                        isGranted = !isBatteryOptimized,
                        onAction = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            }
                        }
                    )
                }
            }

            // 3. Lock Task Mode & Enterprise Device Owner (Level B)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "מצב נעילת משימה וניהול מכשיר",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "• רמה א׳ (מכשיר רגיל): מופעל אוטומטית בכל מכשיר. מציג את השעון מעל מסך הנעילה, שומר על מסך דלוק וחוסם כפתור חזרה.\n\n• רמה ב׳ (מכשיר מנוהל - Device Owner): מאפשר נעילה הרמטית לחלוטין (Lock Task Mode) שבה מערכת ההפעלה מונעת מעבר לאפליקציות אחרות.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "סטטוס Device Owner:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isDeviceOwner) Color(0xFF10B981).copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isDeviceOwner) "פעיל ומאושר" else "לא מוגדר (רגיל)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDeviceOwner) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    if (!isDeviceOwner) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "פקודת ADB להגדרת Device Owner למכשירים מנוהלים:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val cmd = DeviceOwnerHelper.getDeviceOwnerSetupAdbCommand(context.packageName)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable {
                                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cb.setPrimaryClip(ClipData.newPlainText("ADB Command", cmd))
                                    Toast.makeText(context, "פקודת ה-ADB הועתקה ללוח", Toast.LENGTH_SHORT).show()
                                }
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = cmd,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.ContentCopy, contentDescription = "העתק", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // 4. Uninstall Protection (הגנה מפני הסרת התקנה)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "הגנה מפני הסרת התקנה",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "במכשירים מנוהלים כ-Device Owner, ניתן למנוע הסרת התקנה באמצעות ממשק Android הרשמי (setUninstallBlocked). במכשיר רגיל, Android אינו מאפשר חסימת הסרה.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "חסימת הסרת התקנה:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = isUninstallProtectionActive,
                            enabled = isDeviceOwner,
                            onCheckedChange = { enable ->
                                val success = DeviceOwnerHelper.setUninstallBlocked(context, enable)
                                if (success) {
                                    isUninstallProtectionActive = enable
                                    Toast.makeText(
                                        context,
                                        if (enable) "הגנה מפני הסרה הופעלה" else "הגנה מפני הסרה בוטלה",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(context, "אפשרות זו דורשת הגדרת Device Owner", Toast.LENGTH_LONG).show()
                                }
                            }
                        )
                    }

                    if (!isDeviceOwner) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "זמין רק כאשר המכשיר מוגדר כ-Device Owner",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PermissionItemRow(
    title: String,
    description: String,
    isGranted: Boolean,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isGranted) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedButton(
            onClick = onAction,
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(if (isGranted) "מאושר" else "הגדר", style = MaterialTheme.typography.labelSmall)
        }
    }
}
