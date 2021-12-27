package io.github.chenfei0928.util.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.widget.NestedScrollView;

/**
 * 支持超量滑动并放大其中一个子View
 * 使用时内部嵌套一个子ViewGroup，子ViewGroup中放入需要进行超量滑动的目标View和放大的子View
 * Created by MrFeng on 2017/8/3.
 */
public class OverScrollView extends NestedScrollView {
    private final static float OFFSET_RADIO = 1.8f; // support iOS like pull feature.
    private float mLastY = -1; // save event y
    private View mOverScrollView;
    private View mContent;

    public OverScrollView(Context context) {
        this(context, null);
    }

    public OverScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OverScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    /**
     * 设置超量滑动的目标view
     *
     * @param overScrollView 超量滑动时对其进行放大显示的View
     * @param content        超量滑动的内容View
     */
    public void setOverScrollView(View overScrollView, View content) {
        mOverScrollView = overScrollView;
        mContent = content;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mLastY == -1) {
            mLastY = ev.getY();
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float doY = ev.getY() - mLastY;
                mLastY = ev.getY();
                if (getScrollY() == 0 && doY > 0) {
                    // 下拉
                    doY /= OFFSET_RADIO;
                    float mTotalDy = mOverScrollView.getScaleX();
                    mTotalDy += doY / 1500;
                    mOverScrollView.setScaleX(mTotalDy);
                    mOverScrollView.setScaleY(mTotalDy);
                    mContent.setTranslationY(doY + mContent.getTranslationY());
                } else if (mContent.getTranslationY() > 0 && getScrollY() == 0) {
                    // 向上滑动，并且需要先处理顶部缩放
                    float mTotalDy = mOverScrollView.getScaleX();
                    mTotalDy += doY / 1500;
                    if (mTotalDy >= 1) {
                        mOverScrollView.setScaleX(mTotalDy);
                        mOverScrollView.setScaleY(mTotalDy);
                        mContent.setTranslationY(doY + mContent.getTranslationY());
                        boolean event = super.onTouchEvent(ev);
                        setScrollY(0);
                        return event;
                    }
                }
                break;
            default:
                mLastY = -1; // reset
                reset();
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void reset() {
        if (mContent.getTranslationY() == 0)
            return;
        ValueAnimator anim = ValueAnimator.ofFloat(mOverScrollView.getScaleX(), 1f).setDuration(200);
        anim.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            mOverScrollView.setScaleX(value);
            mOverScrollView.setScaleY(value);
        });
        anim.start();
        mContent.animate().cancel();
        mContent.animate()
                .translationYBy(mContent.getTranslationY())
                .translationY(0)
                .setDuration(200)
                .start();
    }
}
