package io.github.chenfei0928.text

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.text.style.ReplacementSpan
import androidx.annotation.IntRange

class TopGravitySpan : ReplacementSpan() {

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: FontMetricsInt?
    ): Int {
        return paint.measureText(text, start, end).toInt()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        @IntRange(from = 0) start: Int,
        @IntRange(from = 0) end: Int,
        x: Float, top: Int, y: Int, bottom: Int,
        paint: Paint
    ) {
        // 此行顶端坐标减去上文字顶端距离基线距离（负数）
        // y参数传入的是要绘制文字的基线baseline
        canvas.drawText(text, start, end, x, top - paint.ascent(), paint)
    }
}
