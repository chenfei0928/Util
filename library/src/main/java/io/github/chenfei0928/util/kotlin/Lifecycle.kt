package io.github.chenfei0928.util.kotlin

import androidx.lifecycle.GenericLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

val Lifecycle.isAlive
    get() = currentState != Lifecycle.State.DESTROYED

val Lifecycle.isResumed
    get() = currentState == Lifecycle.State.RESUMED

inline fun Lifecycle.onEvent(crossinline action: (Lifecycle.Event) -> Unit): GenericLifecycleObserver {
    val observer = GenericLifecycleObserver { _, e -> action(e) }
    this.addObserver(observer)
    return observer
}

inline fun Lifecycle.bindUntilFirstEvent(
    event: Lifecycle.Event, crossinline action: (Lifecycle.Event) -> Unit
): GenericLifecycleObserver {
    val observer = object : GenericLifecycleObserver {
        override fun onStateChanged(l: LifecycleOwner, e: Lifecycle.Event) {
            if (e == event) {
                action(e)
                removeObserver(this)
            }
        }
    }
    this.addObserver(observer)
    return observer
}
