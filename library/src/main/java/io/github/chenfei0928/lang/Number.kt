package io.github.chenfei0928.lang

/**
 * @author chenf()
 * @date 2024-08-16 17:43
 */
operator fun Int.contains(other: Int): Boolean {
    return other and this == other
}

operator fun Long.contains(other: Long): Boolean {
    return other and this == other
}

infix fun Int.anyIn(other: Int): Boolean {
    return other and this != 0
}

fun ByteArray.toLong(): Long {
    var count = 0L
    forEach {
        count = count shl Byte.SIZE_BITS
        count = count or it.toLong()
    }
    return count
}

fun Long.toByteArray(): ByteArray {
    return byteArrayOf(
        (this shr 56).toByte(),
        (this shr 48).toByte(),
        (this shr 40).toByte(),
        (this shr 32).toByte(),
        (this shr 24).toByte(),
        (this shr 16).toByte(),
        (this shr 8).toByte(),
        this.toByte()
    )
}

fun Int.toByteArray(): ByteArray {
    return byteArrayOf(
        (this shr 24).toByte(),
        (this shr 16).toByte(),
        (this shr 8).toByte(),
        this.toByte()
    )
}
