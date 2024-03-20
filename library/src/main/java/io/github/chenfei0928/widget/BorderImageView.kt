package io.github.chenfei0928.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import io.github.chenfei0928.util.R
import io.github.chenfei0928.view.OutlineType

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-01-25 17:21
 */
class BorderImageView
@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : OutlineClipImageView(context, attrs, defStyleAttr) {

    var borderColor: ColorStateList? = null
        set(value) {
            field = value
            mBorderPaint.color = borderColor?.getColorForState(drawableState, DEFAULT_BORDER_COLOR)
                ?: DEFAULT_BORDER_COLOR
        }
    private val mBorderPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = borderColor?.getColorForState(drawableState, DEFAULT_BORDER_COLOR)
            ?: DEFAULT_BORDER_COLOR
    }

    var borderWidth: Float
        set(value) {
            mBorderPaint.strokeWidth = value
        }
        get() = mBorderPaint.strokeWidth

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BorderImageView, defStyleAttr, 0)
        borderWidth = a.getDimension(R.styleable.BorderImageView_biv_borderWidth, 0f)
        borderColor = a.getColorStateList(R.styleable.BorderImageView_biv_borderColor)
        a.recycle()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        val newColor = borderColor?.getColorForState(drawableState, DEFAULT_BORDER_COLOR)
            ?: DEFAULT_BORDER_COLOR
        if (mBorderPaint.color != newColor) {
            mBorderPaint.color = newColor
        }
    }

    override fun setColorFilter(cf: ColorFilter?) {
        super.setColorFilter(cf)
        mBorderPaint.colorFilter = cf
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (borderWidth <= 0) {
            return
        }
        val rectF = RectF(
            paddingLeft.toFloat() + borderWidth / 2,
            paddingTop.toFloat() + borderWidth / 2,
            width - paddingRight.toFloat() - borderWidth / 2,
            height - paddingBottom.toFloat() - borderWidth / 2
        )
        val provider = outlineProvider as? OutlineType
        provider?.drawBorder(canvas, mBorderPaint, rectF)
    }

    companion object {
        const val DEFAULT_BORDER_COLOR = Color.BLACK
    }
}
