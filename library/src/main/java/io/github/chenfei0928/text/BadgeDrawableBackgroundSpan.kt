package io.github.chenfei0928.text

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.style.ReplacementSpan
import androidx.annotation.Px
import kotlin.math.max

/**
 * 带有左侧图标和背景的span样式，适用于不改变字号的场景
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-08-04 15:27
 */
class BadgeDrawableBackgroundSpan(
    private val drawableLeft: Drawable?,
    @Px
    private val drawablePadding: Int,
    private val background: Drawable
) : ReplacementSpan() {
    private val bgPaddingRect = Rect().apply {
        background.getPadding(this)
    }
    private var width = 0

    override fun getSize(
        paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?
    ): Int {
        val textWidth = paint
            .measureText(text, start, end)
            .toInt()
        width = bgPaddingRect.left + if (drawableLeft == null) {
            0
        } else {
            drawableLeft.intrinsicWidth + drawablePadding
        } + textWidth + bgPaddingRect.right
        // 根据背景图padding，调整文字绘制位置
        fm?.let {
            // 计算出文字与图标高度差的一半，为文字偏移
            val heightDif = run {
                val fontHeight = fm.bottom - fm.top
                val contentHeight = max((drawableLeft?.intrinsicHeight ?: 0), fontHeight)
                val drawableHeight = max(
                    contentHeight + bgPaddingRect.bottom + bgPaddingRect.top,
                    background.minimumHeight
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
        return width
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
        // 计算得到使文字居中的基线位置
        val baseline = run {
            val fm = paint.fontMetricsInt
            val fontHeight = fm.bottom - fm.top
            if (drawableLeft != null) {
                (drawableLeft.intrinsicHeight - fontHeight) / 2 - fm.top
            } else {
                0
            }
        }
        // 调整背景图尺寸范围并绘制背景图
        background.setBounds(x.toInt(), top, width, bottom)
        background.draw(canvas)
        // 绘制左侧图标
        drawableLeft?.let {
            val saveCount = canvas.save()
            canvas.translate(bgPaddingRect.left.toFloat(), bgPaddingRect.top.toFloat())
            drawableLeft.draw(canvas)
            canvas.restoreToCount(saveCount)
        }
        // 绘制文字
        text?.let {
            val drawableLeftWidthAndPadding = if (drawableLeft == null) {
                0f
            } else {
                drawableLeft.bounds.right + drawablePadding.toFloat()
            }
            canvas.drawText(
                text,
                start,
                end,
                x + bgPaddingRect.left.toFloat() + drawableLeftWidthAndPadding,
                baseline.toFloat() + bgPaddingRect.top,
                paint
            )
        }
    }
}
