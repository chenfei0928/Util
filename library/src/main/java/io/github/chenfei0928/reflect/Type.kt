package io.github.chenfei0928.reflect

import io.github.chenfei0928.lang.arrayClass
import io.github.chenfei0928.lang.contains
import java.lang.reflect.GenericArrayType
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType

inline fun <reified T> jTypeOf(): Type = LazyTypeToken<T>().getType()

inline fun <reified T> Type.jvmErasureClassOrNull(): Class<out T>? =
    jvmErasureClassOrNull(T::class.java)

/**
 * Must class or null
 * [kotlin.reflect.jvm.jvmErasure]
 */
@Suppress("UNCHECKED_CAST")
fun <T> Type.jvmErasureClassOrNull(base: Class<T>): Class<out T>? = when (this) {
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
    is WildcardType -> upperBounds.firstOrNull { upperBound ->
        val mustClass = upperBound.jvmErasureClassOrNull(base)
        // kotlin.reflect.jvm.jvmErasure
        mustClass != null && Modifier.INTERFACE !in mustClass.modifiers && !mustClass.isAnnotation
    }
    is TypeVariable<*> -> bounds.firstOrNull { bound ->
        val mustClass = bound.jvmErasureClassOrNull(base)
        // kotlin.reflect.jvm.jvmErasure
        mustClass != null && Modifier.INTERFACE !in mustClass.modifiers && !mustClass.isAnnotation
    }
    else -> throw IllegalArgumentException("Not support type ${this.javaClass.name} $this")
} as? Class<T>
