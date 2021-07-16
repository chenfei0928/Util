package com.chenfei.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.github.lzyzsd.jsbridge.BridgeWebView;

/**
 * 提供webView的监听回调
 *
 * @author MrFeng
 * @date 2017/8/22
 */
public class ObservableWebView extends BridgeWebView {
    private int lastContentHeight = 0;
    private OnScrollChangedCallback mOnScrollChangedCallback;
    private OnContentChangeListener mOnContentChangeListener;

    public ObservableWebView(final Context context) {
        super(context);
    }

    public ObservableWebView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public ObservableWebView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (this.mOnScrollChangedCallback != null) {
            this.mOnScrollChangedCallback.onScrollChange(this, l, t, oldl, oldt);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mOnContentChangeListener != null) {
            int contentHeight = getContentHeight();
            if (contentHeight != lastContentHeight) {
                post(() -> mOnContentChangeListener.onContentChange(contentHeight));
            }
            lastContentHeight = contentHeight;
        }
    }

    public void setOnScrollChangedCallback(OnScrollChangedCallback onScrollChangedCallback) {
        mOnScrollChangedCallback = onScrollChangedCallback;
    }

    public void setOnContentChangeListener(OnContentChangeListener onContentChangeListener) {
        this.mOnContentChangeListener = onContentChangeListener;
    }

    /**
     * Impliment in the activity/fragment/view that you want to listen to the webview
     */
    public interface OnScrollChangedCallback {
        void onScrollChange(ObservableWebView view, int scrollX, int scrollY, int oldScrollX, int oldScrollY);
    }

    /**
     * 监听内容高度发生变化
     */
    public interface OnContentChangeListener {
        void onContentChange(int contentHeight);
    }
}
