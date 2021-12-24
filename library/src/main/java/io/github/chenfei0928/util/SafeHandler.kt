package io.github.chenfei0928.util

import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.github.chenfei0928.lifecycle.LifecycleCacheDelegate

/**
 * Created by MrFeng on 2017/6/28.
 */
private class SafeHandler : Handler(Looper.getMainLooper()), LifecycleEventObserver {
    private var mIsDestroyed = false

    override fun dispatchMessage(msg: Message) {
        if (mIsDestroyed) {
            return
        }
        super.dispatchMessage(msg)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            removeCallbacksAndMessages(null)
            mIsDestroyed = true
        } else if (event == Lifecycle.Event.ON_CREATE) {
            mIsDestroyed = false
        }
    }
}

val LifecycleOwner.safeHandler: Handler by LifecycleCacheDelegate<LifecycleOwner, SafeHandler> { _, _ ->
    SafeHandler()
}
