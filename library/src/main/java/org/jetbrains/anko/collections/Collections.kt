@file:JvmName("CollectionsCf0928Util")

package org.jetbrains.anko.collections

/**
 * Iterate the receiver [List] backwards using an index.
 *
 * @f an action to invoke on each list element (index, element).
 */
inline fun <T> List<T>.forEachReversedWithIndex(f: (index: Int, e: T) -> Unit) {
    var i = size - 1
    while (i >= 0) {
        f(i, get(i))
        i--
    }
}
