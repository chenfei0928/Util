package io.github.chenfei0928.util.glide

import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import io.github.chenfei0928.widget.updateDrawableRelative

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-07-23 10:16
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
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
            START -> {
                view.updateDrawableRelative(start = drawable)
            }
            TOP -> {
                view.updateDrawableRelative(top = drawable)
            }
            END -> {
                view.updateDrawableRelative(end = drawable)
            }
            BOTTOM -> {
                view.updateDrawableRelative(bottom = drawable)
            }
        }
    }

    companion object {
        const val START = 0
        const val TOP = 1
        const val END = 2
        const val BOTTOM = 3
    }
}
