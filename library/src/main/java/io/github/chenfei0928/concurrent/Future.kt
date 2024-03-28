package io.github.chenfei0928.concurrent

import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.Future
import kotlin.coroutines.resume

/**
 * @author chenf()
 * @date 2024-03-21 16:52
 */
suspend fun <T> Future<T>.await(): T {
    if (this is ListenableFuture) {
        return this.await()
    }
    val coroutineScope = CoroutineScope(currentCoroutineContext())
    return withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            coroutineScope.launch(Dispatchers.IO) {
                while (!isDone && !isCancelled && isActive) {
                    delay(1L)
                }
                if (isActive && isDone) {
                    continuation.resume(get())
                }
            }
            continuation.invokeOnCancellation {
                cancel(true)
            }
        }
    }
}

suspend fun <T> ListenableFuture<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addListener({
        continuation.resume(get())
    }, Dispatchers.Default.asExecutor())
    continuation.invokeOnCancellation {
        cancel(true)
    }
}
