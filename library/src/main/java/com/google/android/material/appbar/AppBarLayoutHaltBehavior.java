package com.google.android.material.appbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.chenfei.util.Log;

import java.lang.reflect.Field;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

/**
 * AppBarLayout的快速滚动中的点击停止滚动
 * Created by MrFeng on 2017/11/28.
 */
public class AppBarLayoutHaltBehavior extends AppBarLayout.Behavior {
    private static final String TAG = "KW_AppBarLayoutHaltBeh";
    private final Field mFlingRunnableField = getFlingRunnableField();
    private boolean mNeedInterceptTouchEvent = false;

    public AppBarLayoutHaltBehavior() {
    }

    public AppBarLayoutHaltBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, AppBarLayout child, MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            Runnable runnable = getFlingRunnable();
            // 根据Fling动画Runnable是否存在判断是否是在自动滑动中，如果是则需要拦截此次滑动事件
            mNeedInterceptTouchEvent = runnable != null;
            if (runnable != null) {
                child.removeCallbacks(runnable);
                clearFlingRunnable();
            }
        } else if (ev.getActionMasked() == MotionEvent.ACTION_UP) {
            // 如果在自动滑动中点击的，则拦截此次点击事件
            if (mNeedInterceptTouchEvent) {
                super.onInterceptTouchEvent(parent, child, ev);
                return true;
            } else {
                return super.onInterceptTouchEvent(parent, child, ev);
            }
        }
        return super.onInterceptTouchEvent(parent, child, ev);
    }

    @Override
    void onFlingFinished(CoordinatorLayout parent, AppBarLayout layout) {
        super.onFlingFinished(parent, layout);
        // 清空该字段，以标记自动滑动完成
        clearFlingRunnable();
    }

    private void clearFlingRunnable() {
        if (mFlingRunnableField != null) {
            try {
                mFlingRunnableField.set(this, null);
            } catch (IllegalAccessException e) {
                Log.e(TAG, "onFlingFinished: ", e);
            }
        }
    }

    private Runnable getFlingRunnable() {
        if (mFlingRunnableField != null) {
            try {
                return (Runnable) mFlingRunnableField.get(this);
            } catch (IllegalAccessException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    private static Field getFlingRunnableField() {
        try {
            Field flingRunnableField = HeaderBehavior.class.getDeclaredField("flingRunnable");
            flingRunnableField.setAccessible(true);
            return flingRunnableField;
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "getFlingRunnableField: ", e);
            return null;
        }
    }
}
