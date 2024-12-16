package io.github.chenfei0928.reflect

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * 使用 Gson 中的 [TypeToken] 获取类型
 *
 * 使用前确认引入了[Gson](https://github.com/google/gson)依赖
 *
 * Gson 2.11.0 及其以上时，如果泛型 [T] 的类型不是最终类型，
 * 其自身依然有泛型约束的（包含其内元素的泛型）这可能会报错[IllegalArgumentException]：
 * [TypeToken.isCapturingTypeVariablesForbidden]
 *
 * 可通过调用 `System.setProperty("gson.allowCapturingTypeVariables", "true")` 来解决
 */
inline fun <reified T> jTypeOf(): Type = object : TypeToken<T>() {}.type
