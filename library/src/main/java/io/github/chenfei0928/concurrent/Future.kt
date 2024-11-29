package io.github.chenfei0928.concurrent

import androidx.concurrent.futures.await
import com.google.common.util.concurrent.ListenableFuture
import io.github.chenfei0928.util.DependencyChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * @author chenf()
 * @date 2024-03-21 16:52
 */
suspend fun <T> Future<T>.await(
    waitOn: CoroutineContext = Dispatchers.IO
): T = if (isDone) {
    get()
} else if (DependencyChecker.guava && this is ListenableFuture) {
    if (DependencyChecker.androidXListenableFuture) {
        this.await()
    } else suspendCancellableCoroutine { continuation ->
        addListener({
            if (isCancelled) {
                continuation.cancel()
            } else try {
                continuation.resume(get())
            } catch (e: ExecutionException) {
                continuation.resumeWithException(e.cause!!)
            }
        }, Runnable::run)
        continuation.invokeOnCancellation {
            cancel(false)
        }
    }
} else {
    val coroutineScope = CoroutineScope(currentCoroutineContext())
    withContext(waitOn) {
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
                cancel(false)
            }
        }
    }
}
