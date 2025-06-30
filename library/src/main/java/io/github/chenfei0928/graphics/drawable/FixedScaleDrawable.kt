package io.github.chenfei0928.graphics.drawable

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Matrix.ScaleToFit
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.widget.ImageView
import androidx.annotation.ReturnThis
import androidx.core.graphics.withSave
import kotlin.math.roundToInt

/**
 * 像 [ImageView] 一样对 [drawable] 的尺寸进行修复，以达到适配当前显示的drawable的宽高
 * 目的是可以在Glide/操作系统的 [TransitionDrawable] 动画时不会产生额外的拉伸
 *
 * @property drawable 要修复处理的图像
 * @property targetHeight 目标高度
 * @property targetWidth 目标宽度
 * @property scaleType 图像缩放方式
 */
class FixedScaleDrawable(
    private val drawable: Drawable,
    private val targetHeight: Int,
    private val targetWidth: Int,
    val scaleType: ImageView.ScaleType
) : DrawableWrapper(drawable) {
    var drawMatrix: Matrix? = Matrix()
        private set

    override fun getIntrinsicWidth(): Int {
        return targetWidth
    }

    override fun getIntrinsicHeight(): Int {
        return targetHeight
    }

    override fun getMinimumWidth(): Int {
        return targetWidth
    }

    override fun getMinimumHeight(): Int {
        return targetHeight
    }

    override fun onBoundsChange(bounds: Rect) {
        // noop
    }

    override fun draw(canvas: Canvas) {
        canvas.withSave {
            if (drawMatrix != null) {
                canvas.concat(drawMatrix)
            }
            drawable.draw(canvas)
        }
    }

    //<editor-fold desc="像 ImageView 一样处理内容显示范围" defaultstatus="collapsed">
    /**
     * 像 [ImageView] 一样处理内容显示范围
     * [ImageView.configureBounds]
     */
    @Suppress("CyclomaticComplexMethod", "LongMethod", "kotlin:S3776")
    private fun configureBounds() {
        val dwidth: Int = drawable.intrinsicWidth
        val dheight: Int = drawable.intrinsicHeight
        val vwidth: Int = targetWidth
        val vheight: Int = targetHeight
        val fits = ((dwidth < 0 || vwidth == dwidth)
                && (dheight < 0 || vheight == dheight))
        if (dwidth <= 0 || dheight <= 0 || ImageView.ScaleType.FIT_XY == scaleType) {
            /* If the drawable has no intrinsic size, or we're told to
                scaletofit, then we just fill our entire view.
            */
            drawable.setBounds(0, 0, vwidth, vheight)
            drawMatrix = null
        } else {
            // We need to do the scaling ourself, so have the drawable
            // use its native size.
            drawable.setBounds(0, 0, dwidth, dheight)
            when {
                ImageView.ScaleType.MATRIX == scaleType -> {
                    // Use the specified matrix as-is.
                    // use mDrawMatrix
                }
                fits -> {
                    // The bitmap fits exactly, no transform needed.
                    drawMatrix = null
                }
                ImageView.ScaleType.CENTER == scaleType -> {
                    // Center bitmap in view, no scaling.
                    drawMatrix = Matrix()
                    drawMatrix?.setTranslate(
                        ((vwidth - dwidth) / 2f).roundToInt().toFloat(),
                        ((vheight - dheight) / 2f).roundToInt().toFloat()
                    )
                }
                ImageView.ScaleType.CENTER_CROP == scaleType -> {
                    drawMatrix = Matrix()
                    val scale: Float
                    var dx = 0f
                    var dy = 0f
                    if (dwidth * vheight > vwidth * dheight) {
                        scale = vheight.toFloat() / dheight.toFloat()
                        dx = (vwidth - dwidth * scale) / 2f
                    } else {
                        scale = vwidth.toFloat() / dwidth.toFloat()
                        dy = (vheight - dheight * scale) / 2f
                    }
                    drawMatrix?.setScale(scale, scale)
                    drawMatrix?.postTranslate(dx.roundToInt().toFloat(), dy.roundToInt().toFloat())
                }
                ImageView.ScaleType.CENTER_INSIDE == scaleType -> {
                    drawMatrix = Matrix()
                    val scale: Float = if (dwidth <= vwidth && dheight <= vheight) {
                        1.0f
                    } else {
                        minOf(
                            vwidth.toFloat() / dwidth.toFloat(),
                            vheight.toFloat() / dheight.toFloat()
                        )
                    }
                    val dx: Float = ((vwidth - dwidth * scale) / 2f).roundToInt().toFloat()
                    val dy: Float = ((vheight - dheight * scale) / 2f).roundToInt().toFloat()
                    drawMatrix?.setScale(scale, scale)
                    drawMatrix?.postTranslate(dx, dy)
                }
                else -> {
                    // Generate the required transform.
                    drawMatrix = Matrix()
                    drawMatrix?.setRectToRect(
                        RectF().apply { set(0f, 0f, dwidth.toFloat(), dheight.toFloat()) },
                        RectF().apply { set(0f, 0f, vwidth.toFloat(), vheight.toFloat()) },
                        scaleTypeToScaleToFit(scaleType)
                    )
                }
            }
        }
    }

    private val sS2FArray = mapOf(
        ImageView.ScaleType.FIT_XY to ScaleToFit.FILL,
        ImageView.ScaleType.FIT_START to ScaleToFit.START,
        ImageView.ScaleType.FIT_CENTER to ScaleToFit.CENTER,
        ImageView.ScaleType.FIT_END to ScaleToFit.END
    )

    private fun scaleTypeToScaleToFit(st: ImageView.ScaleType): ScaleToFit {
        // ScaleToFit enum to their corresponding Matrix.ScaleToFit values
        @Suppress("kotlin:S6611")
        return sS2FArray[st]!!
    }
    //</editor-fold>

    init {
        configureBounds()
    }

    @ReturnThis
    inline fun configMatrix(block: (Matrix) -> Unit): FixedScaleDrawable {
        require(scaleType == ImageView.ScaleType.MATRIX) {
            "current drawable is not MATRIX"
        }
        drawMatrix?.let(block)
        return this
    }
}
