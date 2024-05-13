package com.example.ezfitness

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class HorizontalBarChartView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val barPaint = Paint()
    private val textPaint = Paint()

    private var barData: List<Pair<String, Float>> = emptyList()

    init {
        barPaint.color = Color.BLUE
        textPaint.color = Color.BLACK
        textPaint.textSize = resources.getDimensionPixelSize(R.dimen.text_size).toFloat()
    }

    fun setBarData(data: List<Pair<String, Float>>) {
        barData = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val barWidth = width.toFloat() / barData.size
        var startX = 0f

        barData.forEach { (label, value) ->
            // Draw bar
            val barHeight = height.toFloat() * (value / MAX_VALUE)
            canvas.drawRect(startX, height.toFloat() - barHeight, startX + barWidth, height.toFloat(), barPaint)

            // Draw label
            canvas.drawText(label, startX + barWidth / 2 - textPaint.measureText(label) / 2, height.toFloat() - 16, textPaint)

            startX += barWidth
        }
    }

    companion object {
        private const val MAX_VALUE = 100f // Maximum value for scaling the bars
    }
}
