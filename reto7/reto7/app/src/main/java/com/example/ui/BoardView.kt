package com.example.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.example.logic.TicTacToeGame

class BoardView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint()
    private val board = CharArray(9) { ' ' } // <-- this is the missing variable
    private var mGame: TicTacToeGame? = null

    fun setGame(game: TicTacToeGame) {
        mGame = game
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBoard(canvas)
        drawMarkers(canvas)
    }

    private fun drawBoard(canvas: Canvas) {
        paint.color = Color.BLACK
        paint.strokeWidth = 10f

        val cellWidth = width / 3f
        val cellHeight = height / 3f

        // Draw vertical lines
        canvas.drawLine(cellWidth, 0f, cellWidth, height.toFloat(), paint)
        canvas.drawLine(2 * cellWidth, 0f, 2 * cellWidth, height.toFloat(), paint)

        // Draw horizontal lines
        canvas.drawLine(0f, cellHeight, width.toFloat(), cellHeight, paint)
        canvas.drawLine(0f, 2 * cellHeight, width.toFloat(), 2 * cellHeight, paint)
    }

    private fun drawMarkers(canvas: Canvas) {
        val cellWidth = width / 3f
        val cellHeight = height / 3f
        val padding = cellWidth / 6f

        paint.strokeWidth = 12f
        paint.style = Paint.Style.STROKE

        for (i in board.indices) {
            val row = i / 3
            val col = i % 3
            val cx = col * cellWidth
            val cy = row * cellHeight

            when (board[i]) {
                'X' -> {
                    paint.color = Color.RED
                    canvas.drawLine(
                        cx + padding, cy + padding,
                        cx + cellWidth - padding, cy + cellHeight - padding, paint
                    )
                    canvas.drawLine(
                        cx + cellWidth - padding, cy + padding,
                        cx + padding, cy + cellHeight - padding, paint
                    )
                }
                'O' -> {
                    paint.color = Color.BLUE
                    canvas.drawCircle(
                        cx + cellWidth / 2,
                        cy + cellHeight / 2,
                        cellWidth / 2.5f, paint
                    )
                }
            }
        }
    }

    fun getBoardCellWidth(): Float = width / 3f
    fun getBoardCellHeight(): Float = height / 3f

    /** Updates the board from a List<String> (Firebase sync or game logic) */
    fun setBoard(boardState: List<String>) {
        for (i in 0 until 9) {
            board[i] = boardState[i].firstOrNull() ?: ' '
        }
        invalidate() // Redraw the board
    }

    /** Optional helper to clear board (used when restarting game) */
    fun clearBoard() {
        for (i in board.indices) board[i] = ' '
        invalidate()
    }


}
