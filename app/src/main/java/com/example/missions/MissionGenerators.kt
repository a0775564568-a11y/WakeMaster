package com.example.missions

import kotlin.random.Random

object MissionGenerators {

    // 1. Math Generator
    fun generateMath(difficulty: String): MissionState.Math {
        val rand = Random.Default
        val (question, answer) = when (difficulty) {
            "EASY" -> {
                val a = rand.nextInt(5, 30)
                val b = rand.nextInt(3, 25)
                if (rand.nextBoolean()) {
                    "$a + $b" to (a + b)
                } else {
                    val max = maxOf(a, b)
                    val min = minOf(a, b)
                    "$max - $min" to (max - min)
                }
            }
            "HARD" -> {
                val type = rand.nextInt(3)
                when (type) {
                    0 -> {
                        val a = rand.nextInt(11, 25)
                        val b = rand.nextInt(4, 15)
                        val c = rand.nextInt(10, 50)
                        "($a × $b) - $c" to ((a * b) - c)
                    }
                    1 -> {
                        val divisor = rand.nextInt(6, 15)
                        val quotient = rand.nextInt(8, 20)
                        val dividend = divisor * quotient
                        val c = rand.nextInt(15, 60)
                        "($dividend ÷ $divisor) + $c" to (quotient + c)
                    }
                    else -> {
                        val a = rand.nextInt(12, 35)
                        val b = rand.nextInt(12, 35)
                        val c = rand.nextInt(10, 30)
                        "$a + $b + $c" to (a + b + c)
                    }
                }
            }
            else -> { // MEDIUM
                val type = rand.nextInt(3)
                when (type) {
                    0 -> {
                        val a = rand.nextInt(6, 14)
                        val b = rand.nextInt(4, 12)
                        "$a × $b" to (a * b)
                    }
                    1 -> {
                        val divisor = rand.nextInt(3, 10)
                        val quotient = rand.nextInt(5, 15)
                        val dividend = divisor * quotient
                        "$dividend ÷ $divisor" to quotient
                    }
                    else -> {
                        val a = rand.nextInt(25, 95)
                        val b = rand.nextInt(15, 80)
                        "$a + $b" to (a + b)
                    }
                }
            }
        }

        // Generate 3 plausible wrong options
        val wrongOptions = mutableSetOf<Int>()
        while (wrongOptions.size < 3) {
            val delta = rand.nextInt(1, 6) * if (rand.nextBoolean()) 1 else -1
            val fake = answer + delta
            if (fake != answer && fake > 0) {
                wrongOptions.add(fake)
            }
        }
        val options = (wrongOptions + answer).shuffled()

        return MissionState.Math(
            question = "$question = ?",
            correctAnswer = answer,
            options = options
        )
    }

    // 2. Sliding Puzzle Generator
    fun generatePuzzle(difficulty: String): MissionState.Puzzle {
        val gridSize = when (difficulty) {
            "HARD" -> 4 // 4x4 (15 puzzle)
            "MEDIUM" -> 3 // 3x3 (8 puzzle)
            else -> 3 // 3x3
        }
        val totalTiles = gridSize * gridSize
        // Generate solvable permutation: start from solved state and apply random moves
        val board = (1 until totalTiles).toMutableList().apply { add(0) }
        var emptyPos = totalTiles - 1

        val shuffleSteps = when (difficulty) {
            "EASY" -> 12
            "MEDIUM" -> 25
            else -> 40
        }

        for (i in 0 until shuffleSteps) {
            val neighbors = mutableListOf<Int>()
            val r = emptyPos / gridSize
            val c = emptyPos % gridSize

            if (r > 0) neighbors.add((r - 1) * gridSize + c)
            if (r < gridSize - 1) neighbors.add((r + 1) * gridSize + c)
            if (c > 0) neighbors.add(r * gridSize + (c - 1))
            if (c < gridSize - 1) neighbors.add(r * gridSize + (c + 1))

            val chosen = neighbors.random()
            board[emptyPos] = board[chosen]
            board[chosen] = 0
            emptyPos = chosen
        }

        return MissionState.Puzzle(
            gridSize = gridSize,
            tiles = board
        )
    }

    // 3. Memory Sequence Generator
    fun generateMemory(difficulty: String): MissionState.Memory {
        val length = when (difficulty) {
            "EASY" -> 4
            "HARD" -> 7
            else -> 5
        }
        val sequence = List(length) { Random.nextInt(4) }
        return MissionState.Memory(
            sequence = sequence,
            userSequence = emptyList(),
            isShowingPattern = true,
            activePadIndex = -1
        )
    }

    // 4. Number Sequences Generator
    fun generateSequence(difficulty: String): MissionState.Sequence {
        val rand = Random.Default
        val type = when (difficulty) {
            "EASY" -> rand.nextInt(2) // Arithmetic step (e.g. +3, +5)
            "HARD" -> rand.nextInt(4) // Fibonacci, geometric, alternating, square
            else -> rand.nextInt(3) // Arithmetic or doubling
        }

        val list = mutableListOf<Int>()
        var hint = ""
        var answer = 0
        var missingIdx = 3

        when (type) {
            0 -> { // Arithmetic step
                val start = rand.nextInt(2, 20)
                val step = rand.nextInt(3, 9)
                hint = "סדרה חשבונית בהפרש $step+"
                for (i in 0..4) list.add(start + (i * step))
                missingIdx = 4
                answer = list[missingIdx]
            }
            1 -> { // Doubling (geometric * 2)
                val start = rand.nextInt(2, 6)
                hint = "סדרה הנדסית מוכפלת פי 2"
                var cur = start
                for (i in 0..4) {
                    list.add(cur)
                    cur *= 2
                }
                missingIdx = 4
                answer = list[missingIdx]
            }
            2 -> { // Alternating step (+a, -b)
                val start = rand.nextInt(10, 30)
                hint = "סדרה בצעדים משתנים"
                var cur = start
                list.add(cur)
                for (i in 1..4) {
                    cur = if (i % 2 == 1) cur + 6 else cur - 2
                    list.add(cur)
                }
                missingIdx = 4
                answer = list[missingIdx]
            }
            else -> { // Squares (e.g., 1, 4, 9, 16, 25)
                val offset = rand.nextInt(1, 4)
                hint = "סדרת חזקות ריבועיות"
                for (i in 0..4) {
                    val n = i + offset
                    list.add(n * n)
                }
                missingIdx = 4
                answer = list[missingIdx]
            }
        }

        val display = list.mapIndexed { idx, v -> if (idx == missingIdx) "?" else v.toString() }
        val wrongOptions = mutableSetOf<Int>()
        while (wrongOptions.size < 3) {
            val delta = rand.nextInt(1, 6) * if (rand.nextBoolean()) 1 else -1
            val fake = answer + delta
            if (fake != answer && fake > 0) {
                wrongOptions.add(fake)
            }
        }
        val options = (wrongOptions + answer).shuffled()

        return MissionState.Sequence(
            sequenceDisplay = display,
            missingIndex = missingIdx,
            correctAnswer = answer,
            options = options,
            hintHebrew = hint
        )
    }

    // 5. Shape Matching Generator
    fun generateShapes(difficulty: String): MissionState.Shapes {
        val count = when (difficulty) {
            "EASY" -> 3
            "HARD" -> 6
            else -> 4
        }
        val types = ShapeType.values()
        val colors = ShapeColor.values()
        val targetList = mutableListOf<ShapeItem>()

        for (i in 0 until count) {
            val type = types.random()
            val color = colors.random()
            targetList.add(ShapeItem(id = i, type = type, color = color))
        }

        return MissionState.Shapes(
            targetShapes = targetList,
            userSelectedShapes = emptyList()
        )
    }

    // 6. Hebrew Sentence Typing Generator
    fun generateTyping(difficulty: String): MissionState.Typing {
        val sentencesEasy = listOf(
            "בוקר חדש של הזדמנויות והצלחה",
            "יום חדש מתחיל עם חיוך ואנרגיה",
            "היום אני מתעורר מלא כוח ומוטיבציה",
            "השמש זורחת וזה הזמן לקום"
        )
        val sentencesMedium = listOf(
            "היום אני בוחר לקום מוקדם ולהגשים את המטרות שלי",
            "כל יום הוא דף נקי שבו אני כותב את העתיד שלי",
            "הצעד הראשון להצלחה הוא פשוט להתעורר בזמן",
            "אומץ ונחישות הם המפתחות לפתיחת הבוקר"
        )
        val sentencesHard = listOf(
            "המשמעת העצמית של הבוקר היא המפתח הבלעדי להצלחה של כל היום כולו",
            "מי שמנצח את השעון המעורר בבוקר מנצח את כל האתגרים שיבואו בהמשך",
            "ההבדל בין חלום למציאות הוא ההחלטה לקום ולפעול עכשיו"
        )

        val target = when (difficulty) {
            "EASY" -> sentencesEasy.random()
            "HARD" -> sentencesHard.random()
            else -> sentencesMedium.random()
        }

        return MissionState.Typing(
            targetSentence = target,
            typedText = "",
            mistakes = 0
        )
    }
}
