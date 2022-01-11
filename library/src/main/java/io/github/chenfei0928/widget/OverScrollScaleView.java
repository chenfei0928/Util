package io.github.chenfei0928.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.widget.NestedScrollView;
import io.github.chenfei0928.animation.AnimatorKt;

/**
 * 支持超量滑动并放大其中一个子View
 * 使用时内部嵌套一个子ViewGroup，子ViewGroup中放入需要进行超量滑动的目标View和放大的子View
 * Created by MrFeng on 2017/8/3.
 */
public class OverScrollScaleView extends NestedScrollView {
    private final static float OFFSET_RADIO = 1.8f; // support iOS like pull feature.
    private float mLastY = -1; // save event y
    private View mOverScrollScaleView;
    private View mContent;

    public OverScrollScaleView(Context context) {
        this(context, null);
    }

    public OverScrollScaleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OverScrollScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    /**
     * 设置超量滑动的目标view
     *
     * @param scaleView 超量滑动时对其进行放大显示的View
     * @param content   超量滑动的内容View
     */
    public void setOverScrollScaleView(View scaleView, View content) {
        mOverScrollScaleView = scaleView;
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
                    float mTotalDy = mOverScrollScaleView.getScaleX();
                    mTotalDy += doY / 1500;
                    mOverScrollScaleView.setScaleX(mTotalDy);
                    mOverScrollScaleView.setScaleY(mTotalDy);
                    mContent.setTranslationY(doY + mContent.getTranslationY());
                } else if (mContent.getTranslationY() > 0 && getScrollY() == 0) {
                    // 向上滑动，并且需要先处理顶部缩放
                    float mTotalDy = mOverScrollScaleView.getScaleX();
                    mTotalDy += doY / 1500;
                    if (mTotalDy >= 1) {
                        mOverScrollScaleView.setScaleX(mTotalDy);
                        mOverScrollScaleView.setScaleY(mTotalDy);
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
        if (mContent.getTranslationY() == 0) {
            return;
        }
        ValueAnimator anim = ValueAnimator.ofFloat(mOverScrollScaleView.getScaleX(), 1f).setDuration(200);
        anim.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            mOverScrollScaleView.setScaleX(value);
            mOverScrollScaleView.setScaleY(value);
        });
        anim.start();
        mContent.animate().cancel();
        mContent.animate()
                .translationYBy(mContent.getTranslationY())
                .translationY(0)
                .setDuration(AnimatorKt.ANIMATE_LAYOUT_CHANGES_DEFAULT_DURATION)
                .start();
    }
}
