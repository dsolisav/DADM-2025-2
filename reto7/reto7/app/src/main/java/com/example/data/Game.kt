package com.example.data

data class Game(
    var board: MutableList<String> = MutableList(9) { " " },
    var playerX: String? = null,
    var playerO: String? = null,
    var turn: String = "X",
    var winner: String = "none",
    var status: String = "waiting"  // "waiting", "playing", "finished"
)