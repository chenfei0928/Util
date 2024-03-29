package io.github.chenfei0928.util

import org.json.JSONArray
import org.json.JSONObject

operator fun JSONObject.set(name: String, value: Any?): JSONObject {
    putOpt(name, value)
    return this
}

inline fun JSONObject.newJsonObject(name: String, action: JSONObject.() -> Unit): JSONObject {
    val jsonObject = JSONObject()
    action(jsonObject)
    this[name] = jsonObject
    return this
}

inline fun JSONObject.newJsonArray(name: String, action: JSONArray.() -> Unit): JSONObject {
    val jsonArray = JSONArray()
    action(jsonArray)
    this[name] = jsonArray
    return this
}

inline fun JSONArray.newJsonObject(action: JSONObject.() -> Unit): JSONArray {
    val jsonObject = JSONObject()
    action(jsonObject)
    put(jsonObject)
    return this
}

inline fun JSONArray.newJsonArray(action: JSONArray.() -> Unit): JSONArray {
    val jsonArray = JSONArray()
    action(jsonArray)
    put(jsonArray)
    return this
}

inline operator fun JSONObject.contains(key: String) = has(key)
