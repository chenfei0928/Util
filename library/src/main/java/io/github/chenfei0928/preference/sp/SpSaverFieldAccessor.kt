package io.github.chenfei0928.preference.sp

import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.convert.DefaultValueSpDelete
import io.github.chenfei0928.content.sp.saver.convert.SpConvertSaver
import io.github.chenfei0928.content.sp.saver.delegate.AbsSpAccessDefaultValueDelegate
import io.github.chenfei0928.preference.base.FieldAccessor
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

/**
 * @author chenf()
 * @date 2024-12-20 15:34
 */
interface SpSaverFieldAccessor<SpSaver : AbsSpSaver<SpSaver>> : FieldAccessor<SpSaver> {

    /**
     * 使用[property]与[delegate]注册一个字段，如果 [delegate] 为 null 则向 [SpSaver] 查询或反射获取
     */
    fun <V> property(
        property: KProperty<V>, vType: PreferenceType, delegate: AbsSpSaver.AbsSpDelegate<V>? = null
    ): FieldAccessor.Field<SpSaver, V>

    /**
     * [SpSaver] 的字段信息接口
     *
     * 用于 [SpSaverPreferenceDataStore] 负责提供的获取其Kt字段信息 [property] 与最外层委托 [outDelegate]
     */
    interface Field<SpSaver : AbsSpSaver<SpSaver>, V> : FieldAccessor.Field<SpSaver, V> {
        val property: KProperty<V>
        val outDelegate: AbsSpSaver.AbsSpDelegate<V>
    }

    class Impl<SpSaver : AbsSpSaver<SpSaver>>(
        private val spSaver: SpSaver,
    ) : FieldAccessor.Impl<SpSaver>(false), SpSaverFieldAccessor<SpSaver> {

        override fun <V> property(
            property: KProperty<V>, vType: PreferenceType, delegate: AbsSpSaver.AbsSpDelegate<V>?
        ): FieldAccessor.Field<SpSaver, V> = if (
            property is KMutableProperty0 && delegate != null && vType is PreferenceType.Native
        ) {
            property(NativeTypeField(property, delegate, vType))
        } else {
            val delegate: AbsSpSaver.AbsSpDelegate<V> = delegate
                ?: spSaver.dataStore.getDelegateByProperty(property)
            val outDelegate: AbsSpSaver.AbsSpDelegate<V> = delegate
            var spAccessDelegate: AbsSpSaver.AbsSpDelegate<*> = delegate
            var defaultValue: Any? = null
            while (spAccessDelegate !is AbsSpAccessDefaultValueDelegate<*>) {
                when (spAccessDelegate) {
                    is DefaultValueSpDelete<*> -> {
                        // 默认值一般是作为最后的装饰，会最先触发，其默认值要保存下来
                        defaultValue = spAccessDelegate.defaultValue
                        spAccessDelegate = spAccessDelegate.saver
                    }
                    is SpConvertSaver<*, *> -> {
                        @Suppress("UNCHECKED_CAST")
                        spAccessDelegate as SpConvertSaver<Any, Any?>
                        // 转换器装饰器，将其解装饰，并处理默认值
                        defaultValue = spAccessDelegate.onSave(defaultValue)
                        spAccessDelegate = spAccessDelegate.saver
                    }
                    else -> {
                        throw IllegalArgumentException("不支持的委托类型: ${spAccessDelegate.javaClass} $spAccessDelegate")
                    }
                }
            }
            @Suppress("UNCHECKED_CAST")
            val field = SpSaverPropertyDelegateField<SpSaver, Any?>(
                outDelegate as AbsSpSaver.AbsSpDelegate<Any?>,
                spAccessDelegate as AbsSpAccessDefaultValueDelegate<Any?>,
                property,
                defaultValue
            ) as FieldAccessor.Field<SpSaver, V>
            this@Impl.property(field)
        }

        private class NativeTypeField<SpSaver : AbsSpSaver<SpSaver>, V>(
            override val property: KMutableProperty0<V>,
            override val outDelegate: AbsSpSaver.AbsSpDelegate<V>,
            override val vType: PreferenceType.Native,
        ) : Field<SpSaver, V> {
            override val pdsKey: String = property.name

            override fun get(data: SpSaver): V = property.get()

            override fun set(data: SpSaver, value: V): SpSaver {
                property.set(value)
                return data
            }
        }

        private class SpSaverPropertyDelegateField<SpSaver : AbsSpSaver<SpSaver>, V>(
            override val outDelegate: AbsSpSaver.AbsSpDelegate<V?>,
            private val spAccessDelegate: AbsSpAccessDefaultValueDelegate<V?>,
            override val property: KProperty<V>,
            private val defaultValue: V?,
        ) : Field<SpSaver, V?> {
            override val pdsKey: String = property.name
            override val vType: PreferenceType = spAccessDelegate.spValueType

            override fun get(data: SpSaver): V? {
                return spAccessDelegate.getValue(data, property) ?: defaultValue
            }

            override fun set(data: SpSaver, value: V?): SpSaver {
                spAccessDelegate.setValue(data, property, value)
                return data
            }
        }
    }

    companion object {
        inline fun <SpSaver : AbsSpSaver<SpSaver>, reified V> SpSaverFieldAccessor<SpSaver>.property(
            property0: KMutableProperty0<V>
        ): FieldAccessor.Field<SpSaver, V> = property(property0, PreferenceType.forType<V>())
    }
}
