package io.github.chenfei0928.reflect

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KType

@Suppress("UNCHECKED_CAST")
fun <T> Class<T>.arrayClass(): Class<Array<T>> =
    java.lang.reflect.Array.newInstance(this, 0).javaClass as Class<Array<T>>

fun Class<*>.isSubclassOf(base: Class<*>) =
    base.isAssignableFrom(this)

val KType.argument0TypeClass: KClass<*>
    get() = arguments[0].type?.classifier as KClass<*>

@Throws(
    IllegalAccessException::class, IllegalArgumentException::class, InvocationTargetException::class
)
inline fun Method.safeInvoke(target: Any?, args: Array<Any?>?): Any? {
    return if (args == null) {
        this.invoke(target)
    } else {
        @Suppress("SpreadOperator")
        this.invoke(target, *args)
    }
}
