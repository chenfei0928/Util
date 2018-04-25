package com.chenfei.library.view;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chenfei.library.util.ExecutorUtil;

import kotlin.jvm.functions.Function2;

/**
 * 使用自定义的次线程执行器来替换其类的布局加载器，并简化逻辑
 * {@link android.support.v4.view.AsyncLayoutInflater}
 * Created by MrFeng on 2018/4/10.
 */
public class AsyncLayoutInflater {
    private static final String TAG = "AsyncLayoutInflater";
    private final LayoutInflater mInflater;

    public AsyncLayoutInflater(@NonNull Context context) {
        mInflater = new BasicInflater(context);
    }

    /**
     * 通过自定义的布局创建者在子线程创建子布局
     *
     * @param onCreateView 子视图创建者
     * @param parent       父布局，用于生成LayoutParam
     * @param callback     子视图创建完成后在主线程的回调
     * @param <VG>         父布局的类型
     */
    @UiThread
    public <VG extends ViewGroup> void inflate(
            Function2<LayoutInflater, VG, View> onCreateView, @Nullable VG parent,
            @NonNull OnInflateFinishedListener callback) {
        ExecutorUtil.execute(() -> {
            try {
                return onCreateView.invoke(mInflater, parent);
            } catch (RuntimeException ex) {
                // Probably a Looper failure, retry on the UI thread
                Log.w(TAG, "Failed to inflate resource in the background! Retrying on the UI"
                        + " thread", ex);
                return null;
            }
        }, view -> {
            if (view == null) {
                view = onCreateView.invoke(mInflater, parent);
            }
            callback.onInflateFinished(view);
        });
    }

    /**
     * 通过布局文件创建者在子线程创建子布局
     *
     * @param resid    子视图布局id
     * @param parent   父布局，用于生成LayoutParam
     * @param callback 子视图创建完成后在主线程的回调
     * @param <VG>     父布局的类型
     */
    @UiThread
    public <VG extends ViewGroup> void inflate(
            @LayoutRes int resid, @Nullable VG parent,
            @NonNull OnInflateFinishedListener callback) {
        ExecutorUtil.execute(() -> {
            try {
                return mInflater.inflate(resid, parent, false);
            } catch (RuntimeException ex) {
                // Probably a Looper failure, retry on the UI thread
                Log.w(TAG, "Failed to inflate resource in the background! Retrying on the UI"
                        + " thread", ex);
                return null;
            }
        }, view -> {
            if (view == null) {
                view = mInflater.inflate(resid, parent, false);
            }
            callback.onInflateFinished(view);
        });
    }

    public interface OnInflateFinishedListener {
        void onInflateFinished(@NonNull View view);
    }

    private static class BasicInflater extends LayoutInflater {
        private static final String[] sClassPrefixList = {
                "android.widget.",
                "android.webkit.",
                "android.app."
        };

        BasicInflater(Context context) {
            super(context);
        }

        @Override
        public LayoutInflater cloneInContext(Context newContext) {
            return new BasicInflater(newContext);
        }

        @Override
        protected View onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
            for (String prefix : sClassPrefixList) {
                try {
                    View view = createView(name, prefix, attrs);
                    if (view != null) {
                        return view;
                    }
                } catch (ClassNotFoundException e) {
                    // In this case we want to let the base class take a crack
                    // at it.
                }
            }
            return super.onCreateView(name, attrs);
        }
    }
}
