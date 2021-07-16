package com.chenfei.util.kotlin

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

inline fun <reified T> typeOf(): Type = object : TypeToken<T>() {}.type
