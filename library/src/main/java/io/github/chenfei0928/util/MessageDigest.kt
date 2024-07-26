package io.github.chenfei0928.util

import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Date
import java.util.Locale

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

/**
 * [Arrays.deepEquals]
 */
fun Any?.deepEquals(b: Any?): Boolean {
    return when {
        this === b -> true
        this == null || b == null -> false
        this is Array<*> && b is Array<*> -> this.contentDeepEquals(b)
        this is ByteArray && b is ByteArray -> this.contentEquals(b)
        this is ShortArray && b is ShortArray -> this.contentEquals(b)
        this is IntArray && b is IntArray -> this.contentEquals(b)
        this is LongArray && b is LongArray -> this.contentEquals(b)
        this is CharArray && b is CharArray -> this.contentEquals(b)
        this is FloatArray && b is FloatArray -> this.contentEquals(b)
        this is DoubleArray && b is DoubleArray -> this.contentEquals(b)
        this is BooleanArray && b is BooleanArray -> this.contentEquals(b)
        else -> this == b
    }
}
