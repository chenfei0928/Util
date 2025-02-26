package io.github.chenfei0928.util.glide

import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.widget.ImageView
import androidx.core.graphics.values
import com.bumptech.glide.request.transition.Transition
import io.github.chenfei0928.graphics.drawable.FixedScaleDrawable

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
        val currentDrawable = adapter.currentDrawable
        val previous = if (currentDrawable == null) {
            ColorDrawable(Color.TRANSPARENT)
        } else {
            val imageView = adapter.view as? ImageView
            // 使用当前正在显示的drawable尺寸构建目标图片尺寸的drawable，并传入想要的ScaleType缩放方式
            if (imageView == null) {
                // 目标view不是ImageView
                FixedScaleDrawable(
                    currentDrawable, current.intrinsicHeight, current.intrinsicWidth,
                    ImageView.ScaleType.CENTER_CROP
                )
            } else if (imageView.scaleType !== ImageView.ScaleType.MATRIX) {
                // 非Matrix方式缩放加载的图片
                FixedScaleDrawable(
                    currentDrawable, current.intrinsicHeight, current.intrinsicWidth,
                    imageView.scaleType
                )
            } else {
                // Matrix方式加载的图片，Matrix认为是目标图片的Matrix
                // 将现有图片的Matrix反向操作到目标Matrix
                FixedScaleDrawable(
                    currentDrawable, current.intrinsicHeight, current.intrinsicWidth,
                    ImageView.ScaleType.MATRIX
                ).configMatrix {
                    // 加载matrix阵列，并对操作数取反/倒数
                    val imageMatrixValues = imageView.imageMatrix.values().apply {
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
        }
        // 创建过渡动画Drawable并设置
        val transitionDrawable = TransitionDrawable(arrayOf(previous, current))
        transitionDrawable.isCrossFadeEnabled = isCrossFadeEnabled
        transitionDrawable.startTransition(duration)
        adapter.setDrawable(transitionDrawable)
        return true
    }
}
