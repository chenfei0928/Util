package com.chenfei.library.util.kotlin

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Reader

inline fun <reified T : Any> Gson.fromJson(json: Reader): T {
    return fromJson(json, object : TypeToken<T>() {}.type)
}

inline fun <reified T : Any> Gson.fromJson(json: String): T {
    return fromJson(json, object : TypeToken<T>() {}.type)
}
