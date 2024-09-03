package io.github.chenfei0928.concurrent

import androidx.concurrent.futures.await
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.Future
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

/**
 * @author chenf()
 * @date 2024-03-21 16:52
 */
suspend fun <T> Future<T>.awaitKt(
    waitOn: CoroutineContext = Dispatchers.IO
): T {
    if (this is ListenableFuture) {
        return this.await()
    }
    val coroutineScope = CoroutineScope(currentCoroutineContext())
    return withContext(waitOn) {
        suspendCancellableCoroutine { continuation ->
            coroutineScope.launch(waitOn) {
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
