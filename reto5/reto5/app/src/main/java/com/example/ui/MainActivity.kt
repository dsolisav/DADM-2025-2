package com.example.ui

import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.logic.TicTacToeGame
import com.example.reto5.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var mGame: TicTacToeGame
    private lateinit var boardButtons: Array<Button>
    private lateinit var infoText: TextView
    private var gameOver = false
    private lateinit var boardView: BoardView
    private var humanMediaPlayer: MediaPlayer? = null
    private var computerMediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mGame = TicTacToeGame()
        boardView = findViewById(R.id.boardView)
        boardView.setGame(mGame)

        boardView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && !gameOver) {
                val col = (event.x / boardView.getBoardCellWidth()).toInt()
                val row = (event.y / boardView.getBoardCellHeight()).toInt()
                val pos = row * 3 + col
                makeMove(pos)
            }
            false
        }

        /*boardButtons = Array(TicTacToeGame.BOARD_SIZE) { i ->
            findViewById(resources.getIdentifier("btn$i", "id", packageName))
        }*/
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

    override fun onResume() {
        super.onResume()
        humanMediaPlayer = MediaPlayer.create(this, R.raw.move_human)
        computerMediaPlayer = MediaPlayer.create(this, R.raw.move_computer)
    }

    override fun onPause() {
        super.onPause()
        humanMediaPlayer?.release()
        computerMediaPlayer?.release()
    }

    private fun startNewGame() {
        mGame.clearBoard()
        gameOver = false
        infoText.text = getString(R.string.first_human)
        boardView.invalidate()
    }

    private fun makeMove(location: Int) {
        if (gameOver) return

        setMove(TicTacToeGame.HUMAN_PLAYER, location)

        var winner = mGame.checkForWinner()
        if (winner == 0) {
            infoText.text = getString(R.string.turn_computer)
            val move = mGame.getComputerMove()
            if (move != -1) {
                // Delay the computer move by 800 ms
                boardView.postDelayed({
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move)

                    val winnerAfter = mGame.checkForWinner()
                    when (winnerAfter) {
                        0 -> infoText.text = getString(R.string.turn_human)
                        1 -> { infoText.text = getString(R.string.result_tie); gameOver = true }
                        2 -> { infoText.text = getString(R.string.result_human_wins); gameOver = true }
                        3 -> { infoText.text = getString(R.string.result_computer_wins); gameOver = true }
                    }
                }, 800) // delay in ms
            }
        } else {
            // Game ended right after human move
            when (winner) {
                1 -> { infoText.text = getString(R.string.result_tie); gameOver = true }
                2 -> { infoText.text = getString(R.string.result_human_wins); gameOver = true }
                3 -> { infoText.text = getString(R.string.result_computer_wins); gameOver = true }
            }
        }
    }

    private fun setMove(player: Char, location: Int) {
        mGame.setMove(player, location)
        boardView.invalidate()
        if (player == TicTacToeGame.HUMAN_PLAYER) {
            humanMediaPlayer?.start()
        } else {
            computerMediaPlayer?.start()
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