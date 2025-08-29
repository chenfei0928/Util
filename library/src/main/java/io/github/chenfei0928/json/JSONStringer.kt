package io.github.chenfei0928.json

import org.json.JSONStringer

/**
 * @author chenfei()
 * @date 2022-07-06 18:57
 */
fun JSONStringer.put(name: String, value: String?): JSONStringer = key(name).value(value)
fun JSONStringer.put(name: String, value: Int): JSONStringer = key(name).value(value)
fun JSONStringer.put(name: String, value: Long): JSONStringer = key(name).value(value)
fun JSONStringer.put(name: String, value: Double): JSONStringer = key(name).value(value)
fun JSONStringer.put(name: String, value: Boolean): JSONStringer = key(name).value(value)

fun JSONStringer.put(name: String, values: List<String?>?): JSONStringer =
    if (values == null) key(name).value(null) else
        internalNewJsonArray(name) { values.forEach { value(it) } }

fun JSONStringer.put(name: String, values: Array<String?>?): JSONStringer =
    if (values == null) key(name).value(null) else
        internalNewJsonArray(name) { values.forEach { value(it) } }

fun JSONStringer.put(name: String, values: IntArray?): JSONStringer =
    if (values == null) key(name).value(null) else
        internalNewJsonArray(name) { values.forEach { value(it) } }

fun JSONStringer.put(name: String, values: LongArray?): JSONStringer =
    if (values == null) key(name).value(null) else
        internalNewJsonArray(name) { values.forEach { value(it) } }

fun JSONStringer.put(name: String, values: DoubleArray?): JSONStringer =
    if (values == null) key(name).value(null) else
        internalNewJsonArray(name) { values.forEach { value(it) } }

fun JSONStringer.put(name: String, values: BooleanArray?): JSONStringer =
    if (values == null) key(name).value(null) else
        internalNewJsonArray(name) { values.forEach { value(it) } }

inline fun <T> JSONStringer.newJsonObject(
    name: String, value: T, block: JSONStringer.(T) -> Unit
): JSONStringer = apply {
    key(name)
    `object`()
    block(this, value)
    endObject()
}

inline fun JSONStringer.internalNewJsonArray(
    name: String, block: JSONStringer.() -> Unit
): JSONStringer = apply {
    key(name)
    array()
    block(this)
    endArray()
}

inline fun <T> JSONStringer.newJsonArray(
    name: String, iterable: Iterable<T>, block: JSONStringer.(T) -> Unit
): JSONStringer = internalNewJsonArray(name) {
    iterable.forEach { block(it) }
}

inline fun <T> JSONStringer.newJsonArray(
    name: String, array: Array<T>, block: JSONStringer.(T) -> Unit
): JSONStringer = internalNewJsonArray(name) {
    array.forEach { block(it) }
}

inline fun <T> JSONStringer.newJsonObjectArray(
    name: String, iterable: Iterable<T>, block: JSONStringer.(T) -> Unit
): JSONStringer = newJsonArray(name, iterable) {
    `object`()
    block(it)
    endObject()
}

inline fun <T> JSONStringer.writeArray(
    iterable: Iterable<T>, block: JSONStringer.(T) -> Unit
): JSONStringer = apply {
    array()
    iterable.forEach { block(it) }
    endArray()
}
