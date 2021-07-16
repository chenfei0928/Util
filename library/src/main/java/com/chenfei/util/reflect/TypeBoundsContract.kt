package com.chenfei.util.reflect

import java.lang.reflect.ParameterizedType
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType

/**
 * 参数化类型范型约束契约，为子类所实现的父类约束类或范围
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-01-11 11:15
 */
data class TypeBoundsContract<T>
constructor(
    private val _clazz: Class<T>? = null,
    private val childClassTypeParameter: TypeVariable<out Class<T>>? = null,
    private val childClassWildcardType: WildcardType? = null,
) {
    val clazz: Class<T>
        get() = _clazz ?: childClassTypeParameter?.run {
            bounds
                .filterIsInstance<Class<T>>()
                .firstOrNull()
        } ?: childClassWildcardType?.run {
            upperBounds
                .filterIsInstance<Class<T>>()
                .firstOrNull()
        } ?: Any::class.java as Class<T>

    fun isInstance(obj: Any?): Boolean {
        if (_clazz != null) {
            return _clazz.isInstance(obj)
        }
        val bounds =
            childClassTypeParameter?.bounds ?: childClassWildcardType?.upperBounds ?: return true
        bounds.forEach { bound ->
            when (bound) {
                is Class<*> -> {
                    // 通过范型约束了无范型类型作为约束条件
                    if (!bound.isInstance(obj)) {
                        return false
                    }
                }
                is ParameterizedType -> {
                    // 通过范型约束了范型类型作为约束条件
                    val rawType = bound.rawType
                    if (rawType is Class<*> && !rawType.isInstance(obj)) {
                        // 只判断其直接类型是否是instance
                        return false
                    }
                }
            }
        }
        return true
    }
}

fun <R> Iterable<*>.filterIsInstance(klass: TypeBoundsContract<R>): List<R> {
    return filterIsInstanceTo(ArrayList<R>(), klass)
}

fun <C : MutableCollection<in R>, R> Iterable<*>.filterIsInstanceTo(
    destination: C, klass: TypeBoundsContract<R>
): C {
    @Suppress("UNCHECKED_CAST") for (element in this) {
        if (klass.isInstance(element)) {
            destination.add(element as R)
        }
    }
    return destination
}
