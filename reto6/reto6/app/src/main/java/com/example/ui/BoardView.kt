package com.example.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.logic.TicTacToeGame
import com.example.reto5.R

class BoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val boardPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 8f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private var boardRect = Rect()
    private var cellWidth: Int = 0
    private var cellHeight: Int = 0

    private var humanBitmap: Bitmap? = null
    private var computerBitmap: Bitmap? = null

    private var game: TicTacToeGame? = null

    fun setGame(game: TicTacToeGame) {
        this.game = game
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        boardRect.set(0, 0, w, h)

        cellWidth = w / 3
        cellHeight = h / 3

        // Scale bitmaps once to fit cells
        humanBitmap = Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(resources, R.drawable.x_img),
            cellWidth,
            cellHeight,
            true
        )

        computerBitmap = Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(resources, R.drawable.o_img),
            cellWidth,
            cellHeight,
            true
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw grid
        for (i in 1..2) {

            canvas.drawLine(
                (cellWidth * i).toFloat(), 0f,
                (cellWidth * i).toFloat(), height.toFloat(),
                boardPaint
            )

            canvas.drawLine(
                0f, (cellHeight * i).toFloat(),
                width.toFloat(), (cellHeight * i).toFloat(),
                boardPaint
            )
        }

        game?.let { game ->
            for (i in 0 until TicTacToeGame.BOARD_SIZE) {
                val row = i / 3
                val col = i % 3

                val left = col * cellWidth
                val top = row * cellHeight
                val destRect = Rect(left, top, left + cellWidth, top + cellHeight)

                when (game.getBoardOccupant(i)) {
                    TicTacToeGame.HUMAN_PLAYER -> {
                        humanBitmap?.let {
                            canvas.drawBitmap(it, null, destRect, null)
                        }
                    }
                    TicTacToeGame.COMPUTER_PLAYER -> {
                        computerBitmap?.let {
                            canvas.drawBitmap(it, null, destRect, null)
                        }
                    }
                }
            }
        }
    }
    fun getBoardCellWidth(): Int {
        return cellWidth
    }

    fun getBoardCellHeight(): Int {
        return cellHeight
    }
}
