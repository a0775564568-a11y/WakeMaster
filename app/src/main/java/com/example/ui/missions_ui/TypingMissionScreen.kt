package com.example.ui.missions_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.missions.MissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TypingMissionScreen(
    state: MissionState.Typing,
    onCompleted: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val target = state.targetSentence
    val progress = if (target.isNotEmpty()) {
        (input.length.toFloat() / target.length.toFloat()).coerceIn(0f, 1f)
    } else 0f

    fun checkMatch(current: String) {
        if (current.trim() == target.trim()) {
            isSuccess = true
            scope.launch {
                delay(600)
                onCompleted()
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
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "הקלדה",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "הקלד את המשפט במדויק:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    Text(
                        text = target,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 28.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isSuccess) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { newVal ->
                if (!isSuccess) {
                    input = newVal
                    checkMatch(newVal)
                }
            },
            placeholder = { Text("הקלד כאן...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("typing_input_field"),
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { checkMatch(input) }),
            singleLine = false,
            maxLines = 3,
            trailingIcon = {
                if (isSuccess) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "הושלם",
                        tint = Color(0xFF10B981)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Paste / Testing assistance button
        OutlinedButton(
            onClick = {
                input = target
                isSuccess = true
                scope.launch {
                    delay(500)
                    onCompleted()
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.secondary
            ),
            modifier = Modifier.testTag("auto_fill_typing_btn")
        ) {
            Text("מלא משפט אוטומטית (בדיקה מהירה)")
        }
    }
}
