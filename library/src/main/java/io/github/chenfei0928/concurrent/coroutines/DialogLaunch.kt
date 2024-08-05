package io.github.chenfei0928.concurrent.coroutines

import android.app.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.github.chenfei0928.lifecycle.ImmortalLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume

inline fun <D> D.launchWithShow(
    parentLifecycleOwner: LifecycleOwner = ImmortalLifecycleOwner,
    crossinline block: suspend CoroutineScope.(D) -> Unit
) where D : Dialog, D : LifecycleOwner {
    val callback = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            cancel()
        }
    }
    parentLifecycleOwner.lifecycle.addObserver(callback)
    coroutineScope.launch {
        try {
            block(this@launchWithShow)
        } finally {
            parentLifecycleOwner.lifecycle.removeObserver(callback)
            dismiss()
        }
    }
    show()
}

suspend inline fun <D : Dialog, T> D.showWithContext(
    context: CoroutineContext = EmptyCoroutineContext,
    crossinline block: suspend CoroutineScope.(D) -> T
): T = coroutineScope {
    show()
    val cancelSelfJob = onCancellation {
        if (isShowing) {
            cancel()
        }
    }
    return@coroutineScope try {
        withContext(currentCoroutineContext() + context) {
            block(this@showWithContext)
        }
    } finally {
        dismiss()
        cancelSelfJob.cancel()
    }
}

suspend fun Dialog.showWithAwaitDismiss() = suspendCancellableCoroutine { continuation ->
    setOnDismissListener {
        continuation.resume(Unit)
    }
    show()
    continuation.invokeOnCancellation {
        cancel()
    }
}
