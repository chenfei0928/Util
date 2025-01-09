package io.github.chenfei0928.preference.base

import com.google.protobuf.Descriptors
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType
import com.google.protobuf.GeneratedMessage
import com.google.protobuf.Message
import com.google.protobuf.MessageLite
import com.google.protobuf.ProtocolMessageEnum
import com.google.protobuf.getProtobufV3DefaultInstance
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.PreferenceType.Companion
import io.github.chenfei0928.preference.base.FieldAccessor.Field
import io.github.chenfei0928.reflect.jTypeOf
import java.lang.reflect.Type
import kotlin.reflect.KFunction

/**
 * 非Lite版Protobuf消息字段
 *
 * 当前类 [get]、[set] 方法中对 [V] 类型的判断不要直接使用 [vType]，先判断是否是枚举型
 * [Descriptors.FieldDescriptor.Type.ENUM]，
 * 可能会涉及到 [Descriptors.FieldDescriptor.JavaType.MESSAGE] 类型，
 * 其在 [PreferenceType.forType] 获取类型时会抛出异常，因为[PreferenceType] 不支持对 [Message] 类型的描述。
 *
 * @param T 字段宿主对象类型
 * @param V 字段类型
 * @property fieldDescriptor 字段描述信息
 *
 * @param vTypeProvider [V] 类型的获取lambda
 *
 * @author chenf()
 * @date 2024-12-19 17:21
 */
class ProtobufMessageField<T : Message, V>(
    private val fieldDescriptor: Descriptors.FieldDescriptor,
    vTypeProvider: () -> Type,
) : Field<T, V> {
    constructor(
        defaultInstance: T, fieldNumber: Int, vTypeProvider: () -> Type,
    ) : this(defaultInstance.descriptorForType.fields[fieldNumber - 1], vTypeProvider)

    override val pdsKey: String = fieldDescriptor.name
    override val vType: PreferenceType by lazy(LazyThreadSafetyMode.NONE) {
        PreferenceType.forType(fieldDescriptor, vTypeProvider)
    }

    @Suppress("UNCHECKED_CAST")
    override fun get(
        data: T
    ): V = if (fieldDescriptor.type != Descriptors.FieldDescriptor.Type.ENUM) {
        // 不是枚举enum，将直接返回原生类型的装箱或protobuf结构体
        data.getField(fieldDescriptor) as V
    } else if (!fieldDescriptor.isRepeated) {
        // 非重复的枚举，返回 EnumValueDescriptor
        val enum = data.getField(fieldDescriptor) as Descriptors.EnumValueDescriptor
        val vType = vType as PreferenceType.EnumNameString<*>
        // 根据其name获取enum
        vType.forName(enum.name) as V
    } else {
        // repeat enum，返回 List<EnumValueDescriptor>
        val enum = data.getField(fieldDescriptor) as List<Descriptors.EnumValueDescriptor>
        when (val vType = vType) {
            is PreferenceType.EnumNameStringCollection<*> -> {
                // 此处并不关心返回的数据类型必须是field类型，不使用field的类型返回
                // 因为当前get方法的返回值是给 BasePreferenceDataStore.getValue 使用的，它会再次进行mapTo
                vType.forProtobufEnumValueDescriptors(enum, false) as V
            }
            is PreferenceType.BaseEnumNameStringCollection<*, *> -> {
                // 根据其 List<EnumValueDescriptor> 获取 Collection<enum>
                vType.forProtobufEnumValueDescriptors(enum) as V
            }
            is PreferenceType.Native,
            is PreferenceType.EnumNameString<*>,
            is PreferenceType.NoSupportPreferenceDataStore -> {
                @Suppress("UseRequire")
                throw IllegalArgumentException("Protobuf 枚举字段 $fieldDescriptor 与vType信息 $vType 类型不匹配")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun set(
        data: T, value: V
    ): T = if (fieldDescriptor.type != Descriptors.FieldDescriptor.Type.ENUM) {
        // 不是枚举enum，直接设置原生类型的装箱或protobuf结构体
        data.toBuilder().setField(fieldDescriptor, value).build() as T
    } else if (fieldDescriptor.isRepeated) {
        // repeat enum，设置 List<EnumValueDescriptor>
        value as Collection<ProtocolMessageEnum>
        data.toBuilder()
            .setField(fieldDescriptor, value.map { it.valueDescriptor })
            .build() as T
    } else {
        // 非重复的枚举，设置 EnumValueDescriptor
        value as ProtocolMessageEnum
        data.toBuilder().setField(fieldDescriptor, value.valueDescriptor).build() as T
    }

    override fun toString(): String = "ProtobufMessageField($pdsKey:${fieldDescriptor.type})"

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

        //<editor-fold desc="对非Full版Protobuf字段的访问的支持" defaultstatus="collapsed">
        /**
         * 用于 Protobuf 的字段创建
         */
        inline fun <T : MessageLite, Builder : MessageLite.Builder, reified V> FieldAccessor<*>.protobufField(
            name: String,
            crossinline getter: (data: T) -> V,
            crossinline setter: (data: Builder, value: V) -> Builder,
        ): Field<T, V> = object : Field<T, V>, () -> PreferenceType {
            override val pdsKey: String = name
            override val vType by lazy(this)
            override fun get(data: T): V = getter(data)
            override fun set(data: T, value: V): T =
                @Suppress("UNCHECKED_CAST")
                setter(data.toBuilder() as Builder, value).build() as T

            override fun invoke(): PreferenceType = PreferenceType.forType<V>()
            override fun toString(): String = "protobufField($pdsKey:$vType)"
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
            return object : Field<T, V>, () -> PreferenceType {
                override val pdsKey: String = name
                override val vType by lazy(this)
                override fun get(data: T): V = getter(data)
                override fun set(data: T, value: V): T =
                    @Suppress("UNCHECKED_CAST")
                    setter(data.toBuilder() as Builder, value).build() as T

                override fun invoke(): PreferenceType = PreferenceType.forType<V>()
                override fun toString(): String = "protobuf($pdsKey:$vType)"
            }
        }
        //</editor-fold>
    }
}
