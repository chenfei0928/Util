package io.github.chenfei0928.reflect

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * 使用 Gson 中的 [TypeToken] 获取类型
 *
 * 使用前确认引入了[Gson](https://github.com/google/gson)依赖
 */
inline fun <reified T> typeOf(): Type = object : TypeToken<T>() {}.type
