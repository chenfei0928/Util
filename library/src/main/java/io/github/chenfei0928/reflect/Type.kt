package io.github.chenfei0928.reflect

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

inline fun <reified T> typeOf(): Type = object : TypeToken<T>() {}.type
