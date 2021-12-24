package io.github.chenfei0928.graphics.drawable

import android.graphics.Canvas
import android.graphics.drawable.Drawable

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-11-23 15:45
 */
class RotateDrawable(drawable: Drawable) : DrawableWrapper(drawable) {
    var pivotXRel = true
    var pivotX = 0.5f
    var pivotYRel = true
    var pivotY = 0.5f
    var degrees = 0.0f

    override fun draw(canvas: Canvas) {
        val bounds = wrappedDrawable.bounds
        val w = bounds.right - bounds.left
        val h = bounds.bottom - bounds.top
        val px: Float = if (pivotXRel) w * pivotX else pivotX
        val py: Float = if (pivotYRel) h * pivotY else pivotY

        val saveCount = canvas.save()
        canvas.rotate(degrees, px + bounds.left, py + bounds.top)
        super.draw(canvas)
        canvas.restoreToCount(saveCount)
    }
}
