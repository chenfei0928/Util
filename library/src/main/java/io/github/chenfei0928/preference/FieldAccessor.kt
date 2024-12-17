package io.github.chenfei0928.preference

import androidx.collection.ArrayMap
import com.google.protobuf.MessageLite

/**
 * 用于 [DataStoreDataStore] 的字段访问器存储与获取
 *
 * @author chenf()
 * @date 2024-10-12 17:54
 */
interface FieldAccessor<T> {

    //<editor-fold desc="快速访问字段扩展" defaultstatus="collapsed">
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

    fun <V> findByName(name: String): Field<T, V>
    //</editor-fold>

    interface Field<T, V> {
        val name: String
        fun get(data: T): V
        fun set(data: T, value: V): T
    }

    open class Impl<T : Any> : FieldAccessor<T> {
        //<editor-fold desc="快速访问字段扩展" defaultstatus="collapsed">
        private val properties: MutableMap<String, Field<T, *>> = ArrayMap()

        /**
         * 判断一个字段是否已经注册
         */
        override operator fun contains(field: Field<T, *>): Boolean =
            field.name in properties

        /**
         * 注册一个持久化字段
         *
         * @param V 值类型
         * @param field 字段说明
         */
        override fun <V> property(field: Field<T, V>): Field<T, V> = field.also {
            val name = field.name
            require(name !in properties) {
                "field name:$name is contain properties:${properties.keys}"
            }
            properties[name] = field
        }

        override fun <V> findByName(name: String): Field<T, V> {
            @Suppress("UNCHECKED_CAST")
            return properties[name] as Field<T, V>
        }
        //</editor-fold>
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
        inline fun <T, V> FieldAccessor<T>.property(
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
        inline fun <T, V> FieldAccessor<*>.field(
            name: String,
            crossinline getter: (data: T) -> V,
            crossinline setter: (data: T, value: V) -> T,
        ): Field<T, V> = object : Field<T, V> {
            override val name: String = name

            override fun get(data: T): V {
                return getter(data)
            }

            override fun set(data: T, value: V): T {
                return setter(data, value)
            }
        }

        /**
         * 用于 Protobuf 的字段创建
         */
        inline fun <T : MessageLite, Builder : MessageLite.Builder, V> FieldAccessor<*>.protobufField(
            name: String,
            crossinline getter: (data: T) -> V,
            crossinline setter: (data: Builder, value: V) -> Builder,
        ): Field<T, V> = object : Field<T, V> {
            override val name: String = name

            override fun get(data: T): V {
                return getter(data)
            }

            override fun set(data: T, value: V): T {
                @Suppress("UNCHECKED_CAST")
                return setter(data.toBuilder() as Builder, value).build() as T
            }
        }
        //</editor-fold>
    }
}
