package io.github.chenfei0928.text

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.style.ReplacementSpan
import kotlin.math.max

/**
 * 带有[Drawable]背景的span
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-08-04 15:27
 */
class DrawableBackgroundSpan(
    private val background: Drawable
) : ReplacementSpan() {
    private val bgPaddingRect = Rect().apply {
        background.getPadding(this)
    }
    private var size = 0

    override fun getSize(
        paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?
    ): Int {
        val textWidth = paint
            .measureText(text, start, end)
            .toInt()
        size = bgPaddingRect.left + textWidth + bgPaddingRect.right
        // 根据背景图padding，调整文字绘制位置
        fm?.let {
            // 计算出文字与图标高度差的一半，为文字偏移
            val heightDif = run {
                val fontHeight = fm.bottom - fm.top
                val drawableHeight = max(
                    fontHeight + bgPaddingRect.bottom + bgPaddingRect.top, background.minimumHeight
                )
                // 通过文字高度与图标高度差计算文字需要向下偏移的量
                drawableHeight - fontHeight
            }
            // 将文字坡度线与左侧坐标纵向对其
            fm.ascent += (heightDif + 1) / 2
            fm.descent += (heightDif + 1) / 2
            // 底线需要撑高行高到与图标高度一致
            fm.bottom += heightDif
            fm.leading += heightDif
        }
        return size
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        background.setBounds(x.toInt(), top, size, bottom)
        background.draw(canvas)
        // 绘制文字
        text?.let {
            canvas.drawText(text, start, end, x + bgPaddingRect.left, y.toFloat(), paint)
        }
    }
}
