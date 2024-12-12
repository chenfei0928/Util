/**
 * 此文件存放List与Array通用的逻辑
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-11-20 17:51
 */
package io.github.chenfei0928.collection

inline fun <reified T, reified R> Collection<T>.mapToArray(
    transform: (T) -> R
): Array<R> = if (this is List<T>) {
    Array<R>(size) {
        transform(get(it))
    }
} else {
    val output = arrayOfNulls<R>(size)
    this.forEachIndexed { index, item ->
        output[index] = transform(item)
    }
    @Suppress("UNCHECKED_CAST")
    output as Array<R>
}

inline fun <T, reified R> List<T>.mapToArray(
    transform: (T) -> R
): Array<R> = Array(size) {
    transform(this[it])
}

inline fun <T, reified R> Array<T>.mapToArray(
    transform: (T) -> R
): Array<R> = Array(size) {
    transform(this[it])
}

inline fun <T> Collection<T>.mapToIntArray(
    transform: (T) -> Int
): IntArray = if (this is List<T>) {
    IntArray(size) {
        transform(get(it))
    }
} else {
    val output = IntArray(size)
    this.forEachIndexed { index, item ->
        output[index] = transform(item)
    }
    output
}

inline fun <T> List<T>.mapToIntArray(
    transform: (T) -> Int
): IntArray = IntArray(size) {
    transform(this[it])
}

inline fun <T> Array<T>.mapToIntArray(
    transform: (T) -> Int
): IntArray = IntArray(size) {
    transform(this[it])
}
