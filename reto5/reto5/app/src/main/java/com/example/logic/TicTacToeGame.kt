package com.example.logic
import kotlin.random.Random

class TicTacToeGame {
    companion object {
        const val HUMAN_PLAYER = 'X'
        const val COMPUTER_PLAYER = 'O'
        const val OPEN_SPOT = ' '
        const val BOARD_SIZE = 9
    }

    enum class DifficultyLevel { Easy, Harder, Expert }

    private var difficultyLevel: DifficultyLevel = DifficultyLevel.Expert

    private val board = CharArray(BOARD_SIZE) { OPEN_SPOT }

    fun getDifficultyLevel(): DifficultyLevel = difficultyLevel
    fun setDifficultyLevel(level: DifficultyLevel) {
        difficultyLevel = level
    }

    fun clearBoard() {
        for (i in board.indices) board[i] = OPEN_SPOT
    }

    fun setMove(player: Char, location: Int) {
        if (board[location] == OPEN_SPOT) board[location] = player
    }

    fun getComputerMove(): Int {
        return when (difficultyLevel) {
            DifficultyLevel.Easy -> getRandomMove()
            DifficultyLevel.Harder -> {
                getWinningMove().takeIf { it != -1 } ?: getRandomMove()
            }
            DifficultyLevel.Expert -> {
                getWinningMove().takeIf { it != -1 }
                    ?: getBlockingMove().takeIf { it != -1 }
                    ?: getRandomMove()
            }
        }
    }

    fun checkForWinner(): Int {
        val lines = listOf(
            listOf(0,1,2), listOf(3,4,5), listOf(6,7,8), // rows
            listOf(0,3,6), listOf(1,4,7), listOf(2,5,8), // cols
            listOf(0,4,8), listOf(2,4,6)                 // diagonals
        )
        for (line in lines) {
            val (a,b,c) = line
            if (board[a] != OPEN_SPOT && board[a] == board[b] && board[b] == board[c]) {
                return if (board[a] == HUMAN_PLAYER) 2 else 3
            }
        }
        return if (board.none { it == OPEN_SPOT }) 1 else 0 // 1=tie, 0=no winner yet
    }

    private fun getRandomMove(): Int {
        val openSpots = board.withIndex().filter { it.value == OPEN_SPOT }.map { it.index }
        return if (openSpots.isNotEmpty()) openSpots.random(Random) else -1
    }

    private fun getWinningMove(): Int {
        for (i in board.indices) {
            if (board[i] == OPEN_SPOT) {
                board[i] = COMPUTER_PLAYER
                if (checkForWinner() == 3) {
                    board[i] = OPEN_SPOT
                    return i
                }
                board[i] = OPEN_SPOT
            }
        }
        return -1
    }

    private fun getBlockingMove(): Int {
        for (i in board.indices) {
            if (board[i] == OPEN_SPOT) {
                board[i] = HUMAN_PLAYER
                if (checkForWinner() == 2) {
                    board[i] = OPEN_SPOT
                    return i
                }
                board[i] = OPEN_SPOT
            }
        }
        return -1
    }

    fun getBoardOccupant(position: Int): Char {
        return board[position]
    }

}