package io.github.chenfei0928.reflect

import android.os.Build
import io.github.chenfei0928.annotation.KeepAllowObfuscation
import io.github.chenfei0928.annotation.WithChildInObfuscation
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * 像 [com.google.gson.reflect.TypeToken] 或 [com.google.common.reflect.TypeToken]
 * 通过构建子类并反射获取并提供 [Type] 类型信息，区别是当前类通过自带双重校验线程锁方式懒加载来推迟泛型信息获取，
 * 而非实例构建时直接获取，以用于在不一定必须需要其类型信息的场景中优化性能。
 *
 * 此类允许被二次继承，但要求传入 [T] 的 [type] 类型。
 * 或由最终子类来实现 [T] 的具体类型，且最终子类实现父类泛型列表中 [T] 必须在首位或传入其下标作为 [actualTypeIndex]。
 *
 * @author chenf()
 * @date 2025-01-15 15:10
 */
@WithChildInObfuscation
@KeepAllowObfuscation
open class LazyTypeToken<T> : () -> Type, Lazy<Type> {
    private val actualTypeIndex: Int

    @Volatile
    private var type: Type? = null

    protected constructor(actualTypeIndex: Int) {
        this.actualTypeIndex = actualTypeIndex
    }

    constructor(type: Type) {
        actualTypeIndex = 0
        this.type = type
    }

    final override fun invoke(): Type = this.type ?: synchronized(this) {
        this.type ?: run {
            val type = (javaClass.genericSuperclass as ParameterizedType)
                .actualTypeArguments[0]
            this.type = type
            type
        }
    }

    final override val value: Type
        get() = invoke()

    final override fun isInitialized(): Boolean =
        type != null

    override fun toString(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            value.typeName
        } else if (value is Class<*>) {
            (value as Class<*>).name
        } else {
            value.toString()
        }
    }

    companion object {
        inline operator fun <reified T> invoke() = object : LazyTypeToken<T>(0) {}
    }
}
