package io.github.chenfei0928.collection

import androidx.recyclerview.widget.SortedList

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2023-04-27 16:38
 */
inline fun <T> SortedList<T>.removeAll(predicate: (T) -> Boolean) {
    for (i in (size() - 1) downTo 0) {
        if (get(i).let(predicate)) {
            removeItemAt(i)
        }
    }
}

inline fun <T> SortedList<T>.forEach(action: (T) -> Unit) {
    for (i in 0 until size()) {
        get(i).let(action)
    }
}

inline fun <T> SortedList<T>.findFirstIndex(predicate: (T) -> Boolean): Int {
    for (i in 0 until size()) {
        if (get(i).let(predicate)) {
            return i
        }
    }
    return SortedList.INVALID_POSITION
}
