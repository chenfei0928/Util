package io.github.chenfei0928.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import io.github.chenfei0928.animation.BezierEvaluator
import java.util.Random

/**
 * [来源博客](https://blog.csdn.net/u010302765/article/details/70843831)
 */
class LikeLayout
@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val random = Random()
    private var params: LayoutParams = LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply {
        gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
    }

    // 插值器
    private val interpolates: Array<Interpolator> = arrayOf(
        // 在动画开始与结束的地方速率改变比较慢，在中间的时候加速
        AccelerateDecelerateInterpolator(),
        // 在动画开始的地方速率改变比较慢，然后开始加速
        AccelerateInterpolator(),
        // 在动画开始的地方快然后慢
        DecelerateInterpolator(),
        // 以常量速率改变
        LinearInterpolator()
    )

    fun addLoveView(drawable: Drawable) {
        val iv = ImageView(context)
        iv.layoutParams = params
        iv.setImageDrawable(drawable)
        addView(iv)
        // 开启动画，并且用完销毁
        val set: AnimatorSet = getAnimatorSet(iv)
        set.start()
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                removeView(iv)
            }
        })
    }

    /**
     * 获取动画集合
     * @param iv
     */
    private fun getAnimatorSet(iv: ImageView): AnimatorSet {
        // 1.alpha动画
        val alpha = ObjectAnimator.ofFloat(iv, View.ALPHA, 0.3f, 1f)
        // 2.缩放动画
        val scaleX = ObjectAnimator.ofFloat(iv, View.SCALE_X, 0.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(iv, View.SCALE_Y, 0.2f, 1f)
        // 动画集合
        val set = AnimatorSet()
        set.playTogether(alpha, scaleX, scaleY)
        set.duration = 300.toLong()
        // 贝塞尔曲线动画
        val bzier: ValueAnimator = getBezierAnimator(iv)
        val set2 = AnimatorSet()
        set2.playSequentially(set, bzier)
        set2.setTarget(iv)
        return set2
    }

    /**
     * 贝塞尔动画
     */
    private fun getBezierAnimator(iv: ImageView): ValueAnimator {
        // 4个点的坐标
        val pointFs = getPointFs(iv)
        val evaluator = BezierEvaluator(pointFs[1], pointFs[2])
        val valueAnim = ValueAnimator.ofObject(evaluator, pointFs[0], pointFs[3])
        valueAnim.addUpdateListener { animation ->
            val p = animation.animatedValue as PointF
            iv.x = p.x
            iv.y = p.y
            // 透明度
            iv.alpha = 1 - animation.animatedFraction
        }
        valueAnim.setTarget(iv)
        valueAnim.duration = 3000
        valueAnim.interpolator = interpolates[random.nextInt(4)]
        return valueAnim
    }

    private fun getPointFs(iv: ImageView): Array<PointF> {
        val mWidth = measuredWidth
        val mHeight = measuredHeight
        return arrayOf(
            // p0
            PointF(
                (mWidth - iv.drawable.intrinsicWidth) / 2f,
                mHeight - iv.drawable.intrinsicHeight.toFloat()
            ),
            // p1
            PointF(
                random
                    .nextInt(mWidth)
                    .toFloat(),
                (random.nextInt(mHeight / 2) + mHeight / 2 + iv.drawable.intrinsicHeight).toFloat()
            ),
            // p2
            PointF(
                random
                    .nextInt(mWidth)
                    .toFloat(),
                random
                    .nextInt(mHeight / 2)
                    .toFloat()
            ),
            // p3
            PointF(
                random
                    .nextInt(mWidth)
                    .toFloat(), 0f
            ),
        )
    }
}
