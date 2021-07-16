package com.chenfei.viewModel

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Build
import android.view.MenuItem
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat

fun MenuItem.setIconTintCompat(@ColorInt color: Int) {
    setIconTintCompat(ColorStateList.valueOf(color))
}

fun MenuItem.setIconTintCompat(color: ColorStateList) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        iconTintList = color
    } else {
        val icon = DrawableCompat.wrap(icon.mutate())
        DrawableCompat.setTintList(icon, color)
        setIcon(icon)
    }
}

fun MenuItem.setIconTintModeCompat(tintMode: PorterDuff.Mode) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        iconTintMode = tintMode
    } else {
        val icon = DrawableCompat.wrap(icon.mutate())
        DrawableCompat.setTintMode(icon, tintMode)
        setIcon(icon)
    }
}
