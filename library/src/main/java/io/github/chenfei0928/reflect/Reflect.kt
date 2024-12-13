package io.github.chenfei0928.reflect

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

@Suppress("UNCHECKED_CAST")
fun <T> Class<T>.arrayClass(): Class<Array<T>> =
    java.lang.reflect.Array.newInstance(this, 0).javaClass as Class<Array<T>>

inline fun Class<*>.isSubclassOf(base: Class<*>) =
    base.isAssignableFrom(this)

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
