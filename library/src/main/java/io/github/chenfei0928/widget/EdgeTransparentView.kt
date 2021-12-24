package io.github.chenfei0928.widget

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import io.github.chenfei0928.util.R

/**
 * [相关来源](https://github.com/qinci/EdgeTranslucent/blob/master/lib/src/main/java/me/qinc/lib/edgetranslucent/EdgeTransparentView.java)O
 */
class EdgeTransparentView
@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var edgePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    }
    private var position = 0
    private var drawSize = 0f

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.EdgeTransparentView)
        position = typedArray.getInt(R.styleable.EdgeTransparentView_edge_position, 0)
        drawSize = typedArray.getDimension(R.styleable.EdgeTransparentView_edge_width, 0f)
        typedArray.recycle()
        edgePaint.shader = LinearGradient(
            0f, 0f, 0f, drawSize,
            //渐变颜色
            intArrayOf(Color.WHITE, Color.TRANSPARENT),
            //渐变位置
            floatArrayOf(0f, 1f), Shader.TileMode.CLAMP
        )
    }

    override fun drawChild(canvas: Canvas, child: View?, drawingTime: Long): Boolean {
        val layerSave = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        } else {
            canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null, Canvas.ALL_SAVE_FLAG)
        }
        val drawChild = super.drawChild(canvas, child, drawingTime)

        val width = measuredWidth
        val height = measuredHeight
        if (position == 0 || position and TOP_MASK != 0) {
            canvas.drawRect(0f, 0f, width.toFloat(), drawSize, edgePaint)
        }
        if (position == 0 || position and BOTTOM_MASK != 0) {
            val save = canvas.save()
            canvas.rotate(180f, width / 2f, height / 2f)
            canvas.drawRect(0f, 0f, width.toFloat(), drawSize, edgePaint)
            canvas.restoreToCount(save)
        }
        val offset = (height - width) / 2f
        if (position == 0 || position and LEFT_MASK != 0) {
            val saveCount = canvas.save()
            canvas.rotate(270f, width / 2f, height / 2f)
            canvas.translate(0f, offset)
            canvas.drawRect(0 - offset, 0f, width + offset, drawSize, edgePaint)
            canvas.restoreToCount(saveCount)
        }
        if (position == 0 || position and RIGHT_MASK != 0) {
            val saveCount = canvas.save()
            canvas.rotate(90f, width / 2f, height / 2f)
            canvas.translate(0f, offset)
            canvas.drawRect(0 - offset, 0f, width + offset, drawSize, edgePaint)
            canvas.restoreToCount(saveCount)
        }
        canvas.restoreToCount(layerSave)
        return drawChild
    }

    companion object {
        private const val TOP_MASK = 0x01
        private const val BOTTOM_MASK = TOP_MASK shl 1
        private const val LEFT_MASK = TOP_MASK shl 2
        private const val RIGHT_MASK = TOP_MASK shl 3
    }
}
