package io.github.chenfei0928.widget;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import io.github.chenfei0928.base.ContextProvider;
import io.github.chenfei0928.util.Log;

/**
 * Toast工具帮助类，缓存View，但不复用Toast
 *
 * @author Admin
 * @date 2015/8/28
 */
public class ToastUtil {
    private static final String TAG = "KW_ToastUtil";
    private static final Handler sHandler = new Handler(Looper.getMainLooper());
    /**
     * Toast暂存，下次使用时取消上一个Toast，防止应用退出后Toast像吃了哔一样往外弹
     */
    private static volatile WeakReference<Toast> sToast;
    private static final ToastFactory sToastFactory;
    private static volatile ToastShowTask sToastShowTask;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            sToastFactory = new ToastFactoryImplQ();
        } else {
            sToastFactory = new ToastFactoryImpl();
        }
    }

    @Deprecated
    public static void showShort(@StringRes int message) {
        Log.w(TAG, "showShort: 无Context参 Toast", new Exception());
        Application context = ContextProvider.getContext();
        showShort(context, context.getString(message));
    }

    @Deprecated
    public static void showShort(String message) {
        Log.w(TAG, "showShort: 无Context参 Toast", new Exception());
        showShort(ContextProvider.getContext(), message);
    }

    public static void showShort(Fragment context, @StringRes int message) {
        showShort(context.getContext(), message);
    }

    public static void showShort(Fragment context, String message) {
        showShort(context.getContext(), message);
    }

    public static void showShort(Context context, @StringRes int message) {
        if (context == null) {
            context = ContextProvider.getContext();
            Log.i(TAG, "showShort: context is null", new NullPointerException());
        }
        showShort(context, context.getString(message));
    }

    public static void showShortThrowable(Context context, Throwable throwable) {
        String message = throwable.getMessage();
        if (TextUtils.isEmpty(message)) {
            message = throwable.toString();
        }
        showShort(context, message);
    }

    /**
     * 将正在展示的toast取消并toast新的message
     * 如果消息为空，将不会显示它，也不会取消正在显示的toast
     */
    public static void showShort(Context context, String message) {
        if (TextUtils.isEmpty(message)) {
            return;
        }
        if (context == null) {
            context = ContextProvider.getContext();
            Log.i(TAG, "showShort: ", new NullPointerException("context is null"));
        }
        cancel();
        if (Looper.myLooper() != Looper.getMainLooper()) {
            sToastShowTask = new ToastShowTask(context, message);
            sHandler.post(sToastShowTask);
        } else {
            Toast toast = sToastFactory.makeText(context, message, Toast.LENGTH_SHORT);
            sToast = new WeakReference<>(toast);
            toast.show();
        }
    }

    public static void cancel() {
        if (sToastShowTask != null) {
            sHandler.removeCallbacks(sToastShowTask);
            sToastShowTask = null;
        }
        if (sToast != null) {
            Toast toast = sToast.get();
            if (toast != null) {
                toast.cancel();
            }
            sToast = null;
        }
    }

    private static class ToastShowTask implements Runnable {
        private final Context context;
        private final String msg;

        ToastShowTask(Context context, String msg) {
            this.context = context;
            this.msg = msg;
        }

        @Override
        public void run() {
            showShort(context, msg);
            sToastShowTask = null;
        }
    }

    //<editor-fold defaultstate="collapsing" desc="Toast工厂">
    private interface ToastFactory {
        Toast makeText(Context context, CharSequence text, int duration);
    }

    private static class ToastFactoryImpl implements ToastFactory {
        private View sToastView;

        @Override
        public Toast makeText(Context context, CharSequence text, int duration) {
            // 11以下缓存toastView，以提升toast创建的性能
            // 10 以上添加了 LayoutInflater#tryInflatePrecompiled 来优化layoutInflater性能，但10中并没有启用
            Toast toast = new Toast(context);
            toast.setView(getToastView(context));
            toast.setText(text);
            return toast;
        }

        @SuppressLint("ShowToast")
        private View getToastView(Context context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return null;
            }
            if (sToastView == null) {
                sToastView = Toast.makeText(context.getApplicationContext(), "", Toast.LENGTH_SHORT).getView();
            }
            return sToastView;
        }
    }

    private static class ToastFactoryImplQ implements ToastFactory {
        @Override
        public Toast makeText(Context context, CharSequence text, int duration) {
            // 11 以上不允许去setView方式设置toastView，直接通过工厂方法创建toast
            return Toast.makeText(context, text, Toast.LENGTH_SHORT);
        }
    }
    //</editor-fold>
}
