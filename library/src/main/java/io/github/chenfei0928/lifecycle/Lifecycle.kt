package io.github.chenfei0928.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

val Lifecycle.isAlive
    get() = currentState != Lifecycle.State.DESTROYED

val Lifecycle.isResumed
    get() = currentState == Lifecycle.State.RESUMED

inline fun Lifecycle.onEvent(crossinline action: (Lifecycle.Event) -> Unit): LifecycleEventObserver {
    val observer = LifecycleEventObserver { _, e -> action(e) }
    this.addObserver(observer)
    return observer
}

inline fun Lifecycle.bindUntilFirstEvent(
    e: Lifecycle.Event, crossinline action: (Lifecycle.Event) -> Unit
): LifecycleEventObserver {
    @Suppress("kotlin:S6516")
    val observer = object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == e) {
                action(event)
                removeObserver(this)
            }
        }
    }
    this.addObserver(observer)
    return observer
}
