/**
 * @author chenf()
 * @date 2024-07-30 17:52
 */
package io.github.chenfei0928.concurrent

import android.os.Build
import java.util.concurrent.atomic.AtomicReference

inline fun <T> AtomicReference<T>.updateAndGetCompat(
    crossinline updateFunction: (T) -> T
): T {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        updateAndGet { updateFunction(it) }
    } else {
        var prev: T?
        var next: T
        do {
            prev = get()
            next = prev ?: updateFunction(prev)
        } while (!compareAndSet(prev, next))
        next
    }
}
