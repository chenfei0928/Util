package io.github.chenfei0928.json

import org.json.JSONStringer

/**
 * @author chenfei()
 * @date 2022-07-06 18:57
 */
inline fun JSONStringer.put(name: String, value: String?): JSONStringer = key(name).value(value)
inline fun JSONStringer.put(name: String, value: Int): JSONStringer = key(name).value(value)
inline fun JSONStringer.put(name: String, value: Long): JSONStringer = key(name).value(value)
inline fun JSONStringer.put(name: String, value: Double): JSONStringer = key(name).value(value)
inline fun JSONStringer.put(name: String, value: Boolean): JSONStringer = key(name).value(value)

inline fun JSONStringer.newJsonObject(
    name: String, block: JSONStringer.() -> Unit
): JSONStringer = apply {
    key(name)
    `object`()
    block(this)
    endObject()
}

inline fun JSONStringer.newJsonArray(
    name: String, block: JSONStringer.() -> Unit
): JSONStringer = apply {
    key(name)
    array()
    block(this)
    endArray()
}
