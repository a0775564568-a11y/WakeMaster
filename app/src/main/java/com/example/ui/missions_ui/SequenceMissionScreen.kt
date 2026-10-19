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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
fun SequenceMissionScreen(
    state: MissionState.Sequence,
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
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = "סדרות מספרים",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "מהו המספר הבא בסדרה?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.hintHebrew,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sequence Cards Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    state.sequenceDisplay.forEach { item ->
                        val isTarget = item == "?"
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isTarget) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surface
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp
                                ),
                                color = if (isTarget) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Options 2x2
        val chunked = state.options.chunked(2)
        chunked.forEach { rowOptions ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowOptions.forEach { option ->
                    val isSelected = selectedOption == option
                    val bgColor by animateColorAsState(
                        targetValue = when {
                            isSelected && isSuccess -> Color(0xFF10B981)
                            isSelected && isError -> Color(0xFFEF4444)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        label = "optionBg"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(bgColor)
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
                            .testTag("seq_option_$option"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = option.toString(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp
                                ),
                                color = if (isSelected && (isSuccess || isError)) Color.White
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isSelected && isSuccess) {
                                Spacer(modifier = Modifier.size(8.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "נכון",
                                    tint = Color.White
                                )
                            } else if (isSelected && isError) {
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
