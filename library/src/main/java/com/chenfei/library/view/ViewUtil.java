package com.chenfei.library.view;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

/**
 * View设置工具类
 * Created by Admin on 2015/8/19.
 */
public class ViewUtil {
    private static final String TAG = "ViewUtil";

    /**
     * 通过API获得屏幕方向，如果是横屏或主动设置了屏幕方向为横屏的话，返回true
     * SCREEN_ORIENTATION_PORTRAIT 为竖屏
     * SCREEN_ORIENTATION_LANDSCAPE 为横屏
     * 正方形返回竖屏、false
     */
    public static boolean isDisplayLandscape(Activity activity) {
        // 取得当前屏幕方向
        int orient = activity.getRequestedOrientation();
        // 若非明确的landscape或portrait时 再透过宽高比例的方法来确认实际显示方向
        // 这会保证orient最终值会是明确的横屏landscape或竖屏portrait
        if (orient != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                && orient != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                && orient != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            //宽>高为横屏,反正为竖屏
            Point point = new Point();
            if (Build.VERSION.SDK_INT > 17)
                activity.getWindowManager().getDefaultDisplay().getRealSize(point);
            else
                activity.getWindowManager().getDefaultDisplay().getSize(point);
            orient = point.x > point.y ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        return orient == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                || orient == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    }

    @ColorInt
    public static int getThemeColor(Context context) {
        // 由于Service没有主题，只能获取ApplicationInfo中的主题id，并根据其获取主题色
        TypedArray array = context.obtainStyledAttributes(context.getApplicationInfo().theme,
                new int[]{android.support.v7.appcompat.R.attr.colorPrimary});
        int color = array.getColor(0, Color.BLACK);
        array.recycle();
        return color;
    }

    /**
     * 获得一个view相对于其某个父view层级的坐标
     *
     * @param target 子view
     * @param parent 父view
     * @return 相对坐标[x, y]
     */
    public static float[] getLocationOnViewParent(@NonNull View target, @NonNull ViewParent parent) {
        float x = 0, y = 0;
        for (View viewParent = target; viewParent != parent; viewParent = (View) viewParent.getParent()) {
            x += viewParent.getX();
            y += viewParent.getY();
        }
        return new float[]{x, y};
    }

    /**
     * 计算指定的 View 在屏幕中的坐标。
     */
    public static RectF calcViewScreenLocation(View view) {
        int[] location = new int[2];
        // 获取控件在屏幕中的位置，返回的数组分别为控件左顶点的 x、y 的值
        view.getLocationOnScreen(location);
        return new RectF(location[0], location[1], location[0] + view.getWidth(),
                location[1] + view.getHeight());
    }

    public static boolean isMotionEventInView(View view, MotionEvent ev) {
        return calcViewScreenLocation(view).contains(ev.getRawX(), ev.getRawY());
    }

    private static String getViewIdResString(View v) {
        StringBuilder out = new StringBuilder();
        final int id = v.getId();
        if (id != View.NO_ID) {
            out.append(" #");
            out.append(Integer.toHexString(id));
            final Resources r = v.getContext().getResources();
            try {
                String pkgname;
                switch (id & 0xff000000) {
                    case 0x7f000000:
                        pkgname = "app";
                        break;
                    case 0x01000000:
                        pkgname = "android";
                        break;
                    default:
                        pkgname = r.getResourcePackageName(id);
                        break;
                }
                String typename = r.getResourceTypeName(id);
                String entryname = r.getResourceEntryName(id);
                out.append(" ");
                out.append(pkgname);
                out.append(":");
                out.append(typename);
                out.append("/");
                out.append(entryname);
            } catch (Resources.NotFoundException e) {
                out.append(v.toString());
            }
        }
        return out.toString();
    }
}
