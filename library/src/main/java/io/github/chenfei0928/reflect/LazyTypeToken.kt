package io.github.chenfei0928.reflect

import io.github.chenfei0928.annotation.KeepAllowObfuscation
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * 像 [com.google.gson.reflect.TypeToken] 或 [com.google.common.reflect.TypeToken]
 * 通过构建子类并反射获取并提供 [Type] 类型信息，区别是当前类通过自带双重校验线程锁方式懒加载来推迟泛型信息获取，
 * 而非实例构建时直接获取，以用于在不一定必须需要其类型信息的场景中优化性能。
 *
 * @author chenf()
 * @date 2025-01-15 15:10
 */
@KeepAllowObfuscation
abstract class LazyTypeToken<T> : () -> Type, Lazy<Type> {
    @Volatile
    private var type: Type? = null

    final override fun invoke(): Type {
        val type = type
        return if (type != null) {
            type
        } else synchronized(this) {
            val type = this.type
            if (type != null) {
                type
            } else {
                val type = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
                this.type = type
                type
            }
        }
    }

    final override val value: Type
        get() = invoke()

    override fun isInitialized(): Boolean =
        type != null
}
