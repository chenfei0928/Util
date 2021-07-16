package com.chenfei.util.kotlin

import java.text.SimpleDateFormat
import java.util.*

inline fun <T1, T2, R> checkNotNull(t1: T1?, t2: T2?, action: (T1, T2) -> R): R? {
    return if (t1 != null && t2 != null) action(t1, t2)
    else null
}

inline fun <T1, T2, T3, R> checkNotNull(t1: T1?, t2: T2?, t3: T3?, action: (T1, T2, T3) -> R): R? {
    return if (t1 != null && t2 != null && t3 != null) action(t1, t2, t3)
    else null
}

inline fun <T1, T2, T3, T4, R> checkNotNull(
    t1: T1?, t2: T2?, t3: T3?, t4: T4?, action: (T1, T2, T3, T4) -> R
): R? {
    return if (t1 != null && t2 != null && t3 != null && t4 != null) action(t1, t2, t3, t4)
    else null
}

inline fun <T1, T2, T3, T4, T5, R> checkNotNull(
    t1: T1?, t2: T2?, t3: T3?, t4: T4?, t5: T5?, action: (T1, T2, T3, T4, T5) -> R
): R? {
    return if (t1 != null && t2 != null && t3 != null && t4 != null && t5 != null) action(
        t1, t2, t3, t4, t5
    )
    else null
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

operator fun Int.contains(other: Int): Boolean {
    return other and this == other
}

operator fun Long.contains(other: Long): Boolean {
    return other and this == other
}

infix fun Int.anyIn(other: Int): Boolean {
    return other and this != 0
}

fun Long.toDate() = Date(this)
fun Long.formatDateTime(pattern: String): String = this
    .toDate()
    .toString(pattern)

/**
 * 规则字符串内如果要使用模式字母，将非匹配规则内的字符以单引号括住
 * 在单引号内的模式字母不会被认为是规则，如要输出单引号本身，则使用两个单引号
 * 比如要输出：LastTime's 12:00 ，则将匹配规则写为：'LastTime''s 'hh:mm
 *
 * @receiver      日期
 * @param pattern 规则
 * @return 格式化后的日期
 */
@JvmOverloads
fun Date.toString(pattern: String, locale: Locale = Locale.US): String =
    SimpleDateFormat(pattern, locale).format(this)

fun Any?.deepEquals(b: Any?): Boolean {
    return when {
        this === b -> true
        this == null || b == null -> false
        else -> deepEquals0(this, b)
    }
}

/**
 * [Arrays.deepEquals]
 */
private fun deepEquals0(e1: Any?, e2: Any): Boolean {
    return when {
        e1 is Array<*> && e2 is Array<*> -> Arrays.deepEquals(e1, e2)
        e1 is ByteArray && e2 is ByteArray -> Arrays.equals(e1, e2)
        e1 is ShortArray && e2 is ShortArray -> Arrays.equals(e1, e2)
        e1 is IntArray && e2 is IntArray -> Arrays.equals(e1, e2)
        e1 is LongArray && e2 is LongArray -> Arrays.equals(e1, e2)
        e1 is CharArray && e2 is CharArray -> Arrays.equals(e1, e2)
        e1 is FloatArray && e2 is FloatArray -> Arrays.equals(e1, e2)
        e1 is DoubleArray && e2 is DoubleArray -> Arrays.equals(e1, e2)
        e1 is BooleanArray && e2 is BooleanArray -> Arrays.equals(e1, e2)
        else -> e1 == e2
    }
}
