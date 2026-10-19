package com.example.missions

sealed class MissionState {
    data class Math(
        val question: String,
        val correctAnswer: Int,
        val options: List<Int>
    ) : MissionState()

    data class Puzzle(
        val gridSize: Int, // 3 for 3x3, 4 for 4x4
        val tiles: List<Int>, // Numbers 1..(N-1), and 0 represents the empty slot
        val isSolved: Boolean = false
    ) : MissionState()

    data class Memory(
        val sequence: List<Int>, // Indices of pads (0..3)
        val userSequence: List<Int> = emptyList(),
        val isShowingPattern: Boolean = true,
        val activePadIndex: Int = -1
    ) : MissionState()

    data class Sequence(
        val sequenceDisplay: List<String>, // e.g. ["2", "5", "8", "11", "?"]
        val missingIndex: Int,
        val correctAnswer: Int,
        val options: List<Int>,
        val hintHebrew: String
    ) : MissionState()

    data class Shapes(
        val targetShapes: List<ShapeItem>,
        val userSelectedShapes: List<ShapeItem> = emptyList()
    ) : MissionState()

    data class Typing(
        val targetSentence: String,
        val typedText: String = "",
        val mistakes: Int = 0
    ) : MissionState()
}

enum class ShapeType {
    CIRCLE, SQUARE, TRIANGLE, STAR, DIAMOND
}

enum class ShapeColor {
    RED, BLUE, GREEN, ORANGE, PURPLE
}

data class ShapeItem(
    val id: Int,
    val type: ShapeType,
    val color: ShapeColor
) {
    fun getHebrewName(): String {
        val shapeName = when (type) {
            ShapeType.CIRCLE -> "עיגול"
            ShapeType.SQUARE -> "ריבוע"
            ShapeType.TRIANGLE -> "משולש"
            ShapeType.STAR -> "כוכב"
            ShapeType.DIAMOND -> "מעוין"
        }
        val colorName = when (color) {
            ShapeColor.RED -> "אדום"
            ShapeColor.BLUE -> "כחול"
            ShapeColor.GREEN -> "ירוק"
            ShapeColor.ORANGE -> "כתום"
            ShapeColor.PURPLE -> "סגול"
        }
        return "$shapeName $colorName"
    }
}
