package com.chenfei.util.kotlin

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.*
import android.os.Build
import com.chenfei.view.ViewUtil

/**
 * 获取帧动画的总时长
 */
val AnimationDrawable.duration: Int
    get() {
        var duration = 0
        for (i in 0 until numberOfFrames) {
            duration += getDuration(i)
        }
        return duration
    }

fun Drawable.asRipple(context: Context): Drawable {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        RippleDrawable(ColorStateList.valueOf(ViewUtil.getColorControlHighlight(context)), this, null)
    } else {
        asRippleLessL(ColorDrawable(0x1a000000))
    }
}

/**
 * 作为低于5.0系统的点击效果处理，添加图层
 */
private fun Drawable.asRippleLessL(pressedDrawableLayer: Drawable): Drawable {
    val stateListDrawable = StateListDrawable()
    // 设置pressed状态
    stateListDrawable.addState(intArrayOf(android.R.attr.state_pressed),
            LayerDrawable(arrayOf(this, pressedDrawableLayer)))
    // 设置normal状态
    stateListDrawable.addState(intArrayOf(), this)
    return stateListDrawable
}

/**
 * 处理图层列表中每一图层
 */
inline fun LayerDrawable.forEachLayer(block: (index: Int, drawable: Drawable) -> Unit) {
    (0 until numberOfLayers).forEach {
        block(it, getDrawable(it))
    }
}
