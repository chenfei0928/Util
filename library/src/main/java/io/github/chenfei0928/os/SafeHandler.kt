package io.github.chenfei0928.os

import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.github.chenfei0928.lifecycle.LifecycleCacheDelegate
import io.github.chenfei0928.lifecycle.isAlive

/**
 * Created by MrFeng on 2017/6/28.
 */
private class SafeHandler(
    owner: LifecycleOwner,
    private val closeCallback: () -> Unit
) : Handler(Looper.getMainLooper()), LifecycleEventObserver {
    private var mIsAlive = !owner.lifecycle.isAlive

    override fun dispatchMessage(msg: Message) {
        if (!mIsAlive) {
            return
        }
        super.dispatchMessage(msg)
    }

    override fun sendMessageAtTime(msg: Message, uptimeMillis: Long): Boolean {
        if (!mIsAlive) {
            return false
        }
        return super.sendMessageAtTime(msg, uptimeMillis)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            removeCallbacksAndMessages(null)
            closeCallback()
            mIsAlive = false
        } else if (event == Lifecycle.Event.ON_CREATE) {
            mIsAlive = true
        }
    }
}

val LifecycleOwner.safeHandler: Handler by LifecycleCacheDelegate<LifecycleOwner, SafeHandler> { owner, closeCallback ->
    SafeHandler(owner, closeCallback)
}
