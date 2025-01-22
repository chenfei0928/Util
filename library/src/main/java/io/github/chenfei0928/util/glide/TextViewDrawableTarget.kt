package io.github.chenfei0928.util.glide

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.widget.TextView
import androidx.annotation.GravityInt
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import io.github.chenfei0928.lang.contains

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-07-23 10:16
 */
@Suppress("MagicNumber")
open class TextViewDrawableTarget
@JvmOverloads constructor(
    view: TextView,
    @GravityInt
    private val direction: Int = Gravity.TOP,
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
        drawable?.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        if (Gravity.RELATIVE_LAYOUT_DIRECTION in direction
            || view.compoundDrawablesRelative.let { it[0] != null || it[3] != null }
        ) {
            val compoundDrawablesRelative = view.compoundDrawablesRelative
            compoundDrawablesRelative.setDrawableToGravity(
                drawable, Gravity.START, Gravity.TOP, Gravity.END, Gravity.BOTTOM
            )
            view.setCompoundDrawablesRelative(
                /* start = */ compoundDrawablesRelative[0],
                /* top = */ compoundDrawablesRelative[1],
                /* end = */ compoundDrawablesRelative[2],
                /* bottom = */ compoundDrawablesRelative[3]
            )
        } else {
            val compoundDrawables = view.compoundDrawables
            @SuppressLint("RtlHardcoded")
            compoundDrawables.setDrawableToGravity(
                drawable, Gravity.LEFT, Gravity.TOP, Gravity.RIGHT, Gravity.BOTTOM
            )
            view.setCompoundDrawables(
                /* left = */ compoundDrawables[0],
                /* top = */ compoundDrawables[1],
                /* right = */ compoundDrawables[2],
                /* bottom = */ compoundDrawables[3]
            )
        }
    }

    private fun Array<Drawable?>.setDrawableToGravity(
        drawable: Drawable?,
        gravity0: Int, gravity1: Int, gravity2: Int, gravity3: Int,
    ) {
        if (gravity0 in direction) {
            this[0] = drawable
        }
        if (gravity1 in direction) {
            this[1] = drawable
        }
        if (gravity2 in direction) {
            this[2] = drawable
        }
        if (gravity3 in direction) {
            this[3] = drawable
        }
    }
}
