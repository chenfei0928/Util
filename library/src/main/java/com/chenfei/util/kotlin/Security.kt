/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-16 15:56
 */
package com.chenfei.util.kotlin

import java.security.MessageDigest

private val hexDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

fun MessageDigest.digestToHex(): String {
    return digest().toHexString(false)
}

fun ByteArray.toHexString(needSeparator: Boolean): String {
    val buffer = CharArray(if (needSeparator) {
        size * 3 - 1
    } else {
        size * 2
    })
    var i = 0
    this.forEachIndexed { index, byte ->
        val int = byte.toInt()
        buffer[i++] = hexDigits[int ushr 4 and 15]   // 高4bit内存位
        buffer[i++] = hexDigits[int and 15]          // 低4bit内存位
        // 如果需要追加分隔符且不是最后一位，追加分隔符
        if (needSeparator && index != lastIndex) {
            buffer[i++] = ':'
        }
    }
    return String(buffer)
}
