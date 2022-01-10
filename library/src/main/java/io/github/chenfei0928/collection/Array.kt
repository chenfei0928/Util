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

inline fun <reified T> arrayOfNotNull(vararg vars: T?): Array<T> {
    if (vars.isEmpty()) {
        return arrayOf()
    }
    val count = vars.count { it != null }
    if (count == vars.size) {
        return vars as Array<T>
    }
    if (count == 0) {
        return arrayOf()
    }
    val array = arrayOfNulls<T>(count)
    var i = 0
    vars.forEach {
        if (it != null) {
            array[i] = it
            i++
        }
    }
    @Suppress("UNCHECKED_CAST")
    return array as Array<T>
}
