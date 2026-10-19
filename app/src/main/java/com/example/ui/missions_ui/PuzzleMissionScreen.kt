package com.example.ui.missions_ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
fun PuzzleMissionScreen(
    state: MissionState.Puzzle,
    onCompleted: () -> Unit
) {
    val gridSize = state.gridSize
    var tiles by remember { mutableStateOf(state.tiles) }
    var moveCount by remember { mutableIntStateOf(0) }
    var isSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun checkSolved(currentTiles: List<Int>): Boolean {
        val total = gridSize * gridSize
        for (i in 0 until total - 1) {
            if (currentTiles[i] != i + 1) return false
        }
        return currentTiles[total - 1] == 0
    }

    fun handleTileClick(index: Int) {
        if (isSuccess) return
        val emptyIndex = tiles.indexOf(0)
        val row = index / gridSize
        val col = index % gridSize
        val emptyRow = emptyIndex / gridSize
        val emptyCol = emptyIndex % gridSize

        val isAdjacent = (row == emptyRow && kotlin.math.abs(col - emptyCol) == 1) ||
                (col == emptyCol && kotlin.math.abs(row - emptyRow) == 1)

        if (isAdjacent) {
            val newTiles = tiles.toMutableList()
            newTiles[emptyIndex] = tiles[index]
            newTiles[index] = 0
            tiles = newTiles
            moveCount++

            if (checkSolved(newTiles)) {
                isSuccess = true
                scope.launch {
                    delay(700)
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Extension,
                contentDescription = "פאזל",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "סדר את המספרים ברצף (1 עד ${gridSize * gridSize - 1})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "מהלכים: $moveCount",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Grid Container
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (r in 0 until gridSize) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (c in 0 until gridSize) {
                            val index = r * gridSize + c
                            val tileValue = tiles.getOrNull(index) ?: 0

                            if (tileValue == 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Transparent)
                                )
                            } else {
                                val isCorrectPos = tileValue == index + 1
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSuccess) Color(0xFF10B981)
                                            else if (isCorrectPos) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.secondaryContainer
                                        )
                                        .clickable { handleTileClick(index) }
                                        .testTag("puzzle_tile_$tileValue"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tileValue.toString(),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = if (gridSize == 3) 26.sp else 20.sp
                                        ),
                                        color = if (isSuccess || isCorrectPos) Color.White
                                        else MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Solve / Testing assistance
        OutlinedButton(
            onClick = {
                val total = gridSize * gridSize
                val solved = (1 until total).toMutableList().apply { add(0) }
                tiles = solved
                isSuccess = true
                scope.launch {
                    delay(500)
                    onCompleted()
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag("auto_solve_puzzle_btn")
        ) {
            Text("פתור פאזל (בדיקה מהירה)")
        }
    }
}
