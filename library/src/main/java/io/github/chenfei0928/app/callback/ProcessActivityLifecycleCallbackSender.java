package io.github.chenfei0928.app.callback;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import io.github.chenfei0928.app.ProcessUtil;

/**
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2021-01-13 16:10
 */
final class ProcessActivityLifecycleCallbackSender implements Application.ActivityLifecycleCallbacks {
    static final String ACTIVITY_INFO = "activityInfo";
    static final String ACTION = "io.github.chenfei0928.util.MULTI_PROCESS_ACTIVITY_LIFECYCLE_CALLBACK";

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        sendBroadcast(activity, Lifecycle.Event.ON_CREATE);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        sendBroadcast(activity, Lifecycle.Event.ON_START);
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        sendBroadcast(activity, Lifecycle.Event.ON_RESUME);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        sendBroadcast(activity, Lifecycle.Event.ON_PAUSE);
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        sendBroadcast(activity, Lifecycle.Event.ON_STOP);
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        // noop
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        sendBroadcast(activity, Lifecycle.Event.ON_DESTROY);
    }

    private void sendBroadcast(@NonNull Activity activity, @NonNull Lifecycle.Event event) {
        ActivityInfo activityInfo = new ActivityInfo(
                ProcessUtil.getProcessName(activity),
                activity.getPackageName(),
                activity.getClass().getName(),
                System.identityHashCode(activity),
                event
        );
        Intent intent = new Intent(ACTION);
        intent.setPackage(activity.getPackageName());
        intent.putExtra(ACTIVITY_INFO, activityInfo);
        activity.sendBroadcast(intent);
    }
}
