package io.github.chenfei0928.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.github.lzyzsd.jsbridge.BridgeWebView;

/**
 * 提供webView的监听回调
 * <p>
 * 父类使用了 [JsBridge](https://github.com/uknownothingsnow/JsBridge) 在使用前确认引入了依赖
 *
 * @author MrFeng
 * @date 2017/8/22
 */
public class ObservableWebView extends BridgeWebView {
    private int lastContentHeight = 0;
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

    public void setOnContentChangeListener(OnContentChangeListener onContentChangeListener) {
        this.mOnContentChangeListener = onContentChangeListener;
    }

    /**
     * 监听内容高度发生变化
     */
    public interface OnContentChangeListener {
        void onContentChange(int contentHeight);
    }
}
