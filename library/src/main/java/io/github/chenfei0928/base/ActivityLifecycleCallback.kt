package io.github.chenfei0928.base

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentViewLifecycleCf0928UtilAccessor

/**
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-02-17 17:52
 */
internal object ActivityLifecycleCallback : Application.ActivityLifecycleCallbacks {

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity is FragmentActivity) {
            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                FragmentViewLifecycleCf0928UtilAccessor, true
            )
        }
    }

    override fun onActivityStarted(activity: Activity) {
        // noop
    }

    override fun onActivityResumed(activity: Activity) {
        // noop
    }

    override fun onActivityPaused(activity: Activity) {
        // noop
    }

    override fun onActivityStopped(activity: Activity) {
        // noop
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // noop
    }

    override fun onActivityDestroyed(activity: Activity) {
        // noop
    }
}
