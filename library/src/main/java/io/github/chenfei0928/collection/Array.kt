package io.github.chenfei0928.collection

inline fun <T> Array<T?>.getOrPut(index: Int, defaultValue: (Int) -> T): T {
    val value = get(index)
    if (value != null) {
        return value
    }
    val newValue = defaultValue(index)
    set(index, newValue)
    return newValue
}

inline fun <reified T> Array<T?>.filterNotNull(): Array<T> {
    if (isEmpty()) {
        return arrayOf()
    }
    val count = count { it != null }
    if (count == size) {
        @Suppress("UNCHECKED_CAST")
        return this as Array<T>
    }
    if (count == 0) {
        return arrayOf()
    }
    val array = arrayOfNulls<T>(count)
    var i = 0
    forEach {
        if (it != null) {
            array[i] = it
            i++
        }
    }
    @Suppress("UNCHECKED_CAST")
    return array as Array<T>
}
