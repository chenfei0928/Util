package io.github.chenfei0928.preference

import androidx.collection.ArrayMap
import com.google.protobuf.Descriptors
import com.google.protobuf.GeneratedMessage
import com.google.protobuf.Message
import com.google.protobuf.MessageLite
import com.google.protobuf.ProtocolMessageEnum
import com.google.protobuf.getProtobufV3DefaultInstance
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.preference.FieldAccessor.ProtobufMessageField
import io.github.chenfei0928.reflect.jTypeOf
import java.lang.reflect.Type
import kotlin.reflect.KFunction

/**
 * 用于 [DataStoreDataStore] 的字段访问器存储与获取
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

    fun <V> findByName(name: String): Field<T, V>
    //</editor-fold>

    interface Field<T, V> {
        val name: String
        val vType: PreferenceType
        fun get(data: T): V
        fun set(data: T, value: V): T
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
                "field name:$name is contain properties:${properties.keys.joinToString()}"
            }
            properties[name] = if (readCache) ReadCacheField(field) else field
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
            private var cachePair: Pair<T, V>? = null
            override fun get(data: T): V {
                val cachePair = cachePair
                return if (cachePair?.first === data) {
                    cachePair.second
                } else {
                    val v = field.get(data)
                    this.cachePair = data to v
                    v
                }
            }
        }
    }

    //<editor-fold desc="Protobuf 标准版字段访问的Field" defaultstate="collapsed">
    class ProtobufMessageField<T : Message, V>(
        private val field: Descriptors.FieldDescriptor,
        vTypeProvider: () -> Type,
    ) : Field<T, V> {
        constructor(
            defaultInstance: T, fieldNumber: Int, vTypeProvider: () -> Type,
        ) : this(defaultInstance.descriptorForType.fields[fieldNumber - 1], vTypeProvider)

        override val name: String = field.name
        override val vType: PreferenceType by lazy {
            PreferenceType.forType(field, vTypeProvider)
        }

        override fun get(data: T): V = if (field.type != Descriptors.FieldDescriptor.Type.ENUM) {
            data.getField(field) as V
        } else if (!field.isRepeated) {
            val enum = data.getField(field) as Descriptors.EnumValueDescriptor
            val vType = vType as PreferenceType.EnumNameString<*>
            vType.forName(enum.name) as V
        } else {
            val enum = data.getField(field) as List<Descriptors.EnumValueDescriptor>
            val vType = vType as PreferenceType.EnumNameStringSet<*>
            vType.forName(enum.map { it.name }) as V
        }

        override fun set(
            data: T, value: V
        ): T = if (field.type != Descriptors.FieldDescriptor.Type.ENUM) {
            data.toBuilder().setField(field, value).build() as T
        } else if (field.isRepeated) {
            value as Collection<out ProtocolMessageEnum>
            data.toBuilder().setField(field, value.map { it.valueDescriptor }).build() as T
        } else {
            value as ProtocolMessageEnum
            data.toBuilder().setField(field, value.valueDescriptor).build() as T
        }

        companion object {
            inline operator fun <reified T : Message, reified V> invoke(
                fieldNumber: Int
            ) = ProtobufMessageField<T, V>(
                T::class.java.getProtobufV3DefaultInstance(), fieldNumber
            ) { jTypeOf<V>() }

            inline fun <reified T : GeneratedMessage, reified V> FieldAccessor<T>.field(
                fieldNumber: Int
            ): Field<T, V> = invoke<T, V>(fieldNumber)

            inline fun <reified T : GeneratedMessage, reified V> FieldAccessor<T>.property(
                fieldNumber: Int
            ): Field<T, V> = field<T, V>(fieldNumber).let(::property)
        }
    }
    //</editor-fold>

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
        ): Field<T, V> = object : Field<T, V> {
            override val name: String = name
            override val vType = PreferenceType.forType<V>()

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
        inline fun <T : MessageLite, Builder : MessageLite.Builder, reified V> FieldAccessor<*>.protobufField(
            name: String,
            crossinline getter: (data: T) -> V,
            crossinline setter: (data: Builder, value: V) -> Builder,
        ): Field<T, V> = object : Field<T, V> {
            override val name: String = name
            override val vType = PreferenceType.forType<V>()

            override fun get(data: T): V = getter(data)

            @Suppress("UNCHECKED_CAST")
            override fun set(data: T, value: V): T =
                setter(data.toBuilder() as Builder, value).build() as T
        }

        /**
         * 用于 Protobuf 的字段创建
         */
        inline fun <T : MessageLite, Builder : MessageLite.Builder, reified V> FieldAccessor<T>.protobufProperty(
            name: String,
            crossinline getter: (data: T) -> V,
            crossinline setter: (data: Builder, value: V) -> Builder,
        ): Field<T, V> = protobufField<T, Builder, V>(name, getter, setter).let(::property)

        /**
         * 用于 Protobuf 的字段创建
         */
        inline fun <T, Builder, reified V, Getter, Setter> FieldAccessor<*>.protobuf(
            name: String,
            getter: Getter,
            setter: Setter,
        ): Field<T, V> where
                T : MessageLite,
                Builder : MessageLite.Builder,
                Getter : KFunction<V>,
                Getter : Function1<T, V>,
                Setter : KFunction<Builder>,
                Setter : Function2<Builder, V, Builder> {
            return object : Field<T, V> {
                override val name: String = name
                override val vType = PreferenceType.forType<V>()

                override fun get(data: T): V {
                    return getter(data)
                }

                override fun set(data: T, value: V): T {
                    return setter(data.toBuilder() as Builder, value).build() as T
                }
            }
        }
        //</editor-fold>
    }
}
