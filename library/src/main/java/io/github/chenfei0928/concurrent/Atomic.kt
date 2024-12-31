/**
 * @author chenf()
 * @date 2024-07-30 17:52
 */
package io.github.chenfei0928.concurrent

import android.os.Build
import java.util.concurrent.atomic.AtomicReference

inline fun <T> AtomicReference<T>.updateAndGetCompat(
    crossinline updateFunction: (T?) -> T
): T {
    // 此处需要让低版本在前面，否则可能会让kotlin编译器在对 updateFunction 内代码进行api版本检查错误的报错
    // 如 io.github.chenfei0928.app.ProcessUtil.getProcessName
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        var prev: T?
        var next: T
        do {
            prev = get()
            next = prev ?: updateFunction(prev)
        } while (!compareAndSet(prev, next))
        next
    } else {
        updateAndGet { updateFunction(it) }
    }
}
