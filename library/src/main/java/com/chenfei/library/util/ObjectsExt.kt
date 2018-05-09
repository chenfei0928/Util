@file:Suppress("NOTHING_TO_INLINE")

package com.chenfei.library.util

inline fun <T> Collection<T>?.isNullOrEmpty() = this == null || isEmpty()
inline fun <T> Array<out T>?.isNullOrEmpty() = this == null || isEmpty()
inline fun ByteArray?.isNullOrEmpty() = this == null || isEmpty()
inline fun ShortArray?.isNullOrEmpty() = this == null || isEmpty()
inline fun IntArray?.isNullOrEmpty() = this == null || isEmpty()
inline fun LongArray?.isNullOrEmpty() = this == null || isEmpty()
inline fun FloatArray?.isNullOrEmpty() = this == null || isEmpty()
inline fun DoubleArray?.isNullOrEmpty() = this == null || isEmpty()
inline fun BooleanArray?.isNullOrEmpty() = this == null || isEmpty()
inline fun CharArray?.isNullOrEmpty() = this == null || isEmpty()

inline fun <T1, T2> checkNotNull(t1: T1?, t2: T2?, action: (T1, T2) -> Unit) {
    if (t1 != null && t2 != null)
        action(t1, t2)
}

inline fun <T1, T2, T3> checkNotNull(t1: T1?, t2: T2?, t3: T3?, action: (T1, T2, T3) -> Unit) {
    if (t1 != null && t2 != null && t3 != null)
        action(t1, t2, t3)
}

inline fun <T1, T2, T3, T4> checkNotNull(t1: T1?, t2: T2?, t3: T3?, t4: T4?, action: (T1, T2, T3, T4) -> Unit) {
    if (t1 != null && t2 != null && t3 != null && t4 != null)
        action(t1, t2, t3, t4)
}

inline fun <T1, T2, T3, T4, T5> checkNotNull(t1: T1?, t2: T2?, t3: T3?, t4: T4?, t5: T5?, action: (T1, T2, T3, T4, T5) -> Unit) {
    if (t1 != null && t2 != null && t3 != null && t4 != null && t5 != null)
        action(t1, t2, t3, t4, t5)
}

inline fun <R> Process.use(block: (Process) -> R): R {
    try {
        return block(this)
    } catch (e: Throwable) {
        throw e
    } finally {
        this.destroy()
    }
}
