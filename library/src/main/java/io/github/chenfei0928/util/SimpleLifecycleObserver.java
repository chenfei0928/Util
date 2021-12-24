package io.github.chenfei0928.util;

import org.jetbrains.annotations.NotNull;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

/**
 * @author pxx
 * @date 18-7-4
 */
public abstract class SimpleLifecycleObserver implements LifecycleEventObserver {

    @Override
    public final void onStateChanged(@NotNull LifecycleOwner source, @NotNull Lifecycle.Event event) {
        switch (event) {
            case ON_CREATE:
                onCreate(source);
                break;
            case ON_START:
                onStart(source);
                break;
            case ON_RESUME:
                onResume(source);
                break;
            case ON_PAUSE:
                onPause(source);
                break;
            case ON_STOP:
                onStop(source);
                break;
            case ON_DESTROY:
                onDestroy(source);
                break;
            case ON_ANY:
                throw new IllegalArgumentException("ON_ANY must not been send by anybody");
        }
    }

    public void onCreate(LifecycleOwner owner) {
    }

    public void onStart(LifecycleOwner owner) {
    }

    public void onResume(LifecycleOwner owner) {
    }

    public void onPause(LifecycleOwner owner) {
    }

    public void onStop(LifecycleOwner owner) {
    }

    public void onDestroy(LifecycleOwner owner) {
    }
}
