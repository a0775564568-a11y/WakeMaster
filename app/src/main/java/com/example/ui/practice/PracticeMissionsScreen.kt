package com.example.ui.practice

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.missions.MissionGenerators
import com.example.missions.MissionState
import com.example.ui.missions_ui.MathMissionScreen
import com.example.ui.missions_ui.MemoryMissionScreen
import com.example.ui.missions_ui.PuzzleMissionScreen
import com.example.ui.missions_ui.SequenceMissionScreen
import com.example.ui.missions_ui.ShapesMissionScreen
import com.example.ui.missions_ui.TypingMissionScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeMissionsScreen(
    onBack: () -> Unit
) {
    val missions = listOf(
        "MATH" to "חשבון",
        "PUZZLE" to "פאזל",
        "MEMORY" to "זיכרון",
        "SEQUENCE" to "סדרות",
        "SHAPES" to "צורות",
        "TYPING" to "הקלדה"
    )

    var selectedMissionIndex by remember { mutableIntStateOf(0) }
    var selectedDifficulty by remember { mutableStateOf("MEDIUM") }
    var refreshKey by remember { mutableIntStateOf(0) }
    var isCompleted by remember { mutableStateOf(false) }

    val missionState = remember(selectedMissionIndex, selectedDifficulty, refreshKey) {
        isCompleted = false
        val currentType = missions[selectedMissionIndex].first
        when (currentType) {
            "MATH" -> MissionGenerators.generateMath(selectedDifficulty)
            "PUZZLE" -> MissionGenerators.generatePuzzle(selectedDifficulty)
            "MEMORY" -> MissionGenerators.generateMemory(selectedDifficulty)
            "SEQUENCE" -> MissionGenerators.generateSequence(selectedDifficulty)
            "SHAPES" -> MissionGenerators.generateShapes(selectedDifficulty)
            "TYPING" -> MissionGenerators.generateTyping(selectedDifficulty)
            else -> MissionGenerators.generateMath(selectedDifficulty)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("תרגול משימות השכמה", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("practice_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "חזרה")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { refreshKey++ },
                        modifier = Modifier.testTag("refresh_mission_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "אתחל משימה")
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mission Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedMissionIndex,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                missions.forEachIndexed { index, (_, label) ->
                    Tab(
                        selected = selectedMissionIndex == index,
                        onClick = { selectedMissionIndex = index },
                        text = { Text(label, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Difficulty Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                val difficulties = listOf(
                    "EASY" to "קל",
                    "MEDIUM" to "בינוני",
                    "HARD" to "מאתגר"
                )
                difficulties.forEach { (dKey, dLabel) ->
                    FilterChip(
                        selected = selectedDifficulty == dKey,
                        onClick = { selectedDifficulty = dKey },
                        label = { Text(dLabel) },
                        modifier = Modifier.padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active Mission Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (isCompleted) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF10B981).copy(alpha = 0.15f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "מצוין! השלמת את המשימה בהצלחה!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { refreshKey++ },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("practice_again_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("משימה חדשה", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    when (val state = missionState) {
                        is MissionState.Math -> {
                            MathMissionScreen(state = state, onCompleted = { isCompleted = true })
                        }
                        is MissionState.Puzzle -> {
                            PuzzleMissionScreen(state = state, onCompleted = { isCompleted = true })
                        }
                        is MissionState.Memory -> {
                            MemoryMissionScreen(state = state, onCompleted = { isCompleted = true })
                        }
                        is MissionState.Sequence -> {
                            SequenceMissionScreen(state = state, onCompleted = { isCompleted = true })
                        }
                        is MissionState.Shapes -> {
                            ShapesMissionScreen(state = state, onCompleted = { isCompleted = true })
                        }
                        is MissionState.Typing -> {
                            TypingMissionScreen(state = state, onCompleted = { isCompleted = true })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
