package io.github.chenfei0928.util

import io.github.chenfei0928.Env
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * @author chenf()
 * @date 2023-09-14 15:39
 */
class RuntimeExecProperty<R : Any>(
    private val command: String
) : ReadOnlyProperty<Any, R> {
    private val valueRef = AtomicReference<R>()

    override fun getValue(thisRef: Any, property: KProperty<*>): R {
        return valueRef.get() ?: valueRef.updateAndGet {
            it ?: run {
                val l = System.currentTimeMillis()
                val result = Runtime.getRuntime().exec(command).use {
                    inputStream.use { it.reader().readText().trim() }
                }
                Env.logger.quiet("VCS ${property.name}: $result, time cost ${System.currentTimeMillis() - l} ms.")
                when (property.returnType.classifier) {
                    String::class -> result as R
                    Int::class -> result.toInt() as R
                    else -> throw IllegalArgumentException("不支持的返回值类型 ${property.returnType}")
                }
            }
        }
    }

    private inline fun <T> Process.use(
        block: Process.() -> T
    ): T = try {
        block()
    } finally {
        destroy()
    }
}
