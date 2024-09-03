package io.github.chenfei0928.graphics

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.util.Property
import android.view.Gravity
import androidx.annotation.ColorInt
import androidx.annotation.GravityInt
import androidx.annotation.Px
import kotlin.math.min

/**
 * 绘制圆弧的Drawable
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-04-14 10:38
 */
class CircularArcDrawable : PaintDrawable() {
    private val oval = RectF()

    @GravityInt
    var gravity = Gravity.LEFT
        set(value) {
            field = value
            resetR()
        }

    @Px
    var radius: Float = 0f
        set(value) {
            if (value != field) {
                field = value
                resetOvalBounds()
                invalidateSelf()
            }
        }

    init {
        paint.apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }
    }

    fun setColor(@ColorInt color: Int) {
        paint.color = color
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        resetOvalBounds()
    }

    override fun draw(canvas: Canvas) {
        canvas.drawOval(oval, paint)
    }

    private fun resetR() {
        radius = when (gravity) {
            Gravity.LEFT -> {
                bounds.height().toFloat()
            }
            Gravity.TOP -> {
                bounds.width().toFloat()
            }
            Gravity.RIGHT -> {
                bounds.height().toFloat()
            }
            Gravity.BOTTOM -> {
                bounds.width().toFloat()
            }
            else -> {
                min(bounds.width(), bounds.height()).toFloat()
            }
        }
    }

    private fun resetOvalBounds() {
        val bounds = bounds
        val width = bounds.width()
        val height = bounds.height()
        val r = radius
        when (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Gravity.getAbsoluteGravity(gravity, layoutDirection)
        } else {
            gravity
        }) {
            Gravity.LEFT, Gravity.START -> {
                oval.setOval(0f, height / 2f, r)
            }
            Gravity.TOP -> {
                oval.setOval(width / 2f, 0f, r)
            }
            Gravity.RIGHT, Gravity.END -> {
                oval.setOval(width.toFloat(), height / 2f, r)
            }
            Gravity.BOTTOM -> {
                oval.setOval(width / 2f, height.toFloat(), r)
            }
        }
    }

    companion object {
        val RADIUS = object : Property<CircularArcDrawable, Float>(Float::class.java, "radius") {
            override fun get(`object`: CircularArcDrawable): Float {
                return `object`.radius
            }

            override fun set(`object`: CircularArcDrawable, value: Float) {
                `object`.radius = value
            }
        }
    }
}
