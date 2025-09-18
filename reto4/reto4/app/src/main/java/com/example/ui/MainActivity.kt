package com.example.ui

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.logic.TicTacToeGame
import com.example.reto3.R
import com.google.android.material.bottomnavigation.BottomNavigationView

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


        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.new_game -> {
                    startNewGame()
                    true
                }
                R.id.ai_difficulty -> {
                    showDifficultyDialog()
                    true
                }
                R.id.quit -> {
                    showQuitDialog()
                    true
                }
                else -> false
            }
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

    private fun showDifficultyDialog() {
        val levels = arrayOf(
            getString(R.string.difficulty_easy),
            getString(R.string.difficulty_harder),
            getString(R.string.difficulty_expert)
        )

        val current = when (mGame.getDifficultyLevel()) {
            TicTacToeGame.DifficultyLevel.Easy -> 0
            TicTacToeGame.DifficultyLevel.Harder -> 1
            TicTacToeGame.DifficultyLevel.Expert -> 2
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.difficulty_choose)
            .setSingleChoiceItems(levels, current) { dialog, which ->
                val level = when (which) {
                    0 -> TicTacToeGame.DifficultyLevel.Easy
                    1 -> TicTacToeGame.DifficultyLevel.Harder
                    else -> TicTacToeGame.DifficultyLevel.Expert
                }
                mGame.setDifficultyLevel(level)
                infoText.text = "Difficulty: ${levels[which]}"
                dialog.dismiss()
            }
            .show()
    }

    private fun showQuitDialog() {
        AlertDialog.Builder(this)
            .setMessage(R.string.quit_question)
            .setPositiveButton(R.string.yes) { _, _ -> finish() }
            .setNegativeButton(R.string.no, null)
            .show()
    }
}