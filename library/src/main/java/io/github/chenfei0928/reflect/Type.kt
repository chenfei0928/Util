package io.github.chenfei0928.reflect

import io.github.chenfei0928.lang.arrayClass
import io.github.chenfei0928.lang.contains
import io.github.chenfei0928.util.DependencyChecker
import java.lang.reflect.GenericArrayType
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType

/**
 * 使用 Gson 中的 `TypeToken` 获取类型
 *
 * 使用前确认引入了[Gson](https://github.com/google/gson)依赖
 *
 * Gson 2.11.0 及其以上时，如果泛型 [T] 的类型不是最终类型，
 * 其自身依然有泛型约束的（包含其内元素的泛型）这可能会报错[IllegalArgumentException]：
 * [com.google.gson.reflect.TypeToken.isCapturingTypeVariablesForbidden]
 *
 * 可通过调用 `System.setProperty("gson.allowCapturingTypeVariables", "true")` 来解决
 */
inline fun <reified T> jTypeOf(): Type = when {
    DependencyChecker.GSON() -> object : com.google.gson.reflect.TypeToken<T>() {}.type
    DependencyChecker.GUAVA() -> object : com.google.common.reflect.TypeToken<T>() {}.type
    else -> throw IllegalArgumentException("没有引入依赖库")
}

inline fun <reified T> Type.jvmErasureClassOrNull(): Class<out T>? =
    jvmErasureClassOrNull(T::class.java) as? Class<T>

/**
 * Must class or null
 * [kotlin.reflect.jvm.jvmErasure]
 */
fun <T> Type.jvmErasureClassOrNull(base: Class<T>): Class<out T>? {
    return when (this) {
        is Class<*> -> this
        is GenericArrayType -> {
            // 获取元素的数组
            genericComponentType.jvmErasureClassOrNull(base.componentType ?: Any::class.java)
                ?.arrayClass()
        }
        is ParameterizedType -> {
            // 泛型擦除，只获取其原始类型信息，不获取其泛型
            rawType.jvmErasureClassOrNull(base)
        }
        is WildcardType -> upperBounds.firstOrNull {
            val mustClass = it.jvmErasureClassOrNull<T>(base)
            // kotlin.reflect.jvm.jvmErasure
            mustClass != null && Modifier.INTERFACE !in mustClass.modifiers && !mustClass.isAnnotation
        }
        is TypeVariable<*> -> bounds.firstOrNull {
            val mustClass = it.jvmErasureClassOrNull<T>(base)
            // kotlin.reflect.jvm.jvmErasure
            mustClass != null && Modifier.INTERFACE !in mustClass.modifiers && !mustClass.isAnnotation
        }
        else -> throw IllegalArgumentException("Not support type ${this.javaClass.name} $this")
    } as? Class<T>
}
