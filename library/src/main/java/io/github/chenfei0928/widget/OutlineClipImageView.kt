package io.github.chenfei0928.widget

import android.content.Context
import android.graphics.Outline
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import androidx.appcompat.widget.AppCompatImageView
import io.github.chenfei0928.util.R
import kotlin.math.max

/**
 * 通过outline实现对ImageView进行圆角、圆形显示的支持，只支持5.0及以上系统。
 * RoundImageView对硬件加速图像和加载动画支持状况不佳，
 * 而通过imageView+outline可实现效果的同时允许硬件加速的bitmap来优化性能。
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-01-25 11:10
 */
open class OutlineClipImageView
@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    var isOval: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    var cornerRadius = DEFAULT_RADIUS
        set(value) {
            field = value
            invalidate()
        }

    var clipToBackground: Boolean
        get() = outlineProvider == ViewOutlineProvider.BACKGROUND
        set(value) {
            outlineProvider = if (value) {
                ViewOutlineProvider.BACKGROUND
            } else {
                OUTLINE_PROVIDER
            }
            clipToOutline = true
        }

    init {
        val a =
            context.obtainStyledAttributes(attrs, R.styleable.OutlineClipImageView, defStyleAttr, 0)
        this.clipToBackground =
            a.getBoolean(R.styleable.OutlineClipImageView_olciv_clipToBackground, false)
        isOval = a.getBoolean(R.styleable.OutlineClipImageView_olciv_oval, false)
        cornerRadius =
            a.getDimension(R.styleable.OutlineClipImageView_olciv_cornerRadius, DEFAULT_RADIUS)
        cornerRadius = max(cornerRadius, MIN_RADIUS)

        a.recycle()
    }

    companion object {
        private const val MIN_RADIUS = 0f
        const val DEFAULT_RADIUS = MIN_RADIUS

        private val OUTLINE_PROVIDER = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                if (view !is OutlineClipImageView) {
                    return
                }
                outline.alpha = view.alpha
                when {
                    view.isOval -> {
                        outline.setOval(
                            view.paddingLeft,
                            view.paddingTop,
                            view.width - view.paddingRight,
                            view.height - view.paddingBottom
                        )
                    }
                    view.cornerRadius != DEFAULT_RADIUS -> {
                        outline.setRoundRect(
                            view.paddingLeft,
                            view.paddingTop,
                            view.width - view.paddingRight,
                            view.height - view.paddingBottom,
                            view.cornerRadius
                        )
                    }
                }
            }
        }
    }
}
