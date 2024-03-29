package io.github.chenfei0928.util.glide

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Matrix.ScaleToFit
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.widget.ImageView
import com.bumptech.glide.request.transition.Transition
import io.github.chenfei0928.graphics.drawable.DrawableWrapper
import kotlin.math.roundToInt

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-09 15:50
 */
class DrawableCrossFadeTransition(
    private val duration: Int,
    private val isCrossFadeEnabled: Boolean
) : Transition<Drawable> {

    override fun transition(current: Drawable, adapter: Transition.ViewAdapter): Boolean {
        val previous = adapter.currentDrawable?.mutate()?.let { currentDrawable ->
            val imageView = adapter.view as? ImageView
            if (imageView == null) {
                // 目标view不是ImageView
                FixedSizeDrawable(
                    currentDrawable, current.intrinsicHeight, current.intrinsicWidth,
                    ImageView.ScaleType.CENTER_CROP
                )
            } else if (imageView.scaleType !== ImageView.ScaleType.MATRIX) {
                // 非Matrix方式缩放加载的图片
                FixedSizeDrawable(
                    currentDrawable, current.intrinsicHeight, current.intrinsicWidth,
                    imageView.scaleType
                )
            } else {
                // Matrix方式加载的图片，Matrix认为是目标图片的Matrix
                // 将现有图片的Matrix反向操作到目标Matrix
                FixedSizeDrawable(
                    currentDrawable, current.intrinsicHeight, current.intrinsicWidth,
                    ImageView.ScaleType.MATRIX
                ).configMatrix {
                    val imageMatrixValues = FloatArray(9).apply {
                        imageView.imageMatrix.getValues(this)
                        this[Matrix.MSCALE_X] = 1 / this[Matrix.MSCALE_X]
                        this[Matrix.MSKEW_X] = -this[Matrix.MSKEW_X]
                        this[Matrix.MTRANS_X] = -this[Matrix.MTRANS_X]
                        this[Matrix.MSKEW_Y] = -this[Matrix.MSKEW_Y]
                        this[Matrix.MSCALE_Y] = 1 / this[Matrix.MSCALE_Y]
                        this[Matrix.MTRANS_Y] = -this[Matrix.MTRANS_Y]
                        this[Matrix.MPERSP_0] = -this[Matrix.MPERSP_0]
                        this[Matrix.MPERSP_1] = -this[Matrix.MPERSP_1]
                        this[Matrix.MPERSP_2] = 1 / this[Matrix.MPERSP_2]
                    }
                    currentDrawable.setBounds(
                        0, 0, imageView.measuredWidth, imageView.measuredHeight
                    )
                    it.setValues(imageMatrixValues)
                }
            }
        } ?: ColorDrawable(Color.TRANSPARENT)
        val transitionDrawable = TransitionDrawable(arrayOf(previous, current))
        transitionDrawable.isCrossFadeEnabled = isCrossFadeEnabled
        transitionDrawable.startTransition(duration)
        adapter.setDrawable(transitionDrawable)
        return true
    }
}

class FixedSizeDrawable(
    private val drawable: Drawable,
    private val height: Int,
    private val width: Int,
    private val mScaleType: ImageView.ScaleType
) : DrawableWrapper(drawable) {
    private var mDrawMatrix: Matrix? = Matrix()

    override fun getIntrinsicWidth(): Int {
        return width
    }

    override fun getIntrinsicHeight(): Int {
        return height
    }

    override fun getMinimumWidth(): Int {
        return width
    }

    override fun getMinimumHeight(): Int {
        return height
    }

    override fun onBoundsChange(bounds: Rect) {
    }

    override fun draw(canvas: Canvas) {
        canvas.save()
        if (mDrawMatrix != null) {
            canvas.concat(mDrawMatrix)
        }
        drawable.draw(canvas)
        canvas.restore()
    }

    private fun configureBounds() {
        val dwidth: Int = drawable.intrinsicWidth
        val dheight: Int = drawable.intrinsicHeight
        val vwidth: Int = width
        val vheight: Int = height
        val fits = ((dwidth < 0 || vwidth == dwidth)
                && (dheight < 0 || vheight == dheight))
        if (dwidth <= 0 || dheight <= 0 || ImageView.ScaleType.FIT_XY == mScaleType) {
            /* If the drawable has no intrinsic size, or we're told to
                scaletofit, then we just fill our entire view.
            */
            drawable.setBounds(0, 0, vwidth, vheight)
            mDrawMatrix = null
        } else {
            // We need to do the scaling ourself, so have the drawable
            // use its native size.
            drawable.setBounds(0, 0, dwidth, dheight)
            if (ImageView.ScaleType.MATRIX == mScaleType) {
                // Use the specified matrix as-is.
                // use mDrawMatrix
            } else if (fits) {
                // The bitmap fits exactly, no transform needed.
                mDrawMatrix = null
            } else if (ImageView.ScaleType.CENTER == mScaleType) {
                // Center bitmap in view, no scaling.
                mDrawMatrix = Matrix()
                mDrawMatrix?.setTranslate(
                    ((vwidth - dwidth) * 0.5f).roundToInt().toFloat(),
                    ((vheight - dheight) * 0.5f).roundToInt().toFloat()
                )
            } else if (ImageView.ScaleType.CENTER_CROP == mScaleType) {
                mDrawMatrix = Matrix()
                val scale: Float
                var dx = 0f
                var dy = 0f
                if (dwidth * vheight > vwidth * dheight) {
                    scale = vheight.toFloat() / dheight.toFloat()
                    dx = (vwidth - dwidth * scale) * 0.5f
                } else {
                    scale = vwidth.toFloat() / dwidth.toFloat()
                    dy = (vheight - dheight * scale) * 0.5f
                }
                mDrawMatrix?.setScale(scale, scale)
                mDrawMatrix?.postTranslate(dx.roundToInt().toFloat(), dy.roundToInt().toFloat())
            } else if (ImageView.ScaleType.CENTER_INSIDE == mScaleType) {
                mDrawMatrix = Matrix()
                val scale: Float = if (dwidth <= vwidth && dheight <= vheight) {
                    1.0f
                } else {
                    minOf(
                        vwidth.toFloat() / dwidth.toFloat(),
                        vheight.toFloat() / dheight.toFloat()
                    )
                }
                val dx: Float = ((vwidth - dwidth * scale) * 0.5f).roundToInt().toFloat()
                val dy: Float = ((vheight - dheight * scale) * 0.5f).roundToInt().toFloat()
                mDrawMatrix?.setScale(scale, scale)
                mDrawMatrix?.postTranslate(dx, dy)
            } else {
                // Generate the required transform.
                mDrawMatrix = Matrix()
                mDrawMatrix?.setRectToRect(
                    RectF().apply { set(0f, 0f, dwidth.toFloat(), dheight.toFloat()) },
                    RectF().apply { set(0f, 0f, vwidth.toFloat(), vheight.toFloat()) },
                    scaleTypeToScaleToFit(mScaleType)
                )
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
        return sS2FArray[st]!!
    }

    init {
        configureBounds()
    }

    fun configMatrix(block: (Matrix) -> Unit): FixedSizeDrawable = apply {
        if (mScaleType != ImageView.ScaleType.MATRIX) {
            return@apply
        }
        mDrawMatrix?.let(block)
    }
}
