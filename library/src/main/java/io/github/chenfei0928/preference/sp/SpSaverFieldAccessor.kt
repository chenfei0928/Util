package io.github.chenfei0928.preference.sp

import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.convert.DefaultValueSpDelete
import io.github.chenfei0928.content.sp.saver.convert.EnumNameSpConvertSaver
import io.github.chenfei0928.content.sp.saver.convert.EnumSetNameSpConvertSaver
import io.github.chenfei0928.content.sp.saver.convert.SpConvertSaver
import io.github.chenfei0928.content.sp.saver.delegate.AbsSpAccessDefaultValueDelegate
import io.github.chenfei0928.preference.base.FieldAccessor
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1
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
        override val pdsKey: String
            get() = property.name
        val property: KProperty<V>
        val outDelegate: AbsSpSaver.AbsSpDelegate<V>
    }

    class Impl<SpSaver : AbsSpSaver<SpSaver>>(
        private val spSaver: SpSaver,
    ) : FieldAccessor.Impl<SpSaver>(false), SpSaverFieldAccessor<SpSaver> {

        override fun <V> property(
            property: KProperty<V>, vType: PreferenceType, delegate: AbsSpSaver.AbsSpDelegate<V>?
        ): FieldAccessor.Field<SpSaver, V> = when {
            delegate == null || vType is PreferenceType.NoSupportPreferenceDataStore -> {
                // 没传入委托、vType复合类型，需要查找委托信息中的 spAccessDelegate
                property(SpSaverPropertyDelegateField.fromProperty(spSaver, property, delegate))
            }
            property is KMutableProperty0 -> {
                // 原生支持的vType类型，传入了委托
                property(SpDelegateField0(property, delegate, vType))
            }
            property is KMutableProperty1<*, *> -> {
                // 原生支持的vType类型，传入了委托
                @Suppress("UNCHECKED_CAST")
                property as KMutableProperty1<SpSaver, V>
                property(SpDelegateField1(property, delegate, vType))
            }
            else -> {
                // 只读字段或KProperty2字段，通过委托进行读写，而非通过 property
                property(ReadOnlySpDelegateField(property, delegate, vType))
            }
        }

        //<editor-fold desc="传入了委托对象和vType" defaultstatus="collapsed">
        private class SpDelegateField0<SpSaver : AbsSpSaver<SpSaver>, V>(
            override val property: KMutableProperty0<V>,
            override val outDelegate: AbsSpSaver.AbsSpDelegate<V>,
            override val vType: PreferenceType,
        ) : Field<SpSaver, V> {
            override fun get(data: SpSaver): V = property.get()
            override fun set(data: SpSaver, value: V): SpSaver = data.apply { property.set(value) }
            override fun toString(): String = "SpDelegateField0($pdsKey:$vType)"
        }

        private class SpDelegateField1<SpSaver : AbsSpSaver<SpSaver>, V>(
            override val property: KMutableProperty1<SpSaver, V>,
            override val outDelegate: AbsSpSaver.AbsSpDelegate<V>,
            override val vType: PreferenceType,
        ) : Field<SpSaver, V> {
            override fun get(data: SpSaver): V = property.get(data)
            override fun set(data: SpSaver, value: V): SpSaver =
                data.apply { property.set(data, value) }

            override fun toString(): String = "SpDelegateField1($pdsKey:$vType)"
        }

        private class ReadOnlySpDelegateField<SpSaver : AbsSpSaver<SpSaver>, V>(
            override val property: KProperty<V>,
            override val outDelegate: AbsSpSaver.AbsSpDelegate<V>,
            override val vType: PreferenceType,
        ) : Field<SpSaver, V> {
            override fun get(data: SpSaver): V = outDelegate.getValue(data, property)
            override fun set(data: SpSaver, value: V): SpSaver =
                data.apply { outDelegate.setValue(data, property, value) }

            override fun toString(): String = "ReadOnlySpDelegateField($pdsKey:$vType)"
        }
        //</editor-fold>

        //<editor-fold desc="没传入委托、vType复合类型，需要查找委托信息中的 spAccessDelegate" defaultstatus=“collapsed">
        /**
         * 对于使用了 [SpConvertSaver] 委托但又不是 [EnumNameSpConvertSaver]
         * 或 [EnumSetNameSpConvertSaver] 字段使用的字段
         *
         * @param V 值类型
         * @property outDelegate 最外层委托
         * @property spAccessDelegate sp访问的委托
         * @property property 字段信息
         * @property defaultValue 默认值（如果设置了[DefaultValueSpDelete]）
         */
        private class SpSaverPropertyDelegateField<SpSaver : AbsSpSaver<SpSaver>, V>(
            override val outDelegate: AbsSpSaver.AbsSpDelegate<V?>,
            private val spAccessDelegate: AbsSpAccessDefaultValueDelegate<V?>,
            override val property: KProperty<V>,
            private val defaultValue: V?,
        ) : Field<SpSaver, V?> {
            override val pdsKey: String = property.name
            override val vType: PreferenceType = spAccessDelegate.spValueType
            override fun get(data: SpSaver): V? =
                spAccessDelegate.getValue(data, property) ?: defaultValue

            override fun set(data: SpSaver, value: V?): SpSaver = data.apply {
                spAccessDelegate.setValue(data, property, value)
            }

            override fun toString(): String = "SpSaverPropertyDelegateField($pdsKey:$vType)"

            companion object {
                fun <SpSaver : AbsSpSaver<SpSaver>, V> fromProperty(
                    spSaver: SpSaver,
                    property: KProperty<V>,
                    delegate: AbsSpSaver.AbsSpDelegate<V>?
                ): FieldAccessor.Field<SpSaver, V> {
                    // vType复合类型或没传入委托，需要查找委托信息中的 spAccessDelegate
                    val delegate: AbsSpSaver.AbsSpDelegate<V> = delegate
                        ?: spSaver.dataStore.getDelegateByProperty(property)
                    val outDelegate: AbsSpSaver.AbsSpDelegate<V> = delegate
                    var spAccessDelegate: AbsSpSaver.AbsSpDelegate<*> = delegate
                    var defaultValue: Any? = null
                    // 查找 spAccessDelegate 为sp原生值访问委托
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
                                defaultValue = defaultValue?.let { spAccessDelegate.onSave(it) }
                                spAccessDelegate = spAccessDelegate.saver
                            }
                            else -> throw IllegalArgumentException(
                                "不支持的委托类型: ${spAccessDelegate.javaClass} $spAccessDelegate"
                            )
                        }
                    }
                    @Suppress("UNCHECKED_CAST")
                    return SpSaverPropertyDelegateField<SpSaver, Any?>(
                        outDelegate as AbsSpSaver.AbsSpDelegate<Any?>,
                        spAccessDelegate as AbsSpAccessDefaultValueDelegate<Any?>,
                        property,
                        defaultValue
                    ) as FieldAccessor.Field<SpSaver, V>
                }
            }
        }
        //</editor-fold>
    }

    companion object {
        inline fun <SpSaver : AbsSpSaver<SpSaver>, reified V> SpSaverFieldAccessor<SpSaver>.property(
            property0: KMutableProperty0<V>
        ): FieldAccessor.Field<SpSaver, V> = property(property0, PreferenceType.forType<V>())
    }
}
