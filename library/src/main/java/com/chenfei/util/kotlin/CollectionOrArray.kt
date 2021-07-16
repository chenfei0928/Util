/**
 * 此文件存放List与Array通用的逻辑
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-11-20 17:51
 */
package com.chenfei.util.kotlin

inline fun <T, reified R> Collection<T>.mapToArray(transform: (T) -> R): Array<R> {
    val output = arrayOfNulls<R>(size)
    this.forEachIndexed { index, item ->
        output[index] = transform(item)
    }
    @Suppress("UNCHECKED_CAST") return output as Array<R>
}

inline fun <T, reified R> List<T>.mapToArray(transform: (T) -> R): Array<R> {
    return Array(size) {
        transform(this[it])
    }
}

inline fun <T, reified R> Array<T>.mapToArray(transform: (T) -> R): Array<R> {
    return Array(size) {
        transform(this[it])
    }
}

inline fun <T> Collection<T>.mapToIntArray(transform: (T) -> Int): IntArray {
    val output = IntArray(size)
    this.forEachIndexed { index, item ->
        output[index] = transform(item)
    }
    return output
}
