package com.chenfei.view;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;

/**
 * 屏幕、显示器工具类
 *
 * @author Admin
 * @date 2015/8/19
 */
public class DisplayUtil {
    /**
     * 通过API获得屏幕方向，如果是横屏或主动设置了屏幕方向为横屏的话，返回true
     * SCREEN_ORIENTATION_PORTRAIT 为竖屏
     * SCREEN_ORIENTATION_LANDSCAPE 为横屏
     * 正方形返回竖屏、false
     */
    public static boolean isDisplayLandscape(Activity activity) {
        int orientation = activity.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return true;
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return false;
        }
        // 取得当前屏幕方向
        int orient = activity.getRequestedOrientation();
        // 若非明确的landscape或portrait时 再透过宽高比例的方法来确认实际显示方向
        // 这会保证orient最终值会是明确的横屏landscape或竖屏portrait
        if (orient != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                && orient != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                && orient != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            // 宽>高为横屏,反正为竖屏
            Point point = getDisplaySize(activity);
            orient = point.x > point.y ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        return orient == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                || orient == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    }

    /**
     * 通过API获得屏幕方向，如果是横屏或主动设置了屏幕方向为横屏的话，返回true
     * SCREEN_ORIENTATION_PORTRAIT 为竖屏
     * SCREEN_ORIENTATION_LANDSCAPE 为横屏
     * 正方形返回竖屏、false
     */
    public static Point getDisplaySize(Activity activity) {
        // 宽>高为横屏,反正为竖屏
        Point point = new Point();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
            activity.getWindowManager().getDefaultDisplay().getRealSize(point);
        } else {
            activity.getWindowManager().getDefaultDisplay().getSize(point);
        }
        return point;
    }
}
