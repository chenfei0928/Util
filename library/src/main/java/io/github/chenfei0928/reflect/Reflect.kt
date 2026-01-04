package io.github.chenfei0928.reflect

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

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

private val classIsKotlin = ConcurrentHashMap<Class<*>, Boolean>()

/**
 * 如果这个类使用Kotlin编写，返回true
 *
 * Kotlin的类都会有[Metadata]注解
 */
val Class<*>.isWriteByKotlin: Boolean
    get() = classIsKotlin.getOrPut(this) { getAnnotation(Metadata::class.java) != null }
