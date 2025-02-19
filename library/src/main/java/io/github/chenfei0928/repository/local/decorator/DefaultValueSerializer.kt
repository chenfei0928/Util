package io.github.chenfei0928.repository.local.decorator

import io.github.chenfei0928.repository.local.LocalSerializer

/**
 * 修改默认值的序列化
 *
 * @author chenfei()
 * @date 2022-07-13 18:54
 */
class DefaultValueSerializer<T : Any>
private constructor(
    private val serializer: LocalSerializer<T>,
    override val defaultValue: T
) : LocalSerializer<T> by serializer {

    override fun toString(): String {
        return "DefaultValueSerializer(serializer=$serializer)"
    }

    companion object {
        /**
         * 修改默认值的序列化
         */
        fun <T : Any> LocalSerializer<T>.defaultValue(defaultValue: T): LocalSerializer<T> =
            DefaultValueSerializer(this, defaultValue)
    }
}
