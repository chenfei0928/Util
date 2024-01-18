package io.github.chenfei0928.concurrent.lazy

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * @author chenfei()
 * @date 2022-08-22 15:10
 */
class SuspendGetter<T>(
    private val context: CoroutineContext = EmptyCoroutineContext,
    private val initializer: suspend CoroutineScope.() -> T
) {
    private val atomicReference = AtomicReference<IGetter<T>>(
        IGetter.DeferredGetter(context, initializer)
    )

    fun reset() {
        atomicReference.set(IGetter.DeferredGetter(context, initializer))
    }

    fun isInitialized(): Boolean =
        atomicReference.get() is IGetter.Object

    tailrec suspend fun get(): T = when (val mayBeDeferred = atomicReference.get()) {
        is IGetter.Object<T> -> mayBeDeferred.obj
        is IGetter.DeferredGetter<T> -> {
            val next = try {
                mayBeDeferred.invoke()
            } catch (e: Throwable) {
                reset()
                throw e
            }
            if (atomicReference.compareAndSet(mayBeDeferred, IGetter.Object(next))) {
                next
            } else {
                get()
            }
        }
    }

    private sealed interface IGetter<T> {
        class DeferredGetter<T>(
            context: CoroutineContext,
            initializer: suspend CoroutineScope.() -> T
        ) : IGetter<T>, suspend () -> T {
            private val deferred = CoroutineScope(context).async(
                start = CoroutineStart.LAZY,
                block = initializer
            )

            override suspend fun invoke(): T {
                return deferred.await()
            }
        }

        class Object<T>(
            val obj: T
        ) : IGetter<T>
    }
}
