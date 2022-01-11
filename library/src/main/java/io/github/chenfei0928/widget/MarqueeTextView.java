package io.github.chenfei0928.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import androidx.appcompat.widget.AppCompatTextView;
import io.github.chenfei0928.util.R;

/**
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2020-09-18 17:06
 */
public class MarqueeTextView extends AppCompatTextView {

    /**
     * 滚动模式-一直滚动
     */
    public static final int SCROLL_FOREVER = 100;
    /**
     * 滚动模式-只滚动一次
     */
    public static final int SCROLL_ONCE = 101;
    /**
     * 默认滚动时间
     */
    private static final int ROLLING_INTERVAL_DEFAULT = 10000;
    /**
     * 第一次滚动默认延迟
     */
    private static final int FIRST_SCROLL_DELAY_DEFAULT = 1000;
    /**
     * 滚动器
     */
    private Scroller mScroller;
    /**
     * 滚动一次的时间
     */
    private int mRollingInterval;
    /**
     * 滚动的初始 X 位置
     */
    private int mXPaused = 0;
    /**
     * 是否暂停
     */
    private boolean mPaused = true;
    /**
     * 是否第一次
     */
    private boolean mFirst = true;
    /**
     * 滚动模式
     */
    private int mScrollMode;
    /**
     * 初次滚动时间间隔
     */
    private int mFirstScrollDelay;
    private Runnable runnable;
    private int rollDuration;
    private boolean isStopToCenter;
    private OnGetRollDurationListener onGetRollDurationListener;

    public MarqueeTextView(Context context) {
        this(context, null);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MarqueeTextView);
        mRollingInterval = typedArray.getInt(R.styleable.MarqueeTextView_mtv_scrollInterval, ROLLING_INTERVAL_DEFAULT);
        mScrollMode = typedArray.getInt(R.styleable.MarqueeTextView_mtv_scrollMode, SCROLL_FOREVER);
        mFirstScrollDelay = typedArray.getInt(R.styleable.MarqueeTextView_mtv_scrollFirstDelay, FIRST_SCROLL_DELAY_DEFAULT);
        typedArray.recycle();
        setSingleLine();
        setEllipsize(null);
    }

    public void setOnGetRollDurationListener(OnGetRollDurationListener onGetRollDurationListener) {
        this.onGetRollDurationListener = onGetRollDurationListener;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(runnable);
    }

    public void setStopToCenter(boolean isStopToCenter) {
        this.isStopToCenter = isStopToCenter;
    }

    /**
     * 开始滚动
     */
    public void startScroll() {
        mPaused = true;
        mFirst = true;
        resumeScroll();
    }

    /**
     * 继续滚动
     */
    public void resumeScroll() {
        if (!mPaused) {
            return;
        }
        // 设置水平滚动
        setHorizontallyScrolling(true);
        setHorizontalFadingEdgeEnabled(true);

        // 使用 LinearInterpolator 进行滚动
        if (mScroller == null) {
            mScroller = new Scroller(this.getContext(), new LinearInterpolator(getContext(), null));
            setScroller(mScroller);
        }
        if (getWidth() > 0) {
            scroll();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                Choreographer.getInstance().postFrameCallback(frameTimeNanos -> scroll());
            } else {
                post(this::scroll);
            }
        }
    }

    private void scroll() {
        if (mXPaused == 0)
            mXPaused = -1 * getMeasuredWidth();
        int scrollingLen = calculateScrollingLen();
        //滚动的距离
        int distance = scrollingLen - mXPaused;
        double durationDouble = mRollingInterval * distance * 1.00000 / scrollingLen;
        if (scrollingLen < getWidth()) {
            durationDouble = durationDouble / (getWidth() / (float) scrollingLen);
        }
        int tmpDistance = distance;
        rollDuration = (int) durationDouble;
        if (isStopToCenter && mXPaused < 0) {
            if (scrollingLen >= getWidth())
                distance = Math.abs(mXPaused);
            else
                distance = Math.abs(mXPaused) - (getWidth() - scrollingLen) / 2;
            rollDuration = (int) (rollDuration / (tmpDistance * 1.0f / distance));
        }
        final int finalDistance = distance;
        if (mFirst) {
            callOnFirstGetRollDuration(rollDuration);
            postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    setVisibility(View.VISIBLE);//gone不能获取宽高，需使用invisible
                    mScroller.startScroll(mXPaused, 0, finalDistance, 0, rollDuration);
                    invalidate();
                    mPaused = false;
                }
            }, mFirstScrollDelay);
        } else {
            callOnFirstGetRollDuration(rollDuration);
            mScroller.startScroll(mXPaused, 0, distance, 0, rollDuration);
            invalidate();
            mPaused = false;
        }
    }

    private void callOnFirstGetRollDuration(int rollDuration) {
        if (onGetRollDurationListener != null) {
            onGetRollDurationListener.onFirstGetRollDuration(rollDuration);
            onGetRollDurationListener = null;
        }
    }

    /**
     * 暂停滚动
     */
    public void pauseScroll() {
        if (null == mScroller)
            return;

        if (mPaused)
            return;

        mPaused = true;

        mXPaused = mScroller.getCurrX();

        mScroller.abortAnimation();
    }

    /**
     * 停止滚动，并回到初始位置
     */
    public void stopScroll() {
        if (null == mScroller) {
            return;
        }
        mPaused = true;
//        mScroller.startScroll(0, 0, 0, 0, 0);//src
        mXPaused = -1 * getMeasuredWidth();
//        mScroller.startScroll(mXPaused, 0, mXPaused, 0, 0);
        setHorizontalFadingEdgeEnabled(false);
    }

    /**
     * 计算滚动的距离
     *
     * @return 滚动的距离
     */
    private int calculateScrollingLen() {
        TextPaint tp = getPaint();
        Rect rect = new Rect();
        String strTxt = getText().toString();
        tp.getTextBounds(strTxt, 0, strTxt.length(), rect);
        return rect.width();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (null == mScroller) {
            return;
        }
        if (mScroller.isFinished() && (!mPaused)) {
            if (mScrollMode == SCROLL_ONCE) {
                stopScroll();
                return;
            }
            mPaused = true;
            mXPaused = -1 * getMeasuredWidth();
            mFirst = false;
            this.resumeScroll();
        }
    }

    /**
     * 获取滚动一次的时间(文本的宽度刚好和控件的宽度相等时的时间)
     */
    public int getRndDuration() {
        return mRollingInterval;
    }

    /**
     * 设置滚动一次的时间(文本的宽度刚好和控件的宽度相等时的时间)
     */
    public void setRndDuration(int duration) {
        this.mRollingInterval = duration;
    }

    //实际滚动的时间，可能为0
    public int getRollDuration() {
        return rollDuration;
    }

    /**
     * 获取滚动模式
     */
    public int getScrollMode() {
        return this.mScrollMode;
    }

    /**
     * 设置滚动模式
     */
    public void setScrollMode(int mode) {
        this.mScrollMode = mode;
    }

    /**
     * 获取第一次滚动延迟
     */
    public int getScrollFirstDelay() {
        return mFirstScrollDelay;
    }

    /**
     * 设置第一次滚动延迟
     */
    public void setScrollFirstDelay(int delay) {
        this.mFirstScrollDelay = delay;
    }

    public boolean isPaused() {
        return mPaused;
    }

    public interface OnGetRollDurationListener {
        void onFirstGetRollDuration(int rollDuration);
    }
}
