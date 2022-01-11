package io.github.chenfei0928.widget

import android.widget.TextView
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import io.github.chenfei0928.content.res.getDimensionPixelSize
import io.github.chenfei0928.content.res.getDrawable

fun TextView.setDrawableStartRes(@DrawableRes drawableRes: Int) {
    setDrawableStart(getDrawable(drawableRes))
}

fun TextView.setDrawableTopRes(@DrawableRes drawableRes: Int) {
    setDrawableTop(getDrawable(drawableRes))
}

fun TextView.setDrawableEndRes(@DrawableRes drawableRes: Int) {
    setDrawableEnd(getDrawable(drawableRes))
}

fun TextView.setDrawableBottomRes(@DrawableRes drawableRes: Int) {
    setDrawableBottom(getDrawable(drawableRes))
}

fun TextView.setDrawablePaddingRes(@DimenRes dimenRes: Int) {
    this.compoundDrawablePadding = getDimensionPixelSize(dimenRes)
}
