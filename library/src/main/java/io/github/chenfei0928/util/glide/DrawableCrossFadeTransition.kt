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
import androidx.annotation.ReturnThis
import com.bumptech.glide.request.transition.Transition
import io.github.chenfei0928.graphics.drawable.DrawableWrapper
import kotlin.math.roundToInt

/**
 * 修复Glide官方处理方式中对长宽比不一致图片过渡时会拉伸图片的bug
 *
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
            // 使用当前正在显示的drawable尺寸构建目标图片尺寸的drawable，并传入想要的ScaleType缩放方式
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
                        // 加载matrix阵列，并对操作数取反/倒数
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
        // 创建过渡动画Drawable并设置
        val transitionDrawable = TransitionDrawable(arrayOf(previous, current))
        transitionDrawable.isCrossFadeEnabled = isCrossFadeEnabled
        transitionDrawable.startTransition(duration)
        adapter.setDrawable(transitionDrawable)
        return true
    }
}

/**
 * 像 [ImageView] 一样对 [drawable] 的尺寸进行修复，以达到适配当前显示的drawable的宽高
 * 目的是可以在Glide/操作系统Modena的 [TransitionDrawable] 动画时不会产生额外的拉伸
 *
 * @property drawable 要修复处理的图像
 * @property targetHeight 目标高度
 * @property targetWidth 目标宽度
 * @property scaleType 图像缩放方式
 */
class FixedSizeDrawable(
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
        canvas.save()
        if (drawMatrix != null) {
            canvas.concat(drawMatrix)
        }
        drawable.draw(canvas)
        canvas.restore()
    }

    //<editor-fold desc="像 ImageView 一样处理内容显示范围" defaultstatus="collapsed">
    /**
     * 像 [ImageView] 一样处理内容显示范围
     * [ImageView.configureBounds]
     */
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
                        ((vwidth - dwidth) * 0.5f).roundToInt().toFloat(),
                        ((vheight - dheight) * 0.5f).roundToInt().toFloat()
                    )
                }
                ImageView.ScaleType.CENTER_CROP == scaleType -> {
                    drawMatrix = Matrix()
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
                    val dx: Float = ((vwidth - dwidth * scale) * 0.5f).roundToInt().toFloat()
                    val dy: Float = ((vheight - dheight * scale) * 0.5f).roundToInt().toFloat()
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
        return sS2FArray[st]!!
    }
    //</editor-fold>

    init {
        configureBounds()
    }

    @ReturnThis
    inline fun configMatrix(block: (Matrix) -> Unit): FixedSizeDrawable {
        require(scaleType == ImageView.ScaleType.MATRIX) {
            "current drawable is not MATRIX"
        }
        drawMatrix?.let(block)
        return this
    }
}
