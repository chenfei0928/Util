package com.chenfei.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * 永生生命周期宿主
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-08-10 16:08
 */
object ImmortalLifecycleOwner : LifecycleOwner {
    override fun getLifecycle(): Lifecycle {
        return ImmortalLifecycle
    }

    private object ImmortalLifecycle : Lifecycle() {
        override fun addObserver(observer: LifecycleObserver) {
        }

        override fun removeObserver(observer: LifecycleObserver) {
        }

        override fun getCurrentState(): State {
            return State.RESUMED
        }
    }
}
