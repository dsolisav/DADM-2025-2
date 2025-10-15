package com.example.ui

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.reto7.R
import com.example.data.Game
import com.example.logic.TicTacToeGame
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.ui.BoardView

class OnlineGameActivity : AppCompatActivity() {

    private lateinit var boardView: BoardView
    private lateinit var infoText: TextView
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var currentGameId = ""
    private var currentPlayer: Char? = null  // Make nullable until assigned
    private var cachedGame: Game? = null  // Cache the current game state
    private var hasMovedAtLeastOnce = false  // Track if player has made a move

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_game)

        boardView = findViewById(R.id.boardView)
        infoText = findViewById(R.id.infoText)

        auth = FirebaseAuth.getInstance()
        currentGameId = intent.getStringExtra("GAME_ID") ?: ""
        dbRef = FirebaseDatabase.getInstance().getReference("games/$currentGameId")

        // Determine if user is X or O
        dbRef.get().addOnSuccessListener { snapshot ->
            val game = snapshot.getValue(Game::class.java)
            val uid = auth.currentUser?.uid
            currentPlayer = if (game?.playerX == uid) 'X' else 'O'
            Log.d("OnlineGame", "Player assigned: $currentPlayer, UID: $uid, PlayerX: ${game?.playerX}, PlayerO: ${game?.playerO}")
            infoText.text = "You are Player $currentPlayer"
        }

        // Listen for game updates
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val game = snapshot.getValue(Game::class.java) ?: return
                cachedGame = game  // Cache the game state
                boardView.setBoard(game.board)
                infoText.text = when (game.winner) {
                    "X" -> "Winner: Player X!"
                    "O" -> "Winner: Player O!"
                    "tie" -> "It's a tie!"
                    else -> "Turn: ${game.turn}"
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // Handle user moves
        boardView.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                handleTouch(event.x, event.y)
            }
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        
        // If player joined but never moved, reset the game back to waiting
        val game = cachedGame
        val player = currentPlayer
        
        if (game != null && player != null && !hasMovedAtLeastOnce) {
            // Check if this was player O (the joiner) and game hasn't started
            if (player == 'O' && game.board.all { it == " " }) {
                Log.d("OnlineGame", "Player O left before making a move, resetting game to waiting")
                val updates = mapOf(
                    "playerO" to null,
                    "status" to "waiting"
                )
                dbRef.updateChildren(updates)
            }
        }
    }

    private fun handleTouch(x: Float, y: Float) {
        val col = (x / boardView.getBoardCellWidth()).toInt()
        val row = (y / boardView.getBoardCellHeight()).toInt()
        val index = row * 3 + col

        Log.d("OnlineGame", "Touch at index: $index")
        
        // Check if player assignment is complete
        val player = currentPlayer
        if (player == null) {
            Log.d("OnlineGame", "Player not assigned yet, waiting...")
            return
        }
        
        // Use the cached game state instead of fetching again
        val game = cachedGame
        if (game == null) {
            Log.d("OnlineGame", "Game is null, waiting for Firebase sync")
            return
        }
        
        Log.d("OnlineGame", "Current turn: ${game.turn}, My player: $player, Cell value: ${game.board[index]}, Winner: ${game.winner}")
        
        // Check if it's this player's turn and the cell is empty
        if (game.turn == player.toString() && game.board[index] == " " && game.winner == "none") {
            Log.d("OnlineGame", "Making move at index $index")
            hasMovedAtLeastOnce = true  // Mark that player has engaged with the game
            val newBoard = game.board.toMutableList()
            newBoard[index] = player.toString()
            
            val logic = TicTacToeGame()
            logic.loadBoard(newBoard.map { it.first() })
            val winner = logic.checkForWinner()

            val updatedGame = game.copy(
                board = newBoard,
                turn = if (player == 'X') "O" else "X",
                winner = when (winner) {
                    2 -> "X"
                    3 -> "O"
                    1 -> "tie"
                    else -> "none"
                },
                status = when {
                    winner != 0 -> "finished"  // Game over
                    game.status == "waiting" -> "playing"  // First move, transition to playing
                    else -> game.status  // Keep current status
                }
            )

            Log.d("OnlineGame", "Updating Firebase with new turn: ${updatedGame.turn}")
            dbRef.setValue(updatedGame)
        } else {
            Log.d("OnlineGame", "Move rejected - Turn check: ${game.turn == player.toString()}, Empty check: ${game.board[index] == " "}, Not finished: ${game.winner == "none"}")
        }
    }
}