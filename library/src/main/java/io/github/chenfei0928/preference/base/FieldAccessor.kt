package io.github.chenfei0928.preference.base

import androidx.collection.ArrayMap
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.preference.DataStorePreferenceDataStore

/**
 * 用于 [DataStorePreferenceDataStore] 的字段访问器存储与获取
 *
 * @author chenf()
 * @date 2024-10-12 17:54
 */
interface FieldAccessor<T> {

    //<editor-fold desc="快速访问字段扩展" defaultstatus="collapsed">
    val properties: Map<String, Field<T, *>>

    /**
     * 判断一个字段是否已经注册
     */
    operator fun contains(field: Field<T, *>): Boolean

    /**
     * 注册一个持久化字段
     *
     * @param V 值类型
     * @param field 字段说明
     */
    fun <V> property(
        field: Field<T, V>
    ): Field<T, V>

    fun <T1, V> property(
        outerField: Field<T, T1>,
        innerField: Field<T1, V>,
    ): Field<T, V>

    fun <T1, T2, V> property(
        property0: Field<T, T1>,
        property1: Field<T1, T2>,
        property2: Field<T2, V>,
    ): Field<T, V>

    fun <V> findByName(name: String): Field<T, V>
    //</editor-fold>

    interface Field<T, V> {
        /**
         * 用于在 [androidx.preference.PreferenceDataStore] 中存取所使用的 key，
         * 同时会设置给 [androidx.preference.Preference.getKey]，不要求与持久化后保存的结构体或Map字典的key相同
         */
        val pdsKey: String

        /**
         * 业务层数据类型与其到持久化后数据类型信息
         */
        val vType: PreferenceType
        fun get(data: T): V
        fun set(data: T, value: V): T

        override fun toString(): String
    }

    open class Impl<T : Any>(
        private val readCache: Boolean
    ) : FieldAccessor<T> {
        //<editor-fold desc="快速访问字段扩展" defaultstatus="collapsed">
        override val properties: MutableMap<String, Field<T, *>> = ArrayMap()

        /**
         * 判断一个字段是否已经注册
         */
        override operator fun contains(field: Field<T, *>): Boolean =
            field.pdsKey in properties

        /**
         * 注册一个持久化字段
         *
         * @param V 值类型
         * @param field 字段说明
         */
        override fun <V> property(field: Field<T, V>): Field<T, V> = field.also {
            val name = field.pdsKey
            require(name !in properties) {
                "field name:$name is contain properties:${properties.keys.joinToString()}"
            }
            properties[name] = if (readCache) ReadCacheField(field) else field
        }

        override fun <T1, V> property(
            outerField: Field<T, T1>,
            innerField: Field<T1, V>,
        ): Field<T, V> = FieldWrapper(outerField, innerField).let(::property)

        override fun <T1, T2, V> property(
            property0: Field<T, T1>,
            property1: Field<T1, T2>,
            property2: Field<T2, V>,
        ): Field<T, V> = FieldWrapper(
            FieldWrapper(property0, property1), property2
        ).let(::property)

        /**
         * 使用只读属性与该data class类的 copy 方法来进行读写的字段
         *
         * @param T 宿主类类型
         * @param T1 中间宿主类类型
         * @param V 字段类型
         * @property outerField 外部字段到中间宿主类型字段的访问方法
         * @property innerField 中间宿主的子字段的访问方法
         */
        private class FieldWrapper<T, T1, V>(
            private val outerField: Field<T, T1>,
            private val innerField: Field<T1, V>,
        ) : Field<T, V> {
            override val pdsKey: String = outerField.pdsKey + "_" + innerField.pdsKey
            override val vType: PreferenceType = innerField.vType
            override fun get(data: T): V = innerField.get(outerField.get(data))
            override fun set(data: T, value: V): T =
                outerField.set(data, innerField.set(outerField.get(data), value))

            override fun toString(): String = "FieldWrapper($pdsKey:$vType)"
        }

        override fun <V> findByName(name: String): Field<T, V> {
            @Suppress("UNCHECKED_CAST")
            return properties[name] as? Field<T, V>
                ?: throw IllegalArgumentException("name $name not registered in ${properties.keys.joinToString()}")
        }
        //</editor-fold>

        internal class ReadCacheField<T : Any, V>(
            val field: Field<T, V>
        ) : Field<T, V> by field {
            private var dataValuePairCache: Pair<T, V>? = null

            override fun get(data: T): V {
                val cachePair = dataValuePairCache
                return if (cachePair?.first === data) {
                    cachePair.second
                } else {
                    val value = field.get(data)
                    this.dataValuePairCache = data to value
                    value
                }
            }

            override fun set(data: T, value: V): T {
                val newData = field.set(data, value)
                dataValuePairCache = newData to value
                return newData
            }
        }
    }

    companion object {
        //<editor-fold desc="对其他的访问" defaultstatus="collapsed">
        /**
         * 通过自定义[getter]、[setter]来访问字段
         *
         * @param T 宿主类类型
         * @param V 字段类型
         * @param name 字段名称
         * @param getter 访问器
         * @param setter 修改器
         */
        inline fun <T, reified V> FieldAccessor<T>.property(
            name: String,
            crossinline getter: (data: T) -> V,
            crossinline setter: (data: T, value: V) -> T,
        ): Field<T, V> = field(name, getter, setter).let(::property)

        /**
         * 通过自定义[getter]、[setter]来访问字段
         *
         * @param T 宿主类类型
         * @param V 字段类型
         * @param name 字段名称
         * @param getter 访问器
         * @param setter 修改器
         */
        inline fun <T, reified V> FieldAccessor<*>.field(
            name: String,
            crossinline getter: (data: T) -> V,
            crossinline setter: (data: T, value: V) -> T,
        ): Field<T, V> = object : Field<T, V>, () -> PreferenceType {
            override val pdsKey: String = name
            override val vType by lazy(LazyThreadSafetyMode.NONE, this)
            override fun get(data: T): V = getter(data)
            override fun set(data: T, value: V): T = setter(data, value)
            override fun invoke(): PreferenceType = PreferenceType.forType<V>()
            override fun toString(): String = "field($pdsKey:$vType)"
        }
        //</editor-fold>
    }
}
