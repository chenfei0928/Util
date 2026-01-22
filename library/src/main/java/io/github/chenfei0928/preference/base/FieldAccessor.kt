package io.github.chenfei0928.preference.base

import androidx.collection.ArrayMap
import androidx.preference.PreferenceDataStore
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.preference.DataStoreFieldAccessorCache

/**
 * 用于 [DataStoreFieldAccessorCache] 的字段访问器存储与获取
 *
 * @author chenf()
 * @date 2024-10-12 17:54
 */
interface FieldAccessor<T> {

    //<editor-fold desc="快速访问字段扩展" defaultstatus="collapsed">
    /**
     * 该访问器所包含的所有field，其中不仅会包含 [T] 的 property，还可能会包含某个结构体字段的二阶字段
     * key: [Field.pdsKey]
     */
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
    fun <V, F : Field<T, V>> property(field: F): F

    fun <T1, V> property(
        outerField: Field<T, T1>,
        innerField: Field<T1, V>,
    ): Field<T, V>

    fun <T1, T2, V> property(
        property0: Field<T, T1>,
        property1: Field<T1, T2>,
        property2: Field<T2, V>,
    ): Field<T, V>

    /**
     * 根据本地存储字段名获取其 [Field]
     *
     * @param V 值类型
     * @param name [Field.pdsKey] 同时作为 [androidx.preference.Preference.getKey] 和 [PreferenceDataStore] 回调中的key
     * @return [Field] 如果找不到对应的字段，则返回 null
     */
    fun <V> findByName(name: String): Field<T, V>
    //</editor-fold>

    interface Field<T, V> {
        /**
         * 用于在 [PreferenceDataStore] 中存取所使用的 key，
         * 同时会设置给 [androidx.preference.Preference.getKey]，
         * 该数据不会要求与持久化后保存的结构体中字段名或Map字典key的一致性
         */
        val pdsKey: String

        /**
         * 业务层数据类型与其到持久化后数据类型信息
         */
        val vType: PreferenceType<V>
        fun get(data: T): V
        fun set(data: T, value: V): T

        override fun toString(): String
    }

    interface SpLocalStorageKey {
        /**
         * 该字段返回在本地持久化存储所使用的key。用于：
         * - [io.github.chenfei0928.content.sp.saver.SpCommit] 中根据
         * [kotlin.reflect.KProperty] 移除或判断其是否在本地存储中存在。
         * - 在sp文件更新时，筛选受影响的 [Field] ，即：
         * - [io.github.chenfei0928.preference.sp.SpSaverFieldAccessorCache.onPropertyChange]
         * - 在sp文件更新时，筛选受影响的 [androidx.preference.Preference] ，即：
         * [io.github.chenfei0928.content.sp.saver.registerOnSpPropertyChangeListener]
         * `BaseSpSaverWatcher.kt` 文件中的 `registerOnSpPropertyChangeListener` 扩展函数。
         * 注：该包路径下有该名称的多个重载，KotlinDoc可能引用到错误的方法，签名为
         * ```kotlin
         * fun <SpSaver : BaseSpSaver<SpSaver>> SpSaver.registerOnSpPropertyChangeListener(
         *     owner: LifecycleOwner,
         *     @MainThread callback: (field: SpSaverFieldAccessor.Field<SpSaver, *>) -> Unit,
         * )
         * ```
         */
        val localStorageKey: String
    }

    interface FieldWrapper<F : Field<T, *>, T, V> : Field<T, V> {
        val localField: F

        companion object {
            inline fun <reified F, T, V> findByType(field: Field<T, *>): F? {
                var field: Any? = field
                while (field != null && field !is F) {
                    field = if (field is FieldWrapper<*, *, *>) {
                        field.localField
                    } else {
                        null
                    }
                }
                return field
            }
        }
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
        override fun <V, F : Field<T, V>> property(field: F): F = field.also {
            val name = field.pdsKey
            require(name !in properties) {
                "field name:$name is contain properties:${properties.keys.joinToString()}"
            }
            properties[name] = if (readCache) ReadCacheField(field) else field
        }

        override fun <T1, V> property(
            outerField: Field<T, T1>,
            innerField: Field<T1, V>,
        ): Field<T, V> = FieldWrapperImpl(outerField, innerField).let(::property)

        override fun <T1, T2, V> property(
            property0: Field<T, T1>,
            property1: Field<T1, T2>,
            property2: Field<T2, V>,
        ): Field<T, V> = FieldWrapperImpl(
            FieldWrapperImpl(property0, property1), property2
        ).let(::property)

        /**
         * 使用只读属性与该data class类的 copy 方法来进行读写的字段
         *
         * @param T 宿主类类型
         * @param T1 中间宿主类类型
         * @param V 字段类型
         * @property localField 外部字段到中间宿主类型字段的访问方法
         * @property propertyField 中间宿主的子字段的访问方法
         */
        private class FieldWrapperImpl<T, T1, V>(
            override val localField: Field<T, T1>,
            private val propertyField: Field<T1, V>,
        ) : Field<T, V>, FieldWrapper<Field<T, T1>, T, V> {
            override val pdsKey: String = localField.pdsKey + "_" + propertyField.pdsKey
            override val vType: PreferenceType<V> = propertyField.vType
            override fun get(data: T): V = propertyField.get(localField.get(data))
            override fun set(data: T, value: V): T =
                localField.set(data, propertyField.set(localField.get(data), value))

            override fun toString(): String = "FieldWrapper($pdsKey:$vType)"
        }

        override fun <V> findByName(name: String): Field<T, V> {
            @Suppress("UNCHECKED_CAST")
            return properties[name] as? Field<T, V>
                ?: throw IllegalArgumentException("name $name not registered in ${properties.keys.joinToString()}")
        }
        //</editor-fold>

        internal class ReadCacheField<T : Any, V>(
            override val localField: Field<T, V>
        ) : Field<T, V> by localField, FieldWrapper<Field<T, V>, T, V> {
            private var dataValuePairCache: Pair<T, V>? = null

            override fun get(data: T): V {
                val cachePair = dataValuePairCache
                return if (cachePair?.first === data) {
                    cachePair.second
                } else {
                    val value = localField.get(data)
                    this.dataValuePairCache = data to value
                    value
                }
            }

            override fun set(data: T, value: V): T {
                val newData = localField.set(data, value)
                dataValuePairCache = newData to value
                return newData
            }
        }
    }

    abstract class Inline<T, V>(
        final override val pdsKey: String,
        vClass: Class<V>,
        actualTypeIndex: Int = 1,
    ) : PreferenceType.LazyPreferenceType<V>(vClass, actualTypeIndex), Field<T, V> {
        final override val vType: PreferenceType<V> get() = getPreferenceType()

        //<editor-fold desc="重写Object方法" defaultstatus="collapsed">
        final override fun toString(): String = "InlineField($pdsKey:$vType)"
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Inline<*, *>) return false
            if (!super.equals(other)) return false

            if (pdsKey != other.pdsKey) return false
            if (vType != other.vType) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + pdsKey.hashCode()
            result = 31 * result + vType.hashCode()
            return result
        }
        //</editor-fold>
    }

    abstract class SpKeyInline<T, V>(
        pdsKey: String,
        final override val localStorageKey: String,
        vClass: Class<V>,
        actualTypeIndex: Int = 1,
    ) : Inline<T, V>(pdsKey, vClass, actualTypeIndex), SpLocalStorageKey

    companion object {
        //<editor-fold desc="对其他的访问" defaultstatus="collapsed">
        /**
         * 通过自定义[getter]、[setter]来访问字段
         *
         * @param T 宿主类类型
         * @param V 字段类型
         * @param name 用于[PreferenceDataStore]中访问的字段名称
         * @param localStorageKey 本地存储的键名，默认为[name]一个下划线之前的字符串，
         * 如果没有下划线也没有指定，则不使用 [SpLocalStorageKey] 接口
         * @param getter 访问器
         * @param setter 修改器
         */
        inline fun <T, reified V> FieldAccessor<T>.property(
            name: String,
            localStorageKey: String? = if ('_' in name) name.substringBefore('_') else null,
            crossinline getter: (data: T) -> V,
            crossinline setter: (data: T, value: V) -> T,
        ): Field<T, V> = field(name, localStorageKey, getter, setter).let(::property)

        /**
         * 通过自定义[getter]、[setter]来访问字段
         *
         * @param T 宿主类类型
         * @param V 字段类型
         * @param name 用于[PreferenceDataStore]中访问的字段名称
         * @param localStorageKey 本地存储的键名，默认为[name]一个下划线之前的字符串，
         * 如果没有下划线也没有指定，则不使用 [SpLocalStorageKey] 接口
         * @param getter 访问器
         * @param setter 修改器
         */
        inline fun <T, reified V> FieldAccessor<*>.field(
            name: String,
            localStorageKey: String? = if ('_' in name) name.substringBefore('_') else null,
            crossinline getter: (data: T) -> V,
            crossinline setter: (data: T, value: V) -> T,
        ): Field<T, V> = if (localStorageKey == null) object : Inline<T, V>(name, V::class.java) {
            override fun get(data: T): V = getter(data)
            override fun set(data: T, value: V): T = setter(data, value)
        } else object : SpKeyInline<T, V>(name, localStorageKey, V::class.java) {
            override fun get(data: T): V = getter(data)
            override fun set(data: T, value: V): T = setter(data, value)
        }
        //</editor-fold>
    }
}
