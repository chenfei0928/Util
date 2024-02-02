/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-08-30 18:34
 */
package io.github.chenfei0928.view

import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.GravityInt
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.core.view.updateMarginsRelative
import androidx.core.view.updatePadding
import androidx.core.view.updatePaddingRelative
import io.github.chenfei0928.util.contains

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
private val supportRelativeDirection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1

fun View.applySystemInsetMargin(@GravityInt direction: Int) {
    val originalMarginLeft = marginLeft
    val originalMarginTop = marginTop
    val originalMarginRight = marginRight
    val originalMarginBottom = marginBottom
    val originalMarginStart = marginStart
    val originalMarginEnd = marginEnd
    setOnApplyWindowInsetsCompatListener { left, top, right, bottom ->
        updateLayoutParams<ViewGroup.MarginLayoutParams> {
            if (supportRelativeDirection && Gravity.RELATIVE_LAYOUT_DIRECTION in direction) {
                val absoluteGravity = Gravity.getAbsoluteGravity(direction, layoutDirection)
                val start = if (Gravity.LEFT in absoluteGravity) left else right
                val end = if (Gravity.RIGHT in absoluteGravity) right else left
                updateMarginsRelative(
                    start = originalMarginStart +
                            (start.takeIf { Gravity.START in direction } ?: 0),
                    top = originalMarginTop +
                            (top.takeIf { Gravity.TOP in direction } ?: 0),
                    end = originalMarginEnd +
                            (end.takeIf { Gravity.END in direction } ?: 0),
                    bottom = originalMarginBottom +
                            (bottom.takeIf { Gravity.BOTTOM in direction } ?: 0)
                )
            } else {
                updateMargins(
                    left = originalMarginLeft +
                            (left.takeIf { Gravity.LEFT in direction } ?: 0),
                    top = originalMarginTop +
                            (top.takeIf { Gravity.TOP in direction } ?: 0),
                    right = originalMarginRight +
                            (right.takeIf { Gravity.RIGHT in direction } ?: 0),
                    bottom = originalMarginBottom +
                            (bottom.takeIf { Gravity.BOTTOM in direction } ?: 0)
                )
            }
        }
    }
}

fun View.applySystemInsetPadding(@GravityInt direction: Int) {
    val originalPaddingLeft = paddingLeft
    val originalPaddingTop = paddingTop
    val originalPaddingRight = paddingRight
    val originalPaddingBottom = paddingBottom
    val originalPaddingStart = if (supportRelativeDirection)
        paddingStart else 0
    val originalPaddingEnd = if (supportRelativeDirection)
        paddingEnd else 0
    val lpHeightMode = View.MeasureSpec.getMode(layoutParams.height)
    val lpHeight = View.MeasureSpec.getSize(layoutParams.height)
    val lpWidthMode = View.MeasureSpec.getMode(layoutParams.width)
    val lpWidth = View.MeasureSpec.getSize(layoutParams.width)
    setOnApplyWindowInsetsCompatListener { left, top, right, bottom ->
        if (supportRelativeDirection && Gravity.RELATIVE_LAYOUT_DIRECTION in direction) {
            val absoluteGravity = Gravity.getAbsoluteGravity(direction, layoutDirection)
            val start = if (Gravity.LEFT in absoluteGravity) left else right
            val end = if (Gravity.RIGHT in absoluteGravity) right else left
            updatePaddingRelative(
                start = originalPaddingStart +
                        (start.takeIf { Gravity.START in direction } ?: 0),
                top = originalPaddingTop +
                        (top.takeIf { Gravity.TOP in direction } ?: 0),
                end = originalPaddingEnd +
                        (end.takeIf { Gravity.END in direction } ?: 0),
                bottom = originalPaddingBottom +
                        (bottom.takeIf { Gravity.BOTTOM in direction } ?: 0)
            )
            if (lpWidthMode != ViewGroup.LayoutParams.WRAP_CONTENT) {
                updateLayoutParams {
                    width = lpWidth +
                            (start.takeIf { Gravity.START in direction } ?: 0) +
                            (end.takeIf { Gravity.END in direction } ?: 0)
                }
            }
        } else {
            updatePadding(
                left = originalPaddingLeft +
                        (left.takeIf { Gravity.LEFT in direction } ?: 0),
                top = originalPaddingTop +
                        (top.takeIf { Gravity.TOP in direction } ?: 0),
                right = originalPaddingRight +
                        (right.takeIf { Gravity.RIGHT in direction } ?: 0),
                bottom = originalPaddingBottom +
                        (bottom.takeIf { Gravity.BOTTOM in direction } ?: 0)
            )
            if (lpWidthMode != ViewGroup.LayoutParams.WRAP_CONTENT) {
                updateLayoutParams {
                    width = lpWidth +
                            (left.takeIf { Gravity.TOP in direction } ?: 0) +
                            (right.takeIf { Gravity.BOTTOM in direction } ?: 0)
                }
            }
        }
        if (lpHeightMode != ViewGroup.LayoutParams.WRAP_CONTENT) {
            updateLayoutParams {
                height = lpHeight +
                        (top.takeIf { Gravity.TOP in direction } ?: 0) +
                        (bottom.takeIf { Gravity.BOTTOM in direction } ?: 0)
            }
        }
    }
}

/**
 * 添加窗口insets监听，用于监听状态栏、导航栏高度或异形屏的缺口高度
 */
fun View.setOnApplyWindowInsetsCompatListener(insetUpdate: View.(left: Int, top: Int, right: Int, bottom: Int) -> Unit) {
    doOnPreDraw {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            WindowInsetsCompat.toWindowInsetsCompat(rootWindowInsets).systemWindowInsets
        } else {
            Insets.of(
                0,
                SystemUiUtil.getStatusBarHeight(context),
                0,
                SystemUiUtil.checkGetNavigationBarHeight(context)
            )
        }.run {
            insetUpdate(left, top, right, bottom)
        }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
        setOnApplyWindowInsetsListener { _, insets ->
            WindowInsetsCompat.toWindowInsetsCompat(insets).systemWindowInsets.run {
                insetUpdate(left, top, right, bottom)
            }
            insets
        }
    }
}
