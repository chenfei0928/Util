package io.github.chenfei0928.content.res

import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

internal fun View.getDimensionPixelSize(@DimenRes dimenRes: Int): Int =
    if (dimenRes == 0) 0 else resources.getDimensionPixelSize(dimenRes)

internal fun View.getDrawable(@DrawableRes drawableRes: Int): Drawable? =
    if (drawableRes == 0) null else ContextCompat.getDrawable(context, drawableRes)
