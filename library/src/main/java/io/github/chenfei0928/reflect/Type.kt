package io.github.chenfei0928.reflect

import io.github.chenfei0928.lang.arrayClass
import io.github.chenfei0928.lang.contains
import java.lang.reflect.GenericArrayType
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType

inline fun <reified T> jTypeOf(): Type = lazyJTypeOf<T>()()
inline fun <reified T> lazyJTypeOf(): LazyTypeToken<T> = object : LazyTypeToken<T>() {}

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
