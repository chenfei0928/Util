package io.github.chenfei0928.preference.sp

import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.convert.DefaultValueSpDelete
import io.github.chenfei0928.content.sp.saver.convert.SpConvertSaver
import io.github.chenfei0928.content.sp.saver.getPropertySpKeyName
import io.github.chenfei0928.preference.FieldAccessor
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.jvm.isAccessible

/**
 * @author chenf()
 * @date 2024-12-20 15:34
 */
interface SpSaverFieldAccessor<SpSaver : AbsSpSaver<SpSaver>> : FieldAccessor<SpSaver> {
    val accessDelegateName: Boolean

    fun <V> property(
        property0: KMutableProperty0<V>, vType: PreferenceType,
    ): FieldAccessor.Field<SpSaver, V>

    fun <V> propertyUseDelegate(property0: KMutableProperty0<V>): FieldAccessor.Field<SpSaver, V>

    fun <V> propertyUseDelegate(
        property: KMutableProperty<V>, delegate: AbsSpSaver.AbsSpDelegate<V>?
    ): FieldAccessor.Field<SpSaver, V>

    class Impl<SpSaver : AbsSpSaver<SpSaver>>(
        private val spSaver: SpSaver,
        override val accessDelegateName: Boolean,
    ) : FieldAccessor.Impl<SpSaver>(false), SpSaverFieldAccessor<SpSaver> {

        override fun <V> property(
            property0: KMutableProperty0<V>, vType: PreferenceType
        ): FieldAccessor.Field<SpSaver, V> = property(NativeTypeField(property0, vType))

        private class NativeTypeField<SpSaver : AbsSpSaver<SpSaver>, V>(
            private val property0: KMutableProperty0<V>,
            override val vType: PreferenceType,
        ) : FieldAccessor.Field<SpSaver, V> {
            override val name: String = property0.name

            override fun get(data: SpSaver): V = property0.get()

            override fun set(data: SpSaver, value: V): SpSaver {
                property0.set(value)
                return data
            }
        }

        override fun <V> propertyUseDelegate(property0: KMutableProperty0<V>): FieldAccessor.Field<SpSaver, V> {
            property0.isAccessible = true
            var delegate: AbsSpSaver.AbsSpDelegate<V>? =
                property0.getDelegate() as AbsSpSaver.AbsSpDelegate<V>
            return propertyUseDelegate(property0, delegate)
        }

        override fun <V> propertyUseDelegate(
            property: KMutableProperty<V>, delegate: AbsSpSaver.AbsSpDelegate<V>?
        ): FieldAccessor.Field<SpSaver, V> {
            val name = spSaver.getPropertySpKeyName(property, delegate, accessDelegateName)
            val outDelegate = delegate ?: run {
                property.isAccessible = true
                when (property) {
                    is KMutableProperty0 -> property.getDelegate()
                    is KMutableProperty1<*, *> ->
                        (property as KMutableProperty1<SpSaver, *>).getDelegate(spSaver)
                    else -> null
                } as? AbsSpSaver.AbsSpDelegate<V>
            }
            var delegate = delegate
            var defaultValue: Any? = null
            while (true) {
                when (delegate) {
                    is DefaultValueSpDelete<*> -> {
                        // 默认值一般是作为最后的装饰，会最先触发，其默认值要保存下来
                        defaultValue = delegate.defaultValue
                        delegate = delegate.saver as AbsSpSaver.AbsSpDelegate<V>
                    }
                    is SpConvertSaver<*, *> -> {
                        @Suppress("UNCHECKED_CAST")
                        delegate as SpConvertSaver<Any, Any?>
                        // 转换器装饰器，将其解装饰，并处理默认值
                        defaultValue = delegate.onSave(defaultValue)
                        delegate = delegate.saver as AbsSpSaver.AbsSpDelegate<V>
                    }
                    is AbsSpSaver.AbsSpDelegate<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        val localDelegate: AbsSpSaver.AbsSpDelegate<Any?> =
                            delegate as AbsSpSaver.AbsSpDelegate<Any?>
                        val field = SpSaverField<SpSaver, Any?>(
                            name,
                            outDelegate as AbsSpSaver.AbsSpDelegate<Any?>,
                            localDelegate,
                            property,
                            defaultValue
                        ) as FieldAccessor.Field<SpSaver, V>
                        return this@Impl.property(field)
                    }
                    else -> throw IllegalArgumentException("不支持的委托类型: ${delegate?.javaClass} $delegate")
                }
            }
        }

        class SpSaverField<SpSaver : AbsSpSaver<SpSaver>, V>(
            override val name: String,
            internal val outDelegate: AbsSpSaver.AbsSpDelegate<V>,
            private val localDelegate: AbsSpSaver.AbsSpDelegate<V?>,
            val property0: KMutableProperty<*>,
            private val defaultValue: V?,
        ) : FieldAccessor.Field<SpSaver, V?> {
            override val vType: PreferenceType = localDelegate.spValueType

            override fun get(data: SpSaver): V? {
                return localDelegate.getValue(data, property0) ?: defaultValue
            }

            override fun set(data: SpSaver, value: V?): SpSaver {
                localDelegate.setValue(data, property0, value)
                return data
            }
        }
    }

    companion object {
        inline fun <SpSaver : AbsSpSaver<SpSaver>, reified V> SpSaverFieldAccessor<SpSaver>.property(
            property0: KMutableProperty0<V>
        ): FieldAccessor.Field<SpSaver, V> = if (accessDelegateName) {
            propertyUseDelegate(property0)
        } else try {
            property(property0, PreferenceType.forType<V>())
        } catch (_: IllegalArgumentException) {
            propertyUseDelegate(property0)
        }
    }
}