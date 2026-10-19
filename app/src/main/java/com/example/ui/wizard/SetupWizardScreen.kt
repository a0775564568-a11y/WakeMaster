package com.example.ui.wizard

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SetupWizardScreen(
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    var canExactAlarm by remember { mutableStateOf(true) }
    var areNotificationsEnabled by remember { mutableStateOf(true) }
    var isBatteryOptimized by remember { mutableStateOf(false) }

    fun refresh() {
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
        refresh()
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ברוכים הבאים ל-WakeMaster!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "כדי שהשעון המעורר יצלצל בדיוק מירבי, יציג את משימת ההשכמה מעל מסך הנעילה ולא יורדם על ידי מערכת ההפעלה, יש לאשר את ההגדרות הבאות:",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 1. Exact Alarm
            WizardPermissionCard(
                icon = Icons.Default.Alarm,
                title = "1. תזמון התראות מדויקות",
                explanation = "מאפשר ל-WakeMaster להעיר אותך בדיוק של שנייה, גם כאשר הטלפון במצב חיסכון בסוללה.",
                isGranted = canExactAlarm,
                buttonText = "אשר התראות מדויקות",
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Notifications
            WizardPermissionCard(
                icon = Icons.Default.Notifications,
                title = "2. הצגת התראות ומסך השכמה",
                explanation = "נדרש כדי להציג את מסך המשימה מעל מסך הנעילה כאשר השעון מצלצל.",
                isGranted = areNotificationsEnabled,
                buttonText = "אשר התראות",
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Battery Saver Exemption
            WizardPermissionCard(
                icon = Icons.Default.BatteryChargingFull,
                title = "3. החרגה מחיסכון סוללה (Doze)",
                explanation = "מבטיח שהמכשיר לא יעצור את שירות השעון המעורר במהלך הלילה.",
                isGranted = !isBatteryOptimized,
                buttonText = "החרג מחיסכון סוללה",
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    }
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("wizard_finish_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("התחל להשתמש ב-WakeMaster", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun WizardPermissionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    explanation: String,
    isGranted: Boolean,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) Color(0xFF10B981).copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isGranted) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isGranted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "מאושר",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!isGranted) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(buttonText, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
