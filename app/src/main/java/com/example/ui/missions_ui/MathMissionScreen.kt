package com.example.ui.missions_ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.missions.MissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MathMissionScreen(
    state: MissionState.Math,
    onCompleted: () -> Unit
) {
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var isError by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Question Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = "חשבון",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "פתור את התרגיל כדי להשתיק:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.question,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Answer options grid (2x2)
        val chunkedOptions = state.options.chunked(2)
        chunkedOptions.forEach { rowOptions ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowOptions.forEach { option ->
                    val isCurrentSelected = selectedOption == option
                    val backgroundColor by animateColorAsState(
                        targetValue = when {
                            isCurrentSelected && isSuccess -> Color(0xFF10B981) // Green
                            isCurrentSelected && isError -> Color(0xFFEF4444) // Red
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        label = "optionColor"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(84.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(backgroundColor)
                            .clickable(enabled = !isSuccess) {
                                selectedOption = option
                                if (option == state.correctAnswer) {
                                    isSuccess = true
                                    isError = false
                                    scope.launch {
                                        delay(600)
                                        onCompleted()
                                    }
                                } else {
                                    isError = true
                                    scope.launch {
                                        delay(700)
                                        isError = false
                                        selectedOption = null
                                    }
                                }
                            }
                            .testTag("math_option_$option"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = option.toString(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isCurrentSelected && (isSuccess || isError)) Color.White
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isCurrentSelected && isSuccess) {
                                Spacer(modifier = Modifier.size(8.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "נכון",
                                    tint = Color.White
                                )
                            } else if (isCurrentSelected && isError) {
                                Spacer(modifier = Modifier.size(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "שגוי",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
