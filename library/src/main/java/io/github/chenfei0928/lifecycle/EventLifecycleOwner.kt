package io.github.chenfei0928.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * 永生生命周期宿主
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-08-10 16:08
 */
class EventLifecycleOwner(
    private val state: State
) : LifecycleOwner {
    override val lifecycle: Lifecycle = object : Lifecycle() {
        override fun addObserver(observer: LifecycleObserver) {
            // noop
        }

        override fun removeObserver(observer: LifecycleObserver) {
            // noop
        }

        override val currentState: State = state
    }

    companion object {
        val immortal = EventLifecycleOwner(State.RESUMED)
        val dead = EventLifecycleOwner(State.DESTROYED)
    }
}
