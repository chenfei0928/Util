package com.chenfei.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.chenfei.lib_base.R;

import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * Created by MrFeng on 2016/10/18.
 */
public class FitWindowConstraintLayout extends ConstraintLayout {
    private final boolean fitStatusBar;

    public FitWindowConstraintLayout(Context context) {
        this(context, null);
    }

    public FitWindowConstraintLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FitWindowConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        @SuppressLint("CustomViewStyleable") final TypedArray a =
                context.obtainStyledAttributes(attrs, R.styleable.FitWindowLayout);
        fitStatusBar = a.getBoolean(R.styleable.FitWindowLayout_fitStatusBar, false);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        FitWindowLayoutKt.onMeasure(this, fitStatusBar);
    }

    /**
     * 当系统UI大小变化时将触发该重写方法
     */
    @Override
    protected boolean fitSystemWindows(Rect insets) {
        FitWindowLayoutKt.fitSystemWindows(this, insets, fitStatusBar);
        return super.fitSystemWindows(insets);
    }
}
