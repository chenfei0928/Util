package io.github.chenfei0928.text

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.text.style.ReplacementSpan
import androidx.annotation.IntRange
import androidx.annotation.Px

/**
 * Created by MrFeng on 2017/6/1.
 */
class HorizontalSpaceSpan(
    @all:Px private val mHorizontalSpace: Int
) : ReplacementSpan() {

    override fun getSize(
        paint: Paint, text: CharSequence,
        @IntRange(from = 0) start: Int, @IntRange(from = 0) end: Int,
        fm: FontMetricsInt?
    ): Int {
        return mHorizontalSpace
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        @IntRange(from = 0) start: Int,
        @IntRange(from = 0) end: Int,
        x: Float, top: Int, y: Int, bottom: Int,
        paint: Paint
    ) {
        // noop
    }
}
