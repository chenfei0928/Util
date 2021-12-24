package io.github.chenfei0928.view;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2021-06-24 14:54
 * @see <a href="https://www.jianshu.com/p/ce14c7d96b0c">博文</a>
 */
public class TouchDelegateComposite extends TouchDelegate {
    private static final Rect USELESS_RECT = new Rect();
    private final List<TouchDelegate> mDelegates = new ArrayList<>(8);

    public TouchDelegateComposite(@NonNull View view) {
        super(USELESS_RECT, view);
    }

    public void addDelegate(@NonNull TouchDelegate delegate) {
        mDelegates.add(delegate);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        for (TouchDelegate delegate : mDelegates) {
            if (delegate.onTouchEvent(event)) {
                return true;
            }
        }
        return false;
    }
}
