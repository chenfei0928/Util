package io.github.chenfei0928.lifecycle

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
    override val lifecycle: Lifecycle
        get() = ImmortalLifecycle

    private object ImmortalLifecycle : Lifecycle() {
        override fun addObserver(observer: LifecycleObserver) {
            // noop
        }

        override fun removeObserver(observer: LifecycleObserver) {
            // noop
        }

        override val currentState: State
            get() = State.RESUMED
    }
}
