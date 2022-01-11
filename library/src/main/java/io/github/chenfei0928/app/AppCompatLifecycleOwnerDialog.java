package io.github.chenfei0928.app;

import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import io.github.chenfei0928.os.SafeHandlerKt;

/**
 * 带生命周期宿主回调的Dialog
 *
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2020-08-13 10:16
 */
public class AppCompatLifecycleOwnerDialog extends AppCompatDialog implements LifecycleOwner {
    private final Handler mHandler = SafeHandlerKt.getSafeHandler(this);
    private final LifecycleRegistry mLifecycle = new LifecycleRegistry(this);

    public AppCompatLifecycleOwnerDialog(Context context) {
        super(context);
    }

    public AppCompatLifecycleOwnerDialog(Context context, int theme) {
        super(context, theme);
    }

    protected AppCompatLifecycleOwnerDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mLifecycle;
    }

    @Override
    public void create() {
        super.create();
        mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
    }

    @Override
    protected void onStart() {
        // 未初始化生命周期（不是执行create方法的情况，要先通知create事件）
        if (mLifecycle.getCurrentState() == Lifecycle.State.INITIALIZED) {
            mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
        }
        super.onStart();
        // 子类重写会影响时间发出时回调执行的时间，发送回调在子类执行完成后通知事件
        mHandler.post(() -> {
            mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START);
            mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 子类重写会影响时间发出时回调执行的时间，发送回调在子类执行完成后通知事件
        mHandler.post(() -> {
            mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE);
            mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
        });
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        // dialog没有destroy回调，但依然需要一个事件通知
        mHandler.post(() -> mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY));
    }
}
