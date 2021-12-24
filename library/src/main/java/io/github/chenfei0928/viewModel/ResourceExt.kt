package io.github.chenfei0928.viewModel

import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.TintAwareDrawable

/**
 * 为Drawable添加着色，如果其原生支持着色，进行着色，并返回this
 * 如果不支持着色，通过[DrawableCompat.wrap]包裹一层，添加着色，返回添加着色支持后的Drawable对象
 *
 * @receiver    要进行着色处理的Drawable
 * @param tint  着色颜色
 * @return      添加着色支持的Drawable，或如果其已经支持着色返回this
 */
fun Drawable.setTintCompat(@ColorInt tint: Int): Drawable? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        this.setTint(tint)
        this
    } else if (this is TintAwareDrawable) {
        DrawableCompat.setTint(this, tint)
        this
    } else {
        val wrap = DrawableCompat.wrap(this.mutate())
        DrawableCompat.setTint(wrap, tint)
        wrap
    }
}

internal fun View.getDimensionPixelSize(@DimenRes dimenRes: Int): Int =
        if (dimenRes == 0) 0 else resources.getDimensionPixelSize(dimenRes)

internal fun View.getDrawable(@DrawableRes drawableRes: Int): Drawable? =
        if (drawableRes == 0) null else ContextCompat.getDrawable(context, drawableRes)
