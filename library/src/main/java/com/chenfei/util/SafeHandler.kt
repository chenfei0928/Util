package com.chenfei.util

import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.chenfei.base.fragment.BaseFragment
import com.chenfei.util.UiTaskExecutor.Companion.runOnUiThread

/**
 * Created by MrFeng on 2017/6/28.
 */
class SafeHandler : Handler(Looper.getMainLooper()), LifecycleEventObserver {
    private var mIsDestroyed = false

    override fun dispatchMessage(msg: Message) {
        if (mIsDestroyed) {
            return
        }
        super.dispatchMessage(msg)
    }

    fun setDestroyed(destroyed: Boolean) {
        mIsDestroyed = destroyed
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            mIsDestroyed = true
        } else if (event == Lifecycle.Event.ON_CREATE) {
            mIsDestroyed = false
        }
    }

    companion object {

        fun getOrCreate(owner: LifecycleOwner): Handler {
            return when (owner) {
                is BaseFragment -> {
                    owner.mHandler
                }
                else -> {
                    create(owner)
                }
            }
        }

        internal fun create(owner: LifecycleOwner): Handler {
            val handler = SafeHandler()
            ExecutorUtil.runOnUiThread {
                owner.lifecycle.addObserver(handler)
            }
            return handler
        }
    }
}
