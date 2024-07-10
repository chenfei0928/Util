package io.github.chenfei0928.collection

import android.util.SparseArray
import android.util.SparseIntArray
import androidx.collection.SparseArrayCompat
import androidx.core.util.containsKey

inline operator fun <T> SparseArrayCompat<T>.contains(value: T): Boolean {
    return containsValue(value)
}

inline fun <T> SparseArray<T>.getOrPut(key: Int, defaultValue: (key: Int) -> T): T {
    val value = get(key)
    if (value != null) {
        return value
    }
    val newValue = defaultValue(key)
    put(key, newValue)
    return newValue
}

fun <T> SparseArray<T>.keys(): IntArray {
    val array = IntArray(size())
    for (i in array.indices) {
        array[i] = keyAt(i)
    }
    return array
}

inline fun SparseIntArray.getOrPut(key: Int, defaultValue: (key: Int) -> Int): Int {
    return if (containsKey(key)) {
        get(key)
    } else {
        val newValue = defaultValue(key)
        put(key, newValue)
        newValue
    }
}
