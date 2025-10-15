package com.example.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.reto7.R
import com.example.data.Game
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LobbyActivity : AppCompatActivity() {

    private lateinit var db: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var listView: ListView
    private lateinit var btnCreate: Button
    private val gameList = mutableListOf<Pair<String, Game>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().getReference("games")

        listView = findViewById(R.id.listViewGames)
        btnCreate = findViewById(R.id.btnCreateGame)

        // Sign in anonymously and wait for completion
        auth.signInAnonymously()
            .addOnSuccessListener {
                Log.d("Lobby", "Auth successful: ${auth.currentUser?.uid}")
                setupLobby()
            }
            .addOnFailureListener { e ->
                Log.e("Lobby", "Auth failed", e)
                Toast.makeText(this, "Authentication failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setupLobby() {
        btnCreate.setOnClickListener {
            createGame()
        }

        // Listen for available games
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                gameList.clear()
                for (gameSnap in snapshot.children) {
                    val game = gameSnap.getValue(Game::class.java)
                    // Show games that are waiting for a second player
                    if (game != null && game.status == "waiting") {
                        gameList.add(gameSnap.key!! to game)
                    }
                }
                val adapter = ArrayAdapter(
                    this@LobbyActivity,
                    android.R.layout.simple_list_item_1,
                    gameList.map { "Game ID: ${it.first}" }
                )
                listView.adapter = adapter
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        listView.setOnItemClickListener { _, _, position, _ ->
            val (id, game) = gameList[position]
            joinGame(id, game)
        }
    }

    private fun createGame() {
        val id = db.push().key!!
        val uid = auth.currentUser?.uid
        val newGame = Game(playerX = uid)
        Log.d("Lobby", "Creating game: ID=$id, PlayerX=$uid")
        db.child(id).setValue(newGame)
        openGame(id)
    }

    private fun joinGame(id: String, game: Game) {
        val uid = auth.currentUser?.uid
        if (game.playerO == null) {
            Log.d("Lobby", "Joining game: ID=$id, PlayerO=$uid, PlayerX=${game.playerX}")
            // Only set playerO, keep status as "waiting" until game actually starts
            db.child(id).child("playerO").setValue(uid)
            openGame(id)
        } else {
            Toast.makeText(this, "Game is full!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGame(id: String) {
        val intent = Intent(this, OnlineGameActivity::class.java)
        intent.putExtra("GAME_ID", id)
        startActivity(intent)
    }
}