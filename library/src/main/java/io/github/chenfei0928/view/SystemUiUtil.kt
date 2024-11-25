package io.github.chenfei0928.view

import android.content.Context
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.Px
import androidx.core.content.getSystemService
import androidx.core.content.res.use

/**
 * 系统ui工具类，用于获取系统ui尺寸（状态栏、导航栏）
 *
 * @author Admin
 * @date 2015/9/23
 */
object SystemUiUtil {
    private var status_bar_height = View.NO_ID
    private var navigation_bar_height = View.NO_ID

    @JvmStatic
    fun getWebViewUserAgentSystemUiSafeAreaInsetsDescription(context: Context): String {
        return " [safeArea/" +
                getStatusBarHeight(context) +
                "," +
                checkGetNavigationBarHeight(context) +
                ']'
    }

    /**
     * 获取手机状态栏高度
     */
    @Px
    fun getStatusBarHeight(context: Context): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.getSystemService<WindowManager>()?.let { wm ->
                val windowMetrics = wm.currentWindowMetrics
                val windowInsets = windowMetrics.windowInsets
                val insets = windowInsets.getInsetsIgnoringVisibility(
                    WindowInsets.Type.statusBars() or WindowInsets.Type.displayCutout()
                )
                return insets.top
            }
        }
        val resources = context.resources
        if (status_bar_height == View.NO_ID) {
            status_bar_height = resources.getIdentifier(
                "status_bar_height", "dimen", "android"
            )
        }
        return if (status_bar_height == View.NO_ID) {
            resources.getDimensionPixelSize(status_bar_height)
        } else 0
    }

    @Px
    fun checkGetNavigationBarHeight(context: Context): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.getSystemService<WindowManager>()?.let { wm ->
                val windowInsets = wm.currentWindowMetrics.windowInsets
                return if (windowInsets.isVisible(WindowInsets.Type.navigationBars())) {
                    val insets = windowInsets.getInsetsIgnoringVisibility(
                        WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout()
                    )
                    insets.bottom
                } else {
                    0
                }
            }
        }
        val resources = context.resources
        if (navigation_bar_height == View.NO_ID) {
            navigation_bar_height = resources.getIdentifier(
                "navigation_bar_height", "dimen", "android"
            )
        }
        // 获取NavigationBar的高度
        return if (navigation_bar_height == View.NO_ID) {
            resources.getDimensionPixelSize(navigation_bar_height)
        } else 0
    }

    @Px
    fun getActionBarSize(context: Context): Int {
        return context.theme.obtainStyledAttributes(
            intArrayOf(android.R.attr.actionBarSize)
        ).use {
            it.getDimensionPixelSize(0, 0)
        }
    }
}
