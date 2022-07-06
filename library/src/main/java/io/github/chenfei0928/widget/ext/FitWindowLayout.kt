/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-08-30 14:49
 */
package io.github.chenfei0928.widget.ext

import android.graphics.Rect
import android.os.Build
import android.view.Gravity
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.view.updatePadding
import io.github.chenfei0928.view.applySystemInsetPadding

fun View.onMeasure(fitStatusBar: Boolean) {
    if (fitStatusBar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 修复状态栏边距
            updatePadding(
                top = rootWindowInsets.systemWindowInsetTop
            )
        } else {
            applySystemInsetPadding(direction = Gravity.TOP)
        }
    }
}

fun View.fitSystemWindows(insets: Rect, fitStatusBar: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        setPaddingRelative(insets.left, insets.top, insets.right, insets.bottom)
    } else {
        setPadding(insets.left, insets.top, insets.right, insets.bottom)
    }
    if (fitStatusBar) {
        // 修复状态栏边距
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            updatePadding(
                top = rootWindowInsets.systemWindowInsetTop
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.P)
private fun View.fitDisplayCutoutSafeInset() {
    // 此字段仅在window被标记为在刘海区域内显示内容时才会返回
    val cutout = rootWindowInsets.displayCutout
    if (cutout != null) {
        // window 在刘海区域内显示内容
        val insetTop = cutout.safeInsetTop
        if (insetTop != 0) {
            // 有刘海，设置为刘海高度
            updatePadding(
                top = insetTop
            )
        }
    } else {
        // 不在刘海区域内显示
        // 有物理刘海区域，状态栏显示在刘海区域内
        // 无物理刘海区域，状态栏覆盖显示在window绘制区域内
        updatePadding(
            top = rootWindowInsets.systemWindowInsetTop
        )
    }
}
