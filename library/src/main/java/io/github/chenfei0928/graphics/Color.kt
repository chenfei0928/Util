/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-18 15:00
 */
package io.github.chenfei0928.graphics

import android.graphics.Color
import androidx.annotation.ColorInt

@ColorInt
fun String.toColorIntOrNull(): Int? {
    return try {
        Color.parseColor(this)
    } catch (ignore: IllegalArgumentException) {
        null
    }
}
