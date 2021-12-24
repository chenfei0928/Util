package io.github.chenfei0928.viewModel

import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Px
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.MarginLayoutParamsCompat
import io.github.chenfei0928.util.Log

private const val TAG = "KW_ViewLayoutSetter"

@get:Px
@setparam:Px
var View.marginStart: Int
    get() = when (val lp = layoutParams) {
        is ViewGroup.MarginLayoutParams -> MarginLayoutParamsCompat.getMarginStart(lp)
        else -> {
            Log.w(TAG, "marginStart: unknown layoutParams type: ${lp.javaClass}")
            0
        }
    }
    set(value) {
        when (val lp = layoutParams) {
            is ViewGroup.MarginLayoutParams -> {
                lp.setMargins(value, lp.topMargin, lp.rightMargin, lp.bottomMargin)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && lp.isMarginRelative) {
                    lp.marginStart = value
                }
            }
            else -> {
                Log.w(TAG, "marginStart: unknown layoutParams type: ${lp.javaClass}")
            }
        }
    }

@get:Px
@setparam:Px
var View.marginTop: Int
    get() = when (val lp = layoutParams) {
        is ViewGroup.MarginLayoutParams -> lp.topMargin
        is ConstraintLayout.LayoutParams -> lp.topMargin
        else -> {
            Log.w(TAG, "marginTop: unknown layoutParams type: ${lp.javaClass}")
            0
        }
    }
    set(value) {
        when (val lp = layoutParams) {
            is ViewGroup.MarginLayoutParams -> {
                lp.setMargins(lp.leftMargin, value, lp.rightMargin, lp.bottomMargin)
            }
            is ConstraintLayout.LayoutParams -> {
                lp.topMargin = value
            }
            else -> {
                Log.w(TAG, "marginTop: unknown layoutParams type: ${lp.javaClass}")
            }
        }
    }

@get:Px
@setparam:Px
var View.marginEnd: Int
    get() = when (val lp = layoutParams) {
        is ViewGroup.MarginLayoutParams -> MarginLayoutParamsCompat.getMarginEnd(lp)
        else -> {
            Log.w(TAG, "marginEnd: unknown layoutParams type: ${lp.javaClass}")
            0
        }
    }
    set(value) {
        when (val lp = layoutParams) {
            is ViewGroup.MarginLayoutParams -> {
                lp.setMargins(lp.leftMargin, lp.topMargin, value, lp.bottomMargin)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && lp.isMarginRelative) {
                    lp.marginEnd = value
                }
            }
            else -> {
                Log.w(TAG, "marginEnd: unknown layoutParams type: ${lp.javaClass}")
            }
        }
    }

@get:Px
@setparam:Px
var View.marginBottom: Int
    get() = when (val lp = layoutParams) {
        is ViewGroup.MarginLayoutParams -> lp.bottomMargin
        is ConstraintLayout.LayoutParams -> lp.bottomMargin
        else -> {
            Log.w(TAG, "marginBottom: unknown layoutParams type: ${lp.javaClass}")
            0
        }
    }
    set(value) {
        when (val lp = layoutParams) {
            is ViewGroup.MarginLayoutParams -> {
                lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, value)
            }
            is ConstraintLayout.LayoutParams -> {
                lp.bottomMargin = value
            }
            else -> {
                Log.w(TAG, "marginBottom: unknown layoutParams type: ${lp.javaClass}")
            }
        }
    }

inline fun <reified Lp : ViewGroup.LayoutParams> View.updateLayoutParamsIfNeed(block: Lp.() -> Boolean) {
    val requestLayout = block(layoutParams as Lp)
    if (requestLayout) {
        layoutParams = layoutParams
    }
}
