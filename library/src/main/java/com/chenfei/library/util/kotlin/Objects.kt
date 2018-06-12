package com.chenfei.library.util.kotlin

import java.text.SimpleDateFormat
import java.util.*

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

fun Long.toDate() = Date(this)
fun Long.formatDateTime(pattern: String): String =
        this.toDate().toString(pattern)

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
fun Date.toString(pattern: String, locale: Locale = Locale.CHINA): String =
        SimpleDateFormat(pattern, locale).format(this)
