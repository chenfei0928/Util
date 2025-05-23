package io.github.chenfei0928.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import androidx.core.content.res.use
import io.github.chenfei0928.util.R
import io.github.chenfei0928.view.OutlineType

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-01-25 17:21
 */
open class BorderImageView
@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : OutlineClipImageView(context, attrs, defStyleAttr) {

    private val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = DEFAULT_BORDER_COLOR
    }
    var borderColor: ColorStateList? = null
        set(value) {
            field = value
            val newColor = borderColor?.getColorForState(drawableState, DEFAULT_BORDER_COLOR)
                ?: DEFAULT_BORDER_COLOR
            if (borderPaint.color != newColor) {
                borderPaint.color = newColor
                invalidate()
            }
        }
    var borderWidth: Float
        set(value) {
            if (borderPaint.strokeWidth != value) {
                borderPaint.strokeWidth = value
                invalidate()
            }
        }
        get() = borderPaint.strokeWidth

    init {
        context.obtainStyledAttributes(
            attrs, R.styleable.BorderImageView, defStyleAttr, 0
        ).use { a ->
            borderWidth = a.getDimension(R.styleable.BorderImageView_biv_borderWidth, 0f)
            borderColor = a.getColorStateList(R.styleable.BorderImageView_biv_borderColor)
        }
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        val newColor = borderColor?.getColorForState(drawableState, DEFAULT_BORDER_COLOR)
            ?: DEFAULT_BORDER_COLOR
        if (borderPaint.color != newColor) {
            borderPaint.color = newColor
            invalidate()
        }
    }

    override fun setColorFilter(cf: ColorFilter?) {
        super.colorFilter = cf
        borderPaint.colorFilter = cf
    }

    private val tmpRectF = RectF()

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (borderWidth <= 0) {
            return
        }
        val provider = outlineProvider as? OutlineType
            ?: return
        @Suppress("kotlin:S6518")
        tmpRectF.set(
            paddingLeft.toFloat() + borderWidth / 2,
            paddingTop.toFloat() + borderWidth / 2,
            width - paddingRight.toFloat() - borderWidth / 2,
            height - paddingBottom.toFloat() - borderWidth / 2
        )
        provider.drawBorder(this, canvas, borderPaint, tmpRectF)
    }

    companion object {
        const val DEFAULT_BORDER_COLOR = Color.BLACK
    }
}
