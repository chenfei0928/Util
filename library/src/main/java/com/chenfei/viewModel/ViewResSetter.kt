package com.chenfei.viewModel

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.TintAwareDrawable
import androidx.core.view.TintableBackgroundView
import androidx.core.widget.TintableImageSourceView

fun View.setBackgroundTintRes(@ColorRes colorRes: Int) {
    val colorStateList = ContextCompat.getColorStateList(this.context, colorRes)
    setBackgroundTintColor(colorStateList)
}

fun View.setBackgroundTintColor(@ColorInt colorInt: Int) {
    val colorStateList = ColorStateList.valueOf(colorInt)
    setBackgroundTintColor(colorStateList)
}

fun View.setBackgroundTintColor(colorStateList: ColorStateList?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        this.backgroundTintList = colorStateList
    } else if (this is TintableBackgroundView) {
        val tintView = this as TintableBackgroundView
        tintView.supportBackgroundTintList = colorStateList
    } else {
        var background: Drawable = this.background ?: return
        if (background !is TintAwareDrawable) {
            background = DrawableCompat.wrap(background)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                this.background = background
            } else {
                this.setBackgroundDrawable(background)
            }
        }
        DrawableCompat.setTintList(background, colorStateList)
    }
}

fun ImageView.setSrcTintRes(@ColorRes colorRes: Int) {
    val colorStateList = ContextCompat.getColorStateList(this.context, colorRes)
    setSrcTint(colorStateList)
}

fun ImageView.setSrcTint(colorStateList: ColorStateList?) {
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
