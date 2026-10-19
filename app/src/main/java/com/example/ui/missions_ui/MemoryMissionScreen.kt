package com.example.ui.missions_ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
fun MemoryMissionScreen(
    state: MissionState.Memory,
    onCompleted: () -> Unit
) {
    var isShowingSequence by remember { mutableStateOf(true) }
    var litPadIndex by remember { mutableIntStateOf(-1) }
    val userInputs = remember { mutableStateListOf<Int>() }
    var isError by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Pad colors: 0=Blue, 1=Green, 2=Orange, 3=Purple
    val normalColors = listOf(
        Color(0xFF3B82F6), // Blue
        Color(0xFF10B981), // Emerald Green
        Color(0xFFF59E0B), // Amber/Orange
        Color(0xFF8B5CF6)  // Purple
    )
    val litColors = listOf(
        Color(0xFF93C5FD), // Bright Light Blue
        Color(0xFFA7F3D0), // Bright Light Green
        Color(0xFFFDE68A), // Bright Light Yellow
        Color(0xFFDDD6FE)  // Bright Light Purple
    )

    // Playback sequence initially
    LaunchedEffect(Unit) {
        delay(600)
        isShowingSequence = true
        for (pad in state.sequence) {
            litPadIndex = pad
            delay(500)
            litPadIndex = -1
            delay(250)
        }
        isShowingSequence = false
    }

    fun handlePadClick(padIndex: Int) {
        if (isShowingSequence || isSuccess) return

        userInputs.add(padIndex)
        val currentIndex = userInputs.size - 1

        if (state.sequence[currentIndex] != padIndex) {
            // Wrong tap!
            isError = true
            scope.launch {
                delay(600)
                isError = false
                userInputs.clear()
                // Replay sequence
                isShowingSequence = true
                delay(400)
                for (pad in state.sequence) {
                    litPadIndex = pad
                    delay(500)
                    litPadIndex = -1
                    delay(250)
                }
                isShowingSequence = false
            }
        } else {
            // Correct tap so far
            if (userInputs.size == state.sequence.size) {
                isSuccess = true
                scope.launch {
                    delay(600)
                    onCompleted()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "זיכרון",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = if (isShowingSequence) "צפה ברצף הצבעים..." else "שחזר את הרצף המלא!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    state.sequence.forEachIndexed { idx, _ ->
                        val filled = idx < userInputs.size
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isError && filled -> Color(0xFFEF4444)
                                        isSuccess -> Color(0xFF10B981)
                                        filled -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    }
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2x2 Pad Grid
        val padIndices = listOf(
            listOf(0, 1),
            listOf(2, 3)
        )

        padIndices.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                row.forEach { padIndex ->
                    val isLit = litPadIndex == padIndex
                    val padColor by animateColorAsState(
                        targetValue = if (isLit) litColors[padIndex] else normalColors[padIndex],
                        animationSpec = tween(durationMillis = 150),
                        label = "padColor"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(padColor)
                            .clickable(enabled = !isShowingSequence && !isSuccess) {
                                handlePadClick(padIndex)
                            }
                            .testTag("memory_pad_$padIndex"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${padIndex + 1}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 32.sp
                            ),
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}
