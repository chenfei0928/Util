package io.github.chenfei0928.graphics

import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

/**
 * 画笔的Drawable
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-04-14 14:27
 */
abstract class PaintDrawable : Drawable() {
    val paint = Paint()

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun getAlpha(): Int {
        return paint.alpha
    }

    override fun getColorFilter(): ColorFilter? {
        return paint.colorFilter
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int =
            PixelFormat.TRANSLUCENT
}
