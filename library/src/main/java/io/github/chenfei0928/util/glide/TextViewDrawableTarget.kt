package io.github.chenfei0928.util.glide

import android.graphics.drawable.Drawable
import android.widget.TextView
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import io.github.chenfei0928.widget.setDrawableBottom
import io.github.chenfei0928.widget.setDrawableEnd
import io.github.chenfei0928.widget.setDrawableStart
import io.github.chenfei0928.widget.setDrawableTop

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-07-23 10:16
 */
open class TextViewDrawableTarget
@JvmOverloads constructor(
    view: TextView, private val place: Int = TOP
) : CustomViewTarget<TextView, Drawable>(view) {
    override fun onLoadFailed(errorDrawable: Drawable?) {
        setDrawable(errorDrawable)
    }

    override fun onResourceCleared(placeholder: Drawable?) {
        setDrawable(placeholder)
    }

    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
        setDrawable(resource)
    }

    protected open fun setDrawable(drawable: Drawable?) {
        when (place) {
            LEFT -> {
                view.setDrawableStart(drawable)
            }
            TOP -> {
                view.setDrawableTop(drawable)
            }
            RIGHT -> {
                view.setDrawableEnd(drawable)
            }
            BOTTOM -> {
                view.setDrawableBottom(drawable)
            }
        }
    }

    companion object {
        const val LEFT = 0
        const val TOP = 1
        const val RIGHT = 2
        const val BOTTOM = 3
    }
}
