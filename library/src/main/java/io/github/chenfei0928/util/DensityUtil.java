package io.github.chenfei0928.util;

import android.util.DisplayMetrics;

import io.github.chenfei0928.base.ContextProvider;

/**
 * @author MrFeng
 * @date 2017/8/10
 */
public class DensityUtil {
    private static final DisplayMetrics metrics =
            ContextProvider.Companion.getContext().getResources().getDisplayMetrics();

    private DensityUtil() {
    }

    /**
     * {@link android.util.TypedValue#complexToDimensionPixelSize(int, android.util.DisplayMetrics)}
     * {@link android.util.TypedValue#applyDimension(int, float, android.util.DisplayMetrics)}
     * {@code
     * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics())
     * }
     */
    public static int dip2px(float dpValue) {
        return (int) (dpValue * metrics.density + 0.5F);
    }

    public static int px2dip(float pxValue) {
        return (int) (pxValue / metrics.density + 0.5F);
    }

    public static int sp2px(float spValue) {
        return (int) (spValue * metrics.scaledDensity + 0.5f);
    }
}
