package com.chenfei.library.util.kotlin

fun <T> Collection<T>?.isNullOrEmpty() = this == null || isEmpty()
fun <T> Array<out T>?.isNullOrEmpty() = this == null || isEmpty()
fun ByteArray?.isNullOrEmpty() = this == null || isEmpty()
fun ShortArray?.isNullOrEmpty() = this == null || isEmpty()
fun IntArray?.isNullOrEmpty() = this == null || isEmpty()
fun LongArray?.isNullOrEmpty() = this == null || isEmpty()
fun FloatArray?.isNullOrEmpty() = this == null || isEmpty()
fun DoubleArray?.isNullOrEmpty() = this == null || isEmpty()
fun BooleanArray?.isNullOrEmpty() = this == null || isEmpty()
fun CharArray?.isNullOrEmpty() = this == null || isEmpty()

fun <T> List<T>.toArrayList(): ArrayList<T> = this as? ArrayList ?: ArrayList(this)

inline fun <T, reified R> Collection<T>.mapToArray(transform: (T) -> R): Array<R> {
    val output = arrayOfNulls<R>(size)
    this.forEachIndexed { index, item ->
        output[index] = transform(item)
    }
    @Suppress("UNCHECKED_CAST")
    return output as Array<R>
}

inline fun <T, reified R> Array<T>.mapToArray(transform: (T) -> R): Array<R> {
    val output = arrayOfNulls<R>(size)
    this.forEachIndexed { index, item ->
        output[index] = transform(item)
    }
    @Suppress("UNCHECKED_CAST")
    return output as Array<R>
}
