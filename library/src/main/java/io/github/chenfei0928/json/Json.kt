package io.github.chenfei0928.json

import androidx.annotation.ReturnThis
import org.json.JSONArray
import org.json.JSONObject

@ReturnThis
operator fun JSONObject.set(name: String, value: Any?): JSONObject {
    putOpt(name, value)
    return this
}

@ReturnThis
inline fun JSONObject.newJsonObject(name: String, action: JSONObject.() -> Unit): JSONObject {
    val jsonObject = JSONObject()
    action(jsonObject)
    this[name] = jsonObject
    return this
}

@ReturnThis
inline fun JSONObject.newJsonArray(name: String, action: JSONArray.() -> Unit): JSONObject {
    val jsonArray = JSONArray()
    action(jsonArray)
    this[name] = jsonArray
    return this
}

@ReturnThis
inline fun JSONArray.newJsonObject(action: JSONObject.() -> Unit): JSONArray {
    val jsonObject = JSONObject()
    action(jsonObject)
    put(jsonObject)
    return this
}

@ReturnThis
inline fun JSONArray.newJsonArray(action: JSONArray.() -> Unit): JSONArray {
    val jsonArray = JSONArray()
    action(jsonArray)
    put(jsonArray)
    return this
}

inline operator fun JSONObject.contains(key: String) = has(key)
