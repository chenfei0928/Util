package io.github.chenfei0928.preference.sp

import androidx.preference.PreferenceManager
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.convert.DefaultValueSpDelete
import io.github.chenfei0928.content.sp.saver.convert.SpConvertSaver
import io.github.chenfei0928.content.sp.saver.getPropertySpKeyName
import io.github.chenfei0928.preference.BasePreferenceDataStore
import io.github.chenfei0928.preference.FieldAccessor
import kotlin.reflect.KMutableProperty0

/**
 * 用于对spSaver的扩展，以优化[PreferenceManager]的访问
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-04-24 10:42
 */
@Suppress("TooManyFunctions")
class SpSaverPreferenceDataStore<SpSaver : AbsSpSaver>(
    private val saver: SpSaver,
    fieldAccessor: FieldAccessor<SpSaver> = FieldAccessor.Impl(),
) : BasePreferenceDataStore<SpSaver>(fieldAccessor) {

    constructor(
        saver: SpSaver,
        fieldAccessor: FieldAccessor<SpSaver> = FieldAccessor.Impl(),
        vararg properties: KMutableProperty0<*>
    ) : this(saver, fieldAccessor) {
        properties.forEach { property(saver.findMutablePropertyField(it)) }
    }

    override fun <V> FieldAccessor.Field<SpSaver, V>.set(value: V) {
        set(saver, value)
    }

    override fun <V> FieldAccessor.Field<SpSaver, V>.get(): V =
        get(saver)

    companion object {
        private fun <SpSaver : AbsSpSaver> SpSaver.findMutablePropertyField(
            property0: KMutableProperty0<*>
        ): FieldAccessor.Field<SpSaver, *> {
            val name = getPropertySpKeyName(property0)
            var delegate: Any? = property0.getDelegate()
            var defaultValue: Any? = null
            while (true) {
                when (delegate) {
                    is DefaultValueSpDelete<*> -> {
                        // 默认值一般是作为最后的装饰，会最先触发，其默认值要保存下来
                        defaultValue = delegate.defaultValue
                        delegate = delegate.saver
                    }
                    is SpConvertSaver<*, *> -> {
                        @Suppress("UNCHECKED_CAST")
                        delegate as SpConvertSaver<Any, Any?>
                        // 转换器装饰器，将其解装饰，并处理默认值
                        defaultValue = delegate.onSave(defaultValue)
                        delegate = delegate.saver
                    }
                    is AbsSpSaver.AbsSpDelegate<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        val localDelegate: AbsSpSaver.AbsSpDelegate<Any?> =
                            delegate as AbsSpSaver.AbsSpDelegate<Any?>
                        return SpSaverField(name, localDelegate, property0, defaultValue)
                    }
                    else -> throw IllegalArgumentException("不支持的委托类型: ${delegate?.javaClass} $delegate")
                }
            }
        }

        private class SpSaverField<SpSaver : AbsSpSaver, V>(
            override val name: String,
            private val localDelegate: AbsSpSaver.AbsSpDelegate<V?>,
            private val property0: KMutableProperty0<*>,
            private val defaultValue: V?,
        ) : FieldAccessor.Field<SpSaver, V?> {
            override fun get(data: SpSaver): V? {
                return localDelegate.getValue(data, property0) ?: defaultValue
            }

            override fun set(data: SpSaver, value: V?): SpSaver {
                localDelegate.setValue(data, property0, value)
                return data
            }
        }
    }
}
