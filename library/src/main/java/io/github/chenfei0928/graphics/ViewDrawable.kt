package io.github.chenfei0928.graphics

import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.widget.Checkable
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-25 17:08
 */
class ViewDrawable(
    val view: View
) : Drawable() {

    override fun getAlpha(): Int {
        return (view.alpha * 255).toInt()
    }

    override fun setVisible(visible: Boolean, restart: Boolean): Boolean {
        view.isVisible = visible
        return super.setVisible(visible, restart)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun getOutline(outline: Outline) {
        view.outlineProvider?.getOutline(view, outline)
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        view.layout(bounds.left, bounds.top, bounds.right, bounds.bottom)
    }

    override fun getIntrinsicWidth(): Int {
        return view.measuredWidth
    }

    override fun getIntrinsicHeight(): Int {
        return view.measuredHeight
    }

    override fun jumpToCurrentState() {
        val state = state
//        view. = android.R.attr.state_above_anchor in state
//        view. = android.R.attr.state_accelerated in state
        view.isActivated = android.R.attr.state_activated in state
//        view. = android.R.attr.state_active in state
//        view. = android.R.attr.state_checkable in state
        if (view is Checkable) {
            view.isChecked = android.R.attr.state_checked in state
        }
//        view.isPressed = android.R.attr.state_drag_can_accept in state
//        view.isPressed = android.R.attr.state_drag_hovered in state
//        view.isPressed = android.R.attr.state_empty in state
        view.isEnabled = android.R.attr.state_enabled in state
//        view.isPressed = android.R.attr.state_expanded in state
//        view.isPressed = android.R.attr.state_first in state
//        view.isf = android.R.attr.state_focused in state
//        view.isPressed = android.R.attr.state_hovered in state
//        view.isPressed = android.R.attr.state_last in state
//        view.isPressed = android.R.attr.state_long_pressable in state
//        view.isPressed = android.R.attr.state_middle in state
//        view.isPressed = android.R.attr.state_multiline in state
        view.isPressed = android.R.attr.state_pressed in state
        view.isSelected = android.R.attr.state_selected in state
//        view.isPressed = android.R.attr.state_single in state
//        view.isPressed = android.R.attr.state_window_focused in state
    }

    override fun isStateful(): Boolean {
        return true
    }

    override fun draw(canvas: Canvas) {
        val save = canvas.save()
        canvas.translate(view.left.toFloat(), view.top.toFloat())
        view.draw(canvas)
        canvas.restoreToCount(save)
    }

    override fun setAlpha(alpha: Int) {
        view.alpha = alpha / 255f
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }
}
