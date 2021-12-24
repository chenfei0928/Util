package io.github.chenfei0928.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Insets;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;

import androidx.annotation.Px;

/**
 * 系统ui工具类，用于获取系统ui尺寸（状态栏、导航栏）
 *
 * @author Admin
 * @date 2015/9/23
 */
public class SystemUiUtil {
    private static int status_bar_height = View.NO_ID;
    private static int navigation_bar_height = View.NO_ID;

    public static String getWebViewUserAgentSystemUiSafeAreaInsetsDescription(Context context) {
        return " [safeArea/" +
                getStatusBarHeight(context) +
                "," +
                checkGetNavigationBarHeight(context) +
                ']';
    }

    /**
     * 获取手机状态栏高度
     */
    @Px
    public static int getStatusBarHeight(Context context) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            return 0;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            WindowMetrics windowMetrics = wm.getCurrentWindowMetrics();
            WindowInsets windowInsets = windowMetrics.getWindowInsets();
            Insets insets = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.statusBars() | WindowInsets.Type.displayCutout());
            return insets.top;
        } else {
            Resources resources = context.getResources();
            if (status_bar_height == View.NO_ID) {
                status_bar_height = resources.getIdentifier("status_bar_height",
                        "dimen", "android");
            }
            return resources.getDimensionPixelSize(status_bar_height);
        }
    }

    @Px
    public static int checkGetNavigationBarHeight(Context activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
            WindowInsets windowInsets = wm.getCurrentWindowMetrics().getWindowInsets();
            if (windowInsets.isVisible(WindowInsets.Type.navigationBars())) {
                Insets insets = windowInsets.getInsetsIgnoringVisibility(
                        WindowInsets.Type.navigationBars() | WindowInsets.Type.displayCutout());
                return insets.bottom;
            } else {
                return 0;
            }
        } else {
            Resources resources = activity.getResources();
            if (navigation_bar_height == View.NO_ID) {
                navigation_bar_height = resources.getIdentifier("navigation_bar_height",
                        "dimen", "android");
            }
            // 获取NavigationBar的高度
            return resources.getDimensionPixelSize(navigation_bar_height);
        }
    }

    @Px
    public static int getActionBarSize(Context context) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{android.R.attr.actionBarSize});
        int size = (int) a.getDimension(0, 0);
        a.recycle();
        return size;
    }
}
