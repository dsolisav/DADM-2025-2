package com.example.ui

import android.content.SharedPreferences
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
import com.example.reto7.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*


class MainActivity : AppCompatActivity() {

    private lateinit var mGame: TicTacToeGame
    private lateinit var boardButtons: Array<Button>
    private lateinit var infoText: TextView
    private var gameOver = false
    private lateinit var boardView: BoardView
    private var humanMediaPlayer: MediaPlayer? = null
    private var computerMediaPlayer: MediaPlayer? = null
    private lateinit var prefs: SharedPreferences
    private var humanWins = 0
    private var computerWins = 0
    private var ties = 0

    private lateinit var database: DatabaseReference
    private lateinit var playerSymbol: String   // "X" or "O"
    private lateinit var currentPlayer: String  // current turn from Firebase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE)

        humanWins = prefs.getInt("humanWins", 0)
        computerWins = prefs.getInt("computerWins", 0)
        ties = prefs.getInt("ties", 0)

        displayScores()


        mGame = TicTacToeGame()
        boardView = findViewById(R.id.boardView)
        boardView.setGame(mGame)

        boardView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && !gameOver) {
                val col = (event.x / boardView.getBoardCellWidth()).toInt()
                val row = (event.y / boardView.getBoardCellHeight()).toInt()
                val pos = row * 3 + col
                makeMove(pos)
                true   // ✅ consume event so no extra taps get through
            } else {
                false
            }
        }

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

        if (savedInstanceState == null) {
            startNewGame()
        } else {
            mGame.setBoardState(savedInstanceState.getCharArray("board")!!)
            gameOver = savedInstanceState.getBoolean("gameOver")
            infoText.text = savedInstanceState.getCharSequence("infoText")
            humanWins = savedInstanceState.getInt("humanWins")
            computerWins = savedInstanceState.getInt("computerWins")
            ties = savedInstanceState.getInt("ties")
            mGame.setDifficultyLevel(
                savedInstanceState.getSerializable("difficulty") as TicTacToeGame.DifficultyLevel
            )
            displayScores()
        }

        database = FirebaseDatabase.getInstance().getReference("games/demoGame")

        // Assign this user as X or O (you’ll handle joining logic later)
                playerSymbol = if (Math.random() < 0.5) "X" else "O"
                infoText.text = "You are player $playerSymbol"

        // Listen for changes from Firebase
                listenForGameUpdates()

    }

    private fun displayScores() {
        val scoreText = "Human: $humanWins  Computer: $computerWins  Ties: $ties"
        findViewById<TextView>(R.id.scoreDisplay).text = scoreText
    }


    override fun onStop() {
        super.onStop()
        prefs.edit().apply {
            putInt("humanWins", humanWins)
            putInt("computerWins", computerWins)
            putInt("ties", ties)
            apply()
        }
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

        val startPlayer = "X"
        val updates = mapOf(
            "board" to mGame.getBoardState().map { it.toString() },
            "currentPlayer" to startPlayer,
            "winner" to ""
        )
        database.updateChildren(updates)

        infoText.text = if (playerSymbol == startPlayer) "Your turn!" else "Opponent's turn"
        boardView.invalidate()
    }

    private fun makeMove(location: Int) {
        if (gameOver) return

        // Only allow tapping if it's this user's turn
        if (playerSymbol != currentPlayer) {
            infoText.text = "Wait for your turn!"
            return
        }

        // Check if cell is already taken
        val board = mGame.getBoardState()
        if (board[location] != TicTacToeGame.OPEN_SPOT) return

        // Update local board
        mGame.setMove(playerSymbol.first(), location)
        boardView.invalidate()

        // Check winner locally
        val winner = mGame.checkForWinner()

        // Prepare next player
        val nextPlayer = if (playerSymbol == "X") "O" else "X"

        // Send update to Firebase
        val updates = mapOf(
            "board" to mGame.getBoardState().map { it.toString() },
            "currentPlayer" to nextPlayer,
            "winner" to when (winner) {
                2 -> "X"
                3 -> "O"
                1 -> "T"
                else -> ""
            }
        )
        database.updateChildren(updates)
    }

    // Helper function to avoid duplicating winner logic
    private fun handleWinner(winner: Int) {
        when (winner) {
            0 -> infoText.text = getString(R.string.turn_human)
            1 -> {
                infoText.text = getString(R.string.result_tie)
                ties++
                gameOver = true
                displayScores()
            }
            2 -> {
                infoText.text = getString(R.string.result_human_wins)
                humanWins++
                gameOver = true
                displayScores()
            }
            3 -> {
                infoText.text = getString(R.string.result_computer_wins)
                computerWins++
                gameOver = true
                displayScores()
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putCharArray("board", mGame.getBoardState())
        outState.putBoolean("gameOver", gameOver)
        outState.putCharSequence("infoText", infoText.text)
        outState.putInt("humanWins", humanWins)
        outState.putInt("computerWins", computerWins)
        outState.putInt("ties", ties)
        outState.putSerializable("difficulty", mGame.getDifficultyLevel())
    }

    private fun listenForGameUpdates() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val boardList = snapshot.child("board").children.map { it.getValue(String::class.java) ?: " " }
                val winner = snapshot.child("winner").getValue(String::class.java) ?: ""
                val current = snapshot.child("currentPlayer").getValue(String::class.java) ?: "X"

                currentPlayer = current
                mGame.setBoardState(boardList.joinToString("").toCharArray())
                boardView.invalidate()

                when (winner) {
                    "X" -> {
                        infoText.text = "Player X wins!"
                        gameOver = true
                    }
                    "O" -> {
                        infoText.text = "Player O wins!"
                        gameOver = true
                    }
                    "T" -> {
                        infoText.text = "It's a tie!"
                        gameOver = true
                    }
                    else -> {
                        // ✅ Whoever’s turn it is gets to play
                        gameOver = currentPlayer != playerSymbol
                        infoText.text = if (!gameOver) "Your turn!" else "Opponent's turn"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                infoText.text = "Error syncing game"
            }
        })
    }
}