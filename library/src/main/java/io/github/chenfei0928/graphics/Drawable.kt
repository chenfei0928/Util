package io.github.chenfei0928.graphics

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.*
import android.os.Build
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.TintAwareDrawable
import io.github.chenfei0928.view.ViewUtil

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
        RippleDrawable(
            ColorStateList.valueOf(ViewUtil.getColorControlHighlight(context)),
            this,
            null
        )
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
    stateListDrawable.addState(
        intArrayOf(android.R.attr.state_pressed),
        LayerDrawable(arrayOf(this, pressedDrawableLayer))
    )
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

/**
 * 为Drawable添加着色，如果其原生支持着色，进行着色，并返回this
 * 如果不支持着色，通过[DrawableCompat.wrap]包裹一层，添加着色，返回添加着色支持后的Drawable对象
 *
 * @receiver    要进行着色处理的Drawable
 * @param tint  着色颜色
 * @return      添加着色支持的Drawable，或如果其已经支持着色返回this
 */
fun Drawable.setTintCompat(@ColorInt tint: Int): Drawable {
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
