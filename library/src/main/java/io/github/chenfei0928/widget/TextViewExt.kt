package io.github.chenfei0928.widget

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import io.github.chenfei0928.graphics.setTintCompat

private fun setIntrinsicBounds(drawable: Drawable?) {
    drawable?.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
}

fun TextView.setDrawableBottom(drawable: Drawable?) {
    setIntrinsicBounds(drawable)
    val drawables = this.compoundDrawables
    this.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawable)
}

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
fun TextView.updateDrawableRelative(
    start: Drawable? = compoundDrawablesRelative[0],
    top: Drawable? = compoundDrawablesRelative[1],
    end: Drawable? = compoundDrawablesRelative[2],
    bottom: Drawable? = compoundDrawablesRelative[3]
) {
    setCompoundDrawablesRelative(start, top, end, bottom)
}

@Deprecated(
    message = "use setDrawableStart insert",
    replaceWith = ReplaceWith(
        expression = "setDrawableStart(drawable)",
        imports = ["io.github.chenfei0928.widget.setDrawableStart"]
    )
)
fun TextView.setDrawableLeft(drawable: Drawable?) {
    setIntrinsicBounds(drawable)
    val drawables = this.compoundDrawables
    this.setCompoundDrawables(drawable, drawables[1], drawables[2], drawables[3])
}

@Deprecated(
    message = "use setDrawableEnd insert",
    replaceWith = ReplaceWith(
        expression = "setDrawableEnd(drawable)",
        imports = ["io.github.chenfei0928.widget.setDrawableEnd"]
    )
)
fun TextView.setDrawableRight(drawable: Drawable?) {
    setIntrinsicBounds(drawable)
    val drawables = this.compoundDrawables
    this.setCompoundDrawables(
        drawables[0], drawables[1], drawable,
        drawables[3]
    )
}

fun TextView.setDrawableTop(drawable: Drawable?) {
    setIntrinsicBounds(drawable)
    val drawables = this.compoundDrawables
    this.setCompoundDrawables(
        drawables[0], drawable, drawables[2],
        drawables[3]
    )
}

fun TextView.setDrawableStart(drawable: Drawable?) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
        @Suppress("DEPRECATION")
        setDrawableLeft(drawable)
    } else {
        setIntrinsicBounds(drawable)
        val drawables = this.compoundDrawablesRelative
        this.setCompoundDrawablesRelative(drawable, drawables[1], drawables[2], drawables[3])
    }
}

fun TextView.setDrawableEnd(drawable: Drawable?) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
        @Suppress("DEPRECATION")
        setDrawableRight(drawable)
    } else {
        setIntrinsicBounds(drawable)
        val drawables = this.compoundDrawablesRelative
        this.setCompoundDrawablesRelative(drawables[0], drawables[1], drawable, drawables[3])
    }
}

@JvmOverloads
fun TextView.setDrawableTint(@ColorInt color: Int, fixLayoutXml: Boolean = true) {
    // Android 6.0 开始 TextView 原生支持解析 drawableTint
    // 5.0以下不支持ColorStateList作为文字颜色，会崩溃
    // 7.0以下不支持ColorStateList作为TextView控件的drawableTint，没有效果
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
        // 如果不是从xml文件加载的，设置tint
        if (!fixLayoutXml) {
            compoundDrawableTintList = ColorStateList.valueOf(color)
        }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        val drawables = this.compoundDrawablesRelative
        for (i in drawables.indices) {
            drawables[i] = drawables[i]?.setTintCompat(color)
        }
        setCompoundDrawablesRelative(drawables[0], drawables[1], drawables[2], drawables[3])
    } else {
        val drawables = this.compoundDrawables
        for (i in drawables.indices) {
            drawables[i] = drawables[i]?.setTintCompat(color)
        }
        setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3])
    }
}
