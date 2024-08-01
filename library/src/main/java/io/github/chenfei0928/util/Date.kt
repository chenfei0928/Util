/**
 * @author chenf()
 * @date 2024-08-01 14:51
 */
package io.github.chenfei0928.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
