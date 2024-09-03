package io.github.chenfei0928.graphics.drawable

import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.widget.Checkable
import android.widget.CheckedTextView
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.withTranslation
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.widget.CheckedTextViewCompat
import androidx.core.widget.CompoundButtonCompat
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.TextViewCompat

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-25 17:08
 */
class ViewDrawable(
    val view: View
) : Drawable() {

    override fun getMinimumHeight(): Int {
        return view.minimumHeight
    }

    override fun getMinimumWidth(): Int {
        return view.minimumWidth
    }

    override fun getIntrinsicWidth(): Int {
        return view.measuredWidth
    }

    override fun getIntrinsicHeight(): Int {
        return view.measuredHeight
    }

    @Suppress("kotlin:S6518")
    override fun getPadding(padding: Rect): Boolean {
        padding.set(view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom)
        return true
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        view.layout(bounds.left, bounds.top, bounds.right, bounds.bottom)
    }

    override fun getOutline(outline: Outline) {
        view.outlineProvider?.getOutline(view, outline)
            ?: super.getOutline(outline)
    }

    override fun setVisible(visible: Boolean, restart: Boolean): Boolean {
        view.isVisible = visible
        return super.setVisible(visible, restart)
    }

    override fun invalidateSelf() {
        super.invalidateSelf()
        view.invalidate()
    }

    override fun onStateChange(state: IntArray): Boolean {
        jumpToCurrentState()
        return true
    }

    override fun jumpToCurrentState() {
        val state = state
//        view. = android.R.attr.state_above_anchor in state
//        view.isHardwareAccelerated = android.R.attr.state_accelerated in state
        view.isActivated = android.R.attr.state_activated in state
//        view. = android.R.attr.state_active in state
//        view is Checkable = android.R.attr.state_checkable in state
        if (view is Checkable) {
            view.isChecked = android.R.attr.state_checked in state
        }
//        view. = android.R.attr.state_drag_can_accept in state
//        view. = android.R.attr.state_drag_hovered in state
//        view. = android.R.attr.state_empty in state
        view.isEnabled = android.R.attr.state_enabled in state
//        view. = android.R.attr.state_expanded in state
//        view. = android.R.attr.state_first in state
//        view. = android.R.attr.state_focused in state
//        view. = android.R.attr.state_hovered in state
//        view. = android.R.attr.state_last in state
        view.isLongClickable = android.R.attr.state_long_pressable in state
//        view. = android.R.attr.state_middle in state
//        view. = android.R.attr.state_multiline in state
        view.isPressed = android.R.attr.state_pressed in state
        view.isSelected = android.R.attr.state_selected in state
//        view. = android.R.attr.state_single in state
//        view. = android.R.attr.state_window_focused in state
    }

    override fun isStateful(): Boolean {
        return true
    }

    override fun draw(canvas: Canvas) {
        canvas.withTranslation(view.left.toFloat(), view.top.toFloat()) {
            view.draw(canvas)
        }
    }

    override fun setAlpha(alpha: Int) {
        view.alpha = alpha / 255f
    }

    override fun getAlpha(): Int {
        return (view.alpha * 255).toInt()
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        view.setLayerPaint(colorFilter?.let {
            Paint().apply {
                setColorFilter(colorFilter)
            }
        })
    }

    override fun setTintList(tint: ColorStateList?) {
        ViewCompat.setBackgroundTintList(view, tint)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.foregroundTintList = tint
        }
        when (view) {
            is CheckedTextView -> {
                CheckedTextViewCompat.setCheckMarkTintList(view, tint)
                TextViewCompat.setCompoundDrawableTintList(view, tint)
            }
            is CompoundButton -> {
                CompoundButtonCompat.setButtonTintList(view, tint)
                TextViewCompat.setCompoundDrawableTintList(view, tint)
            }
            is TextView -> TextViewCompat.setCompoundDrawableTintList(view, tint)
            is ImageView -> ImageViewCompat.setImageTintList(view, tint)
        }
    }

    override fun setTintMode(tintMode: PorterDuff.Mode?) {
        ViewCompat.setBackgroundTintMode(view, tintMode)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.foregroundTintMode = tintMode
        }
        when (view) {
            is CheckedTextView -> {
                CheckedTextViewCompat.setCheckMarkTintMode(view, tintMode)
                TextViewCompat.setCompoundDrawableTintMode(view, tintMode)
            }
            is CompoundButton -> {
                CompoundButtonCompat.setButtonTintMode(view, tintMode)
                TextViewCompat.setCompoundDrawableTintMode(view, tintMode)
            }
            is TextView -> TextViewCompat.setCompoundDrawableTintMode(view, tintMode)
            is ImageView -> ImageViewCompat.setImageTintMode(view, tintMode)
        }
    }
}
