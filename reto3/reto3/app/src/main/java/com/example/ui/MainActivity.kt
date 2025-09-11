package com.example.ui

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.logic.TicTacToeGame
import com.example.reto3.R

class MainActivity : AppCompatActivity() {

    private lateinit var mGame: TicTacToeGame
    private lateinit var boardButtons: Array<Button>
    private lateinit var infoText: TextView
    private var gameOver = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mGame = TicTacToeGame()

        boardButtons = Array(TicTacToeGame.BOARD_SIZE) { i ->
            findViewById(resources.getIdentifier("btn$i", "id", packageName))
        }
        infoText = findViewById(R.id.infoText)

        val restartButton: Button = findViewById(R.id.btnRestart)
        restartButton.setOnClickListener {
            startNewGame()
        }

        startNewGame()
    }

    private fun startNewGame() {
        mGame.clearBoard()
        gameOver = false
        infoText.text = getString(R.string.first_human)

        for (i in boardButtons.indices) {
            boardButtons[i].apply {
                text = ""
                isEnabled = true
                setOnClickListener { makeMove(i) }
            }
        }
    }

    private fun makeMove(location: Int) {
        if (gameOver) return

        setMove(TicTacToeGame.HUMAN_PLAYER, location)

        var winner = mGame.checkForWinner()
        if (winner == 0) {
            infoText.text = getString(R.string.turn_computer)
            val move = mGame.getComputerMove()
            if (move != -1) setMove(TicTacToeGame.COMPUTER_PLAYER, move)
            winner = mGame.checkForWinner()
        }

        when (winner) {
            0 -> infoText.text = getString(R.string.turn_human)
            1 -> { infoText.text = getString(R.string.result_tie); gameOver = true }
            2 -> { infoText.text = getString(R.string.result_human_wins); gameOver = true }
            3 -> { infoText.text = getString(R.string.result_computer_wins); gameOver = true }
        }
    }

    private fun setMove(player: Char, location: Int) {
        mGame.setMove(player, location)
        boardButtons[location].apply {
            text = player.toString()
            isEnabled = false
            setTextColor(
                if (player == TicTacToeGame.HUMAN_PLAYER) Color.GREEN else Color.RED
            )
        }
    }
}