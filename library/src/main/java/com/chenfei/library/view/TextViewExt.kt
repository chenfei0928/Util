package com.chenfei.library.view

import android.os.Build
import android.support.annotation.ColorInt
import android.widget.TextView

fun TextView.setDrawableTint(@ColorInt color: Int) {
    // Android 6.0 开始 TextView 原生支持解析 drawableTint
    // 5.0以下不支持ColorStateList作为文字颜色，会崩溃
    // 7.0以下不支持ColorStateList作为TextView控件的drawableTint，没有效果
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
        return
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        val drawables = this.compoundDrawablesRelative
        for (i in 0 until drawables.size) {
            drawables[i] = drawables[i]?.setTintCompat(color)
        }
        setCompoundDrawablesRelative(drawables[0], drawables[1], drawables[2], drawables[3])
    } else {
        val drawables = this.compoundDrawables
        for (i in 0 until drawables.size) {
            drawables[i] = drawables[i]?.setTintCompat(color)
        }
        setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3])
    }
}
