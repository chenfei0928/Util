package com.chenfei.library.util.kotlin

import org.json.JSONArray
import org.json.JSONObject

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
inline operator fun <reified T> JSONObject.get(key: String): T {
    return when (T::class) {
        String::class -> optString(key) as T
        Byte::class -> optInt(key).toByte() as T
        Short::class -> optInt(key).toShort() as T
        Int::class -> optInt(key) as T
        Long::class -> optLong(key) as T
        Float::class -> optDouble(key).toFloat() as T
        Double::class -> optDouble(key) as T
        Boolean::class -> optBoolean(key) as T
        JSONObject::class -> optJSONObject(key) as T
        JSONArray::class -> optJSONArray(key) as T
        else -> opt(key) as T
    }
}

operator fun JSONObject.set(key: String, value: Any): JSONObject {
    putOpt(key, value)
    return this
}

inline fun JSONObject.newJsonObject(key: String, action: JSONObject.() -> Unit): JSONObject {
    val jsonObject = JSONObject()
    action(jsonObject)
    this[key] = jsonObject
    return this
}

inline fun JSONObject.newJsonArray(key: String, action: JSONArray.() -> Unit): JSONObject {
    val jsonArray = JSONArray()
    action(jsonArray)
    this[key] = jsonArray
    return this
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
inline operator fun <reified T> JSONArray.get(index: Int): T {
    return when (T::class) {
        String::class -> optString(index) as T
        Byte::class -> optInt(index).toByte() as T
        Short::class -> optInt(index).toShort() as T
        Int::class -> optInt(index) as T
        Long::class -> optLong(index) as T
        Float::class -> optDouble(index).toFloat() as T
        Double::class -> optDouble(index) as T
        Boolean::class -> optBoolean(index) as T
        JSONObject::class -> optJSONObject(index) as T
        JSONArray::class -> optJSONArray(index) as T
        else -> opt(index) as T
    }
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

fun Map<String, Any>.toJson(): JSONObject = JSONObject(this)
fun Collection<Any>.toJson(): JSONArray = JSONArray(this)

operator fun JSONObject.contains(key: String) = has(key)
