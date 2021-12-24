/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-18 15:00
 */
package io.github.chenfei0928.util.kotlin

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red

/**
 * [com.google.android.material.internal.CollapsingTextHelper.blendColors]
 */
@ColorInt
fun blendColors(
    @ColorInt startColor: Int,
    @ColorInt endColor: Int,
    @FloatRange(from = 0.0, to = 1.0) ratio: Float
): Int {
    if (ratio == 0f) {
        return startColor
    } else if (ratio == 1f) {
        return endColor
    }
    val inverseRatio = 1f - ratio
    val a = startColor.alpha * inverseRatio + endColor.alpha * ratio
    val r = startColor.red * inverseRatio + endColor.red * ratio
    val g = startColor.green * inverseRatio + endColor.green * ratio
    val b = startColor.blue * inverseRatio + endColor.blue * ratio
    return Color.argb(a.toInt(), r.toInt(), g.toInt(), b.toInt())
}

fun String.toColorIntOrNull(): Int? {
    return try {
        Color.parseColor(this)
    } catch (e: Exception) {
        null
    }
}
