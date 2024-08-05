package io.github.chenfei0928.graphics

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import io.github.chenfei0928.view.ViewUtil

/**
 * 获取帧动画的总时长
 */
val AnimationDrawable.duration: Int
    get() = (0 until numberOfFrames).sumOf {
        getDuration(it)
    }

fun Drawable.asRipple(context: Context): Drawable {
    return RippleDrawable(
        ColorStateList.valueOf(ViewUtil.getColorControlHighlight(context)),
        this,
        null
    )
}

/**
 * 处理图层列表中每一图层
 */
inline fun LayerDrawable.forEachLayer(block: (index: Int, drawable: Drawable) -> Unit) {
    for (it in 0 until numberOfLayers) {
        block(it, getDrawable(it))
    }
}
