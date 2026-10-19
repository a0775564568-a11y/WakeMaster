package com.example.ui.missions_ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.missions.MissionState
import com.example.missions.ShapeColor
import com.example.missions.ShapeItem
import com.example.missions.ShapeType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ShapesMissionScreen(
    state: MissionState.Shapes,
    onCompleted: () -> Unit
) {
    val userSelected = remember { mutableStateListOf<ShapeItem>() }
    var isError by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Distinct shape buttons pool to choose from
    val allOptions = remember {
        val list = mutableListOf<ShapeItem>()
        var idCounter = 100
        val usedPairs = mutableSetOf<Pair<ShapeType, ShapeColor>>()

        // First add all target shapes
        state.targetShapes.forEach {
            if (usedPairs.add(it.type to it.color)) {
                list.add(it)
            }
        }
        // Then fill up to 6 or 8 options with random distinct shapes
        for (type in ShapeType.values()) {
            for (color in ShapeColor.values()) {
                if (list.size >= 6) break
                if (usedPairs.add(type to color)) {
                    list.add(ShapeItem(idCounter++, type, color))
                }
            }
        }
        list.shuffled()
    }

    fun handleShapeClick(option: ShapeItem) {
        if (isSuccess || isError) return

        val currentStep = userSelected.size
        val expected = state.targetShapes.getOrNull(currentStep)

        if (expected != null && expected.type == option.type && expected.color == option.color) {
            userSelected.add(option)
            if (userSelected.size == state.targetShapes.size) {
                isSuccess = true
                scope.launch {
                    delay(600)
                    onCompleted()
                }
            }
        } else {
            isError = true
            scope.launch {
                delay(600)
                isError = false
                userSelected.clear()
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
                        imageVector = Icons.Default.Category,
                        contentDescription = "צורות",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "הקש על הצורות לפי הסדר המבוקש:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Target Sequence
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    state.targetShapes.forEachIndexed { index, shape ->
                        val isMatched = index < userSelected.size
                        val isCurrent = index == userSelected.size

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isMatched -> Color(0xFF10B981).copy(alpha = 0.2f)
                                        isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                )
                                .border(
                                    width = if (isCurrent) 2.dp else 1.dp,
                                    color = if (isCurrent) MaterialTheme.colorScheme.primary
                                    else if (isMatched) Color(0xFF10B981)
                                    else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            ShapeCanvas(shape = shape, size = 32.dp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Options Grid (3 across, 2 rows)
        val chunked = allOptions.chunked(3)
        chunked.forEach { rowShapes ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowShapes.forEach { shape ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(84.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(enabled = !isSuccess) {
                                handleShapeClick(shape)
                            }
                            .testTag("shape_option_${shape.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        ShapeCanvas(shape = shape, size = 44.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun ShapeCanvas(shape: ShapeItem, size: androidx.compose.ui.unit.Dp) {
    val composeColor = when (shape.color) {
        ShapeColor.RED -> Color(0xFFEF4444)
        ShapeColor.BLUE -> Color(0xFF3B82F6)
        ShapeColor.GREEN -> Color(0xFF10B981)
        ShapeColor.ORANGE -> Color(0xFFF59E0B)
        ShapeColor.PURPLE -> Color(0xFF8B5CF6)
    }

    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        when (shape.type) {
            ShapeType.CIRCLE -> {
                drawCircle(color = composeColor, radius = w / 2.2f)
            }
            ShapeType.SQUARE -> {
                val pad = w * 0.15f
                drawRoundRect(
                    color = composeColor,
                    topLeft = Offset(pad, pad),
                    size = Size(w - 2 * pad, h - 2 * pad)
                )
            }
            ShapeType.TRIANGLE -> {
                val path = Path().apply {
                    moveTo(w / 2f, h * 0.12f)
                    lineTo(w * 0.88f, h * 0.88f)
                    lineTo(w * 0.12f, h * 0.88f)
                    close()
                }
                drawPath(path, color = composeColor)
            }
            ShapeType.DIAMOND -> {
                val path = Path().apply {
                    moveTo(w / 2f, h * 0.1f)
                    lineTo(w * 0.9f, h / 2f)
                    lineTo(w / 2f, h * 0.9f)
                    lineTo(w * 0.1f, h / 2f)
                    close()
                }
                drawPath(path, color = composeColor)
            }
            ShapeType.STAR -> {
                val path = Path().apply {
                    moveTo(w / 2f, h * 0.1f)
                    lineTo(w * 0.62f, h * 0.38f)
                    lineTo(w * 0.92f, h * 0.38f)
                    lineTo(w * 0.68f, h * 0.58f)
                    lineTo(w * 0.78f, h * 0.88f)
                    lineTo(w / 2f, h * 0.70f)
                    lineTo(w * 0.22f, h * 0.88f)
                    lineTo(w * 0.32f, h * 0.58f)
                    lineTo(w * 0.08f, h * 0.38f)
                    lineTo(w * 0.38f, h * 0.38f)
                    close()
                }
                drawPath(path, color = composeColor)
            }
        }
    }
}
