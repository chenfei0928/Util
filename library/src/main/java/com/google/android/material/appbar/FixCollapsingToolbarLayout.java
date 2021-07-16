package com.google.android.material.appbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * @see <a href="https://issuetracker.google.com/issues/66999164">Google Issue</a>
 * Created by MrFeng on 2017/12/5.
 */
public class FixCollapsingToolbarLayout extends CollapsingToolbarLayout {
    public FixCollapsingToolbarLayout(Context context) {
        super(context);
    }

    public FixCollapsingToolbarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixCollapsingToolbarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int mode = View.MeasureSpec.getMode(heightMeasureSpec);
        final int topInset = lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;
        if (mode == View.MeasureSpec.UNSPECIFIED && topInset > 0) {
            // If we have a top inset and we're set to wrap_content height we need to make sure
            // we add the top inset to our height, therefore we re-measure
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                    getMeasuredHeight() - topInset, View.MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
