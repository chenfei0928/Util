/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-18 15:00
 */
package io.github.chenfei0928.graphics

import androidx.annotation.ColorInt
import androidx.core.graphics.toColorInt

@ColorInt
fun String.toColorIntOrNull(): Int? {
    return try {
        this.toColorInt()
    } catch (_: IllegalArgumentException) {
        null
    }
}
