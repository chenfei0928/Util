package io.github.chenfei0928.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import io.github.chenfei0928.util.R
import io.github.chenfei0928.view.OutlineType
import kotlin.math.max

/**
 * 通过outline实现对ImageView进行圆角、圆形显示的支持，只支持5.0及以上系统。
 * RoundImageView对硬件加速图像和加载动画支持状况不佳，
 * 而通过imageView+outline可实现效果的同时允许硬件加速的bitmap来优化性能。
 *
 * [Android中绘制圆角的三种方式](https://teoking.github.io/post/draw_round_corner_3ways_in_android/)
 *
 * [InfoQ镜像](https://xie.infoq.cn/article/83198028c199a3ae284e63eb6)
 *
 * @constructor 构造器，用于框架反射构建实例
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-01-25 11:10
 */
open class OutlineClipImageView
@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        outlineProvider = context.obtainStyledAttributes(
            attrs, R.styleable.OutlineClipImageView, defStyleAttr, 0
        ).use { a ->
            when {
                a.getBoolean(R.styleable.OutlineClipImageView_olciv_clipToBackground, false) -> {
                    // 根据背景Drawable
                    OutlineType.Background
                }
                a.getBoolean(R.styleable.OutlineClipImageView_olciv_oval, false) -> {
                    // 圆形
                    OutlineType.Oval
                }
                else -> when (val cornerRadius = max(
                    MIN_RADIUS,
                    a.getDimension(
                        R.styleable.OutlineClipImageView_olciv_cornerRadius, DEFAULT_RADIUS
                    )
                )) {
                    // 圆角
                    MIN_RADIUS -> null
                    else -> OutlineType.SameCornerRadius(cornerRadius)
                }
            }
        }
        clipToOutline = true
    }

    companion object {
        private const val MIN_RADIUS = 0f
        const val DEFAULT_RADIUS = MIN_RADIUS
    }
}
