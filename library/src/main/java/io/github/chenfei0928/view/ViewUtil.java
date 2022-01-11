package io.github.chenfei0928.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

/**
 * View设置工具类
 *
 * @author Admin
 * @date 2015/8/19
 */
public class ViewUtil {

    @ColorInt
    public static int getThemeColor(Context context) {
        // 由于Service没有主题，只能获取ApplicationInfo中的主题id，并根据其获取主题色
        TypedArray array = context.obtainStyledAttributes(context.getApplicationInfo().theme,
                new int[]{androidx.appcompat.R.attr.colorPrimary});
        int color = array.getColor(0, Color.BLACK);
        array.recycle();
        return color;
    }

    @ColorInt
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public static int getColorControlHighlight(Context context) {
        // 由于Service没有主题，只能获取ApplicationInfo中的主题id，并根据其获取主题色
        TypedArray array = context.obtainStyledAttributes(context.getApplicationInfo().theme,
                new int[]{android.R.attr.colorControlHighlight});
        int color = array.getColor(0, 0x1a000000);
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
                String pkgName;
                switch (id & 0xff000000) {
                    case 0x7f000000:
                        pkgName = "app";
                        break;
                    case 0x01000000:
                        pkgName = "android";
                        break;
                    default:
                        pkgName = r.getResourcePackageName(id);
                        break;
                }
                String typename = r.getResourceTypeName(id);
                String entryName = r.getResourceEntryName(id);
                out.append(" ");
                out.append(pkgName);
                out.append(":");
                out.append(typename);
                out.append("/");
                out.append(entryName);
            } catch (Resources.NotFoundException e) {
                out.append(v);
            }
        }
        return out.toString();
    }
}
