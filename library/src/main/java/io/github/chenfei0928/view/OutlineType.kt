package io.github.chenfei0928.view

import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import io.github.chenfei0928.util.Log

/**
 * 通过outline实现对View进行圆角、圆形显示的支持，只支持5.0及以上系统。
 * RoundImageView对硬件加速图像和加载动画支持状况不佳，
 * 而通过imageView+outline可实现效果的同时允许硬件加速的bitmap来优化性能。
 *
 * [Android中绘制圆角的三种方式](https://teoking.github.io/post/draw_round_corner_3ways_in_android/)
 *
 * [InfoQ镜像](https://xie.infoq.cn/article/83198028c199a3ae284e63eb6)
 *
 * @author chenf()
 * @date 2024-01-26 17:38
 */
sealed class OutlineType : ViewOutlineProvider() {

    /**
     * 绘制边线
     *
     * @param canvas
     * @param paint
     * @param rectF
     */
    abstract fun drawBorder(view: View, canvas: Canvas, paint: Paint, rectF: RectF)

    /**
     * 裁剪到圆形
     */
    data object Oval : OutlineType() {
        override fun drawBorder(view: View, canvas: Canvas, paint: Paint, rectF: RectF) {
            canvas.drawOval(rectF, paint)
        }

        override fun getOutline(view: View, outline: Outline) {
            outline.setOval(
                view.paddingLeft,
                view.paddingTop,
                view.width - view.paddingRight,
                view.height - view.paddingBottom
            )
        }
    }

    /**
     * 根据[View.getBackground]的[Drawable.getOutline]裁剪
     */
    data object Background : OutlineType() {
        val path: Path = Path()

        override fun drawBorder(view: View, canvas: Canvas, paint: Paint, rectF: RectF) {
            when (val d = view.background) {
                is GradientDrawable -> drawBroder(view, d, canvas, paint, rectF)
                else -> {
                    Log.w(TAG, "drawBorder: clipToBackground 时不支持绘制边框: ${d.javaClass}")
                }
            }
        }

        private fun drawBroder(
            view: View, d: GradientDrawable, canvas: Canvas, paint: Paint, rectF: RectF
        ): Unit = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.w(TAG, "drawBorder: clipToBackground 时不支持绘制边框: GradientDrawable")
        } else when (d.shape) {
            GradientDrawable.LINE -> Log.w(TAG, run {
                "drawBorder: clipToBackground 时不支持绘制边框: GradientDrawable shape is LINE"
            })
            GradientDrawable.OVAL -> {
                canvas.drawOval(rectF, paint)
            }
            GradientDrawable.RECTANGLE -> {
                val radius = d.cornerRadius
                if (radius != 0f) {
                    canvas.drawRoundRect(rectF, radius, radius, paint)
                } else {
                    path.rewind()
                    path.addRoundRect(
                        view.paddingLeft.toFloat(),
                        view.paddingTop.toFloat(),
                        view.width - view.paddingRight.toFloat(),
                        view.height - view.paddingBottom.toFloat(),
                        d.cornerRadii!!,
                        Path.Direction.CW
                    )
                    canvas.drawPath(path, paint)
                }
            }
            GradientDrawable.RING -> {
                canvas.drawOval(rectF, paint)
            }
            else -> Log.w(TAG, run {
                "drawBorder: clipToBackground 时不支持绘制边框: GradientDrawable shape is ${d.shape}"
            })
        }

        override fun getOutline(view: View?, outline: Outline?) {
            BACKGROUND.getOutline(view, outline)
        }
    }

    /**
     * 四个角圆角大小一致
     *
     * 可直接更新[cornerRadius]，后调用[View.invalidateOutline]方法即可
     */
    data class SameCornerRadius(
        var cornerRadius: Float
    ) : OutlineType() {
        override fun drawBorder(view: View, canvas: Canvas, paint: Paint, rectF: RectF) {
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)
        }

        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(
                view.paddingLeft,
                view.paddingTop,
                view.width - view.paddingRight,
                view.height - view.paddingBottom,
                cornerRadius
            )
        }
    }

    /**
     * 基于[Path]
     *
     * 可直接更新[path]，后调用[View.invalidateOutline]方法即可
     */
    open class PathType : OutlineType() {
        val path: Path = Path()

        override fun drawBorder(view: View, canvas: Canvas, paint: Paint, rectF: RectF) {
            canvas.drawPath(path, paint)
        }

        override fun getOutline(view: View, outline: Outline) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                outline.setPath(path)
            } else {
                @Suppress("DEPRECATION")
                outline.setConvexPath(path)
            }
        }
    }

    /**
     * 自定义圆角大小
     *
     * 可直接更新[cornerRadii]块，后调用[View.invalidateOutline]方法即可
     *
     * @property cornerRadii 圆角大小，类似[GradientDrawable.setCornerRadii]，或[Path.addRoundRect]
     */
    data class CornerRadii(
        var cornerRadii: FloatArray
    ) : PathType() {
        override fun getOutline(view: View, outline: Outline) {
            path.rewind()
            path.addRoundRect(
                view.paddingLeft.toFloat(),
                view.paddingTop.toFloat(),
                view.width - view.paddingRight.toFloat(),
                view.height - view.paddingBottom.toFloat(),
                cornerRadii,
                Path.Direction.CW
            )
            super.getOutline(view, outline)
        }
    }

    /**
     * 需要view尺寸配置自定义路径
     *
     * 可直接更新[block]块，后调用[View.invalidateOutline]方法即可
     */
    class ViewPath(
        var block: (view: View, path: Path) -> Unit
    ) : PathType() {
        override fun getOutline(view: View, outline: Outline) {
            path.rewind()
            block(view, path)
            super.getOutline(view, outline)
        }
    }

    companion object {
        private const val TAG = "KW_OutlineType"
    }
}
