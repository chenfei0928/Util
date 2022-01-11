package io.github.chenfei0928.view

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.TintAwareDrawable
import androidx.core.view.TintableBackgroundView

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
