package io.github.chenfei0928.widget

import android.annotation.TargetApi
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import androidx.resourceinspection.annotation.AppCompatShadowedAttributes
import androidx.resourceinspection.annotation.Attribute
import io.github.chenfei0928.util.Log
import io.github.chenfei0928.util.R

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-01-25 17:21
 */
@AppCompatShadowedAttributes
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class BorderImageView
@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : OutlineClipImageView(context, attrs, defStyleAttr) {

    @get:Attribute(value = "io.github.chenfei0928:biv_borderColor")
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

    @get:Attribute(value = "io.github.chenfei0928:biv_borderWidth")
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
        when {
            clipToBackground -> {
                Log.w(TAG, "draw: clipToBackground 时不支持绘制边框")
            }
            isOval -> {
                // 圆/椭圆形
                canvas.drawOval(rectF, mBorderPaint)
            }
            cornerRadius != DEFAULT_RADIUS -> {
                // 四圆角同等半径
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, mBorderPaint)
            }
            else -> {
                // 绘制矩形
                canvas.drawRect(rectF, mBorderPaint)
            }
        }
    }

    companion object {
        private const val TAG = "KW_BorderImageView"
        const val DEFAULT_BORDER_COLOR = Color.BLACK
    }
}
