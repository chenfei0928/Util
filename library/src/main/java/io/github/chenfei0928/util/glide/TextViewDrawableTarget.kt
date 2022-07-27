package io.github.chenfei0928.util.glide

import android.graphics.drawable.Drawable
import android.os.Build
import android.view.Gravity
import android.widget.TextView
import androidx.annotation.GravityInt
import androidx.annotation.RequiresApi
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import io.github.chenfei0928.util.contains

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-07-23 10:16
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
open class TextViewDrawableTarget
@JvmOverloads constructor(
    view: TextView, @GravityInt private val direction: Int = Gravity.TOP
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
        if (Gravity.RELATIVE_LAYOUT_DIRECTION in direction) {
            val compoundDrawablesRelative = view.compoundDrawablesRelative
            if (Gravity.START in direction) {
                compoundDrawablesRelative[0] = drawable
            }
            if (Gravity.TOP in direction) {
                compoundDrawablesRelative[1] = drawable
            }
            if (Gravity.END in direction) {
                compoundDrawablesRelative[2] = drawable
            }
            if (Gravity.BOTTOM in direction) {
                compoundDrawablesRelative[3] = drawable
            }
            view.setCompoundDrawablesRelative(
                compoundDrawablesRelative[0],
                compoundDrawablesRelative[1],
                compoundDrawablesRelative[2],
                compoundDrawablesRelative[3]
            )
        } else {
            val compoundDrawables = view.compoundDrawables
            if (Gravity.LEFT in direction) {
                compoundDrawables[0] = drawable
            }
            if (Gravity.TOP in direction) {
                compoundDrawables[1] = drawable
            }
            if (Gravity.RIGHT in direction) {
                compoundDrawables[2] = drawable
            }
            if (Gravity.BOTTOM in direction) {
                compoundDrawables[3] = drawable
            }
            view.setCompoundDrawables(
                compoundDrawables[0],
                compoundDrawables[1],
                compoundDrawables[2],
                compoundDrawables[3]
            )
        }
    }
}
