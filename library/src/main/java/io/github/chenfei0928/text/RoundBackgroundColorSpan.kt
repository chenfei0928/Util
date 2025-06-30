package io.github.chenfei0928.text

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.graphics.RectF
import android.text.style.ReplacementSpan
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.annotation.Px

/**
 * 背景为圆角纯色的[android.text.style.BackgroundColorSpan]
 *
 * [原博客](http://blog.csdn.net/zyldzs27/article/details/75091299)
 *
 * @author MrFeng
 * @date 2017/12/28
 */
class RoundBackgroundColorSpan(
    // 圆角的半径
    @param:Px var radius: Int = 15,
    @param:ColorInt private val bgColor: Int,
    @param:ColorInt private val textColor: Int,
    // 文本内容相对圆角背景水平方向的内边距
    private val paddingHorizontal: Int = 20,
    // 文本内容相对圆角背景垂直方向的内边距
    private val paddingVertical: Int = 1,
) : ReplacementSpan() {
    private val mTmpRectF = RectF()

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: FontMetricsInt?
    ): Int {
        return paint.measureText(text, start, end).toInt() + paddingHorizontal * 2
    }

    override fun draw(
        canvas: Canvas, text: CharSequence,
        @IntRange(from = 0) start: Int,
        @IntRange(from = 0) end: Int,
        x: Float, top: Int, y: Int, bottom: Int,
        paint: Paint
    ) {
        val originalColor = paint.color
        paint.color = bgColor
        // 设置圆角背景绘制区域
        @Suppress("kotlin:S6518")
        mTmpRectF.set(
            x,
            (top + paddingVertical).toFloat(),
            x + (paint.measureText(text, start, end).toInt() + paddingHorizontal * 2),
            (bottom - paddingVertical).toFloat()
        )
        // 绘制圆角背景
        canvas.drawRoundRect(mTmpRectF, radius.toFloat(), radius.toFloat(), paint)
        paint.color = textColor
        // 绘制文字
        canvas.drawText(text, start, end, x + paddingHorizontal, y.toFloat(), paint)
        paint.color = originalColor
    }
}
