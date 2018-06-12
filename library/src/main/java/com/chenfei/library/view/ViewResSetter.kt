package com.chenfei.library.view

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.graphics.drawable.TintAwareDrawable
import android.support.v4.view.TintableBackgroundView
import android.support.v4.widget.TintableImageSourceView
import android.view.View
import android.widget.ImageView

fun View.setBackgroundTintRes(@ColorRes colorRes: Int) {
    val colorStateList = ContextCompat.getColorStateList(this.context, colorRes)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        this.backgroundTintList = colorStateList
    } else if (this is TintableBackgroundView) {
        val tintView = this as TintableBackgroundView
        tintView.supportBackgroundTintList = colorStateList
    } else {
        var background: Drawable = this.background ?: return
        if (background !is TintAwareDrawable) {
            background = DrawableCompat.wrap(background)
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                this.background = background
            } else {
                this.setBackgroundDrawable(background)
            }
        }
        DrawableCompat.setTintList(background, colorStateList)
    }
}

@SuppressLint("RestrictedApi")
fun ImageView.setSrcTintRes(@ColorRes colorRes: Int) {
    val colorStateList = ContextCompat.getColorStateList(this.context, colorRes)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        this.imageTintList = colorStateList
    } else if (this is TintableImageSourceView) {
        val tintView = this as TintableImageSourceView
        tintView.supportImageTintList = colorStateList
    } else {
        var drawable: Drawable = this.drawable ?: return
        if (drawable !is TintAwareDrawable) {
            drawable = DrawableCompat.wrap(drawable)
            this.setImageDrawable(drawable)
        }
        DrawableCompat.setTintList(drawable, colorStateList)
    }
}
