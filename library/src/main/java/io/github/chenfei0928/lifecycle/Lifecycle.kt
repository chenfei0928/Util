package io.github.chenfei0928.lifecycle

import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.github.chenfei0928.os.safeHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

val Lifecycle.isAlive
    get() = currentState != Lifecycle.State.DESTROYED

val Lifecycle.isResumed
    get() = currentState == Lifecycle.State.RESUMED

inline fun LifecycleOwner.onEvent(crossinline action: (Lifecycle.Event) -> Unit): LifecycleEventObserver {
    val observer = LifecycleEventObserver { _, e -> action(e) }
    lifecycle.addObserver(observer)
    return observer
}

inline fun LifecycleOwner.bindUntilFirstEvent(
    e: Lifecycle.Event, crossinline action: (Lifecycle.Event) -> Unit
): LifecycleEventObserver {
    @Suppress("kotlin:S6516")
    val observer = object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == e) {
                action(event)
                lifecycle.removeObserver(this)
            }
        }
    }
    lifecycle.addObserver(observer)
    return observer
}

/**
 * 等待指定fragment销毁，通常可以用于[DialogFragment]的showAndAwait功能
 */
suspend fun LifecycleOwner.awaitDestroy() {
    suspendCancellableCoroutine { continuation ->
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY && continuation.isActive) {
                continuation.resume(Unit)
            }
        }
        lifecycle.addObserver(observer)
        continuation.invokeOnCancellation {
            safeHandler.post { lifecycle.removeObserver(observer) }
        }
    }
}
