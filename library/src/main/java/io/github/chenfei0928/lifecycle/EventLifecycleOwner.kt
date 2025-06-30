package io.github.chenfei0928.lifecycle

import android.annotation.SuppressLint
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Lifecycling

/**
 * 永远处于某个生命周期状态的生命周期宿主
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-08-10 16:08
 */
class EventLifecycleOwner(
    private val state: State
) : LifecycleOwner {
    private val events = when (state) {
        State.DESTROYED -> arrayOf()
        State.INITIALIZED -> arrayOf()
        State.CREATED -> arrayOf(Lifecycle.Event.ON_CREATE)
        State.STARTED -> arrayOf(Lifecycle.Event.ON_CREATE, Lifecycle.Event.ON_START)
        State.RESUMED -> arrayOf(
            Lifecycle.Event.ON_CREATE,
            Lifecycle.Event.ON_START,
            Lifecycle.Event.ON_RESUME
        )
    }

    override val lifecycle: Lifecycle = object : Lifecycle() {
        @SuppressLint("RestrictedApi")
        override fun addObserver(observer: LifecycleObserver) {
            if (events.isEmpty())
                return
            val eventObserver = Lifecycling.lifecycleEventObserver(observer)
            events.forEach { eventObserver.onStateChanged(this@EventLifecycleOwner, it) }
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
