package io.github.chenfei0928.view.asyncinflater;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import io.github.chenfei0928.concurrent.ExecutorUtil;
import io.github.chenfei0928.util.Log;
import kotlin.jvm.functions.Function2;

/**
 * 使用自定义的次线程执行器来替换其类的布局加载器，并简化逻辑
 * {@link androidx.asynclayoutinflater.view.AsyncLayoutInflater}
 *
 * @author MrFeng
 * @date 2018/4/10
 */
public class AsyncLayoutInflater {
    private static final String TAG = "KW_AsyncLayoutInflater";
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
        /**
         * 当View加载完成后的回调，此方法会在主线程回调
         *
         * @param view 加载完成的View
         */
        @MainThread
        void onInflateFinished(@NonNull View view);
    }

    static class BasicInflater extends LayoutInflater {
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
