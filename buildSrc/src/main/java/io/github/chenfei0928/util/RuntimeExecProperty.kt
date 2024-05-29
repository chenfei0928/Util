package io.github.chenfei0928.util

import io.github.chenfei0928.Env
import java.io.File
import java.util.Enumeration
import java.util.StringTokenizer
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * @author chenf()
 * @date 2023-09-14 15:39
 */
class RuntimeExecProperty<R : Any>
constructor(
    private val cmdarray: Array<String>,
    private val envp: Array<String>? = null,
    private val dir: File? = null,
) : ReadOnlyProperty<Any?, R> {
    private val valueRef = AtomicReference<R>()

    constructor(
        command: String,
        envp: Array<String>? = null,
        dir: File? = null,
    ) : this(StringTokenizer(command).tokens(), envp, dir)

    override fun getValue(thisRef: Any?, property: KProperty<*>): R {
        return valueRef.get() ?: valueRef.updateAndGet {
            it ?: run {
                val l = System.currentTimeMillis()
                val result = Runtime.getRuntime().exec(
                    cmdarray, envp, dir
                ).inputReader().use {
                    it.readText().trim()
                }
                Env.logger.quiet("VCS ${property.name}: $result, time cost ${System.currentTimeMillis() - l} ms.")
                when (property.returnType.classifier) {
                    String::class -> result as R
                    Int::class -> result.toInt() as R
                    Long::class -> result.toLong() as R
                    Boolean::class -> result.toBoolean() as R
                    Float::class -> result.toFloat() as R
                    Double::class -> result.toDouble() as R
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

    companion object {
        fun <E> Enumeration<E>.elements() = asIterator().run {
            val list = ArrayList<E>()
            while (hasNext()) {
                list.add(next())
            }
            list
        }

        fun StringTokenizer.tokens() =
            Array(countTokens()) { nextToken() }
    }
}
