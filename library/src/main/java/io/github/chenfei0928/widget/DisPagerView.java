package io.github.chenfei0928.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

/**
 * 禁用了左右滑动的ViewPager，用来管理Fragment最简单实现
 * Created by Admin on 2015/11/23.
 */
public class DisPagerView extends ViewPager {
    private boolean mAllowScroll = false;

    public DisPagerView(Context context) {
        super(context);
    }

    public DisPagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAllowScroll(boolean allowScroll) {
        mAllowScroll = allowScroll;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mAllowScroll && super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mAllowScroll && super.onTouchEvent(ev);
    }
}
