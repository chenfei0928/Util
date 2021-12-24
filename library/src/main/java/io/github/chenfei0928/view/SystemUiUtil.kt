/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-08-30 18:34
 */
package io.github.chenfei0928.view

import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import io.github.chenfei0928.viewModel.marginBottom
import io.github.chenfei0928.viewModel.marginTop

fun View.applySystemInsetTopMargin() {
    val originalMarginTop = marginTop
    setOnApplyWindowInsetsCompatListener { _, top, _, _ ->
        marginTop = originalMarginTop + top
    }
}

fun View.applySystemInsetTopPadding() {
    val originalPaddingTop = paddingTop
    val lpMode = View.MeasureSpec.getMode(layoutParams.height)
    val lpHeight = View.MeasureSpec.getSize(layoutParams.height)
    setOnApplyWindowInsetsCompatListener { _, top, _, _ ->
        updatePadding(top = originalPaddingTop + top)
        if (lpMode != ViewGroup.LayoutParams.WRAP_CONTENT) {
            updateLayoutParams {
                height = lpHeight + top
            }
        }
    }
}

fun View.applySystemInsetBottomMargin() {
    val originalMarginBottom = marginBottom
    setOnApplyWindowInsetsCompatListener { _, _, _, bottom ->
        marginBottom = originalMarginBottom + bottom
    }
}

fun View.applySystemInsetBottomPadding() {
    val originalPaddingBottom = paddingBottom
    val lpMode = View.MeasureSpec.getMode(layoutParams.height)
    val lpHeight = View.MeasureSpec.getSize(layoutParams.height)
    setOnApplyWindowInsetsCompatListener { _, _, _, bottom ->
        updatePadding(bottom = originalPaddingBottom + bottom)
        if (lpMode != ViewGroup.LayoutParams.WRAP_CONTENT) {
            updateLayoutParams {
                height = lpHeight + bottom
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
