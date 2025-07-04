package io.github.chenfei0928.content.sp.saver

import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.collection.ArraySet
import androidx.collection.SparseArrayCompat
import androidx.preference.PreferenceDataStore
import com.bumptech.glide.util.Util
import com.google.common.reflect.GoogleTypes
import com.google.protobuf.Descriptors
import com.google.protobuf.PROTOBUF_ENUM_UNRECOGNIZED_NAME
import com.google.protobuf.ProtocolMessageEnum
import com.google.protobuf.enumClass
import com.google.protobuf.isUnrecognized
import io.github.chenfei0928.content.sp.saver.convert.BaseSpConvert
import io.github.chenfei0928.lang.contains
import io.github.chenfei0928.preference.DataStoreFieldAccessorCache
import io.github.chenfei0928.preference.base.FieldAccessor.Field
import io.github.chenfei0928.reflect.LazyTypeToken
import io.github.chenfei0928.reflect.isSubclassOf
import io.github.chenfei0928.reflect.isSubtypeOf
import io.github.chenfei0928.reflect.jvmErasureClassOrNull
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import java.util.LinkedList
import java.util.Queue
import java.util.SortedSet
import java.util.TreeSet
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure

/**
 * 当前类中 [Native.STRING_SET] 依赖了 [GoogleTypes] 类，
 * 其依赖了 [Gson](https://github.com/google/gson) 或 [Guava](https://github.com/google/guava) 库作为内部实现。
 *
 * @author chenf()
 * @date 2024-12-18 15:51
 */
sealed interface PreferenceType {

    /**
     * [PreferenceDataStore] 的原生类型支持，同时也是 [SharedPreferences] 的原生类型支持
     */
    enum class Native(
        private val type: Type,
        private val primitiveType: Class<*>?,
    ) : PreferenceType {
        STRING(String::class.java, null),
        STRING_SET(
            // 经测试此与 jTypeOf<Set<String>>() 类型一致，直接构建类型以减少一次类创建与反射开销
            // 因为 Set 接口的泛型参数声明为 out V ，所以第三个参数不是 String，而是 subtypeOf String
            // 为它和它的子类型
            GoogleTypes.newParameterizedTypeWithOwner(
                null, Set::class.java, GoogleTypes.subtypeOf(String::class.java)
            ), null
        ),
        INT(Int::class.javaObjectType, Int::class.javaPrimitiveType),
        LONG(Long::class.javaObjectType, Long::class.javaPrimitiveType),
        FLOAT(Float::class.javaObjectType, Float::class.javaPrimitiveType),
        BOOLEAN(Boolean::class.javaObjectType, Boolean::class.javaPrimitiveType);

        companion object {
            fun forTypeOrNull(tClass: Class<*>, tTypeProvider: () -> Type): Native? {
                return entries.find { it.type == tClass || it.primitiveType == tClass }
                    ?: if (tTypeProvider().isSubtypeOf(STRING_SET.type))
                        STRING_SET else null
            }

            fun forType(tClass: Class<*>, tTypeProvider: () -> Type): Native {
                return forTypeOrNull(tClass, tTypeProvider)
                    ?: throw IllegalArgumentException("Not support type: ${tTypeProvider()}")
            }

            inline fun <reified T> forType(): Native =
                forType(T::class.java, LazyTypeToken<T>())
        }
    }

    /**
     * 用于 [DataStoreFieldAccessorCache] 的 [Field] 的数据类型，
     * 在[DataStoreFieldAccessorCache]中对该类型进行判断
     */
    open class EnumNameString<E : Enum<E>>(
        val eClass: Class<E>,
        protected val values: Array<E>,
    ) : PreferenceType {
        constructor(eClass: Class<E>) : this(eClass, eClass.enumConstants)

        //<editor-fold desc="枚举值转换与重写Object的方法" defaultstatus="collapsed">
        open fun forName(name: String?, defaultValue: E? = null): E =
            values.find { it.name == name } ?: defaultValue
            ?: throw IllegalArgumentException("Enum name: $name not found in $eClass")

        open fun toName(value: E?): String = value?.name ?: ""

        override fun equals(other: Any?): Boolean {
            return if (this.javaClass != other?.javaClass) {
                false
            } else if (other is EnumNameString<*>) {
                this.eClass == other.eClass && this.values.contentEquals(other.values)
            } else false
        }

        override fun hashCode(): Int = Util.hashCode(eClass.hashCode(), values.contentHashCode())
        override fun toString(): String = "EnumNameString(eClass=$eClass)"
        //</editor-fold>

        companion object {
            inline operator fun <reified E : Enum<E>> invoke() =
                EnumNameString(E::class.java, enumValues<E>())
        }

        /**
         * Protobuf枚举，仅用于序列化到本地文件/从本地文件反序列化，避免跨版本间枚举名变更。
         *
         * 内存之间交互数据使用时使用 [Enum.name] 来处理即可，不使用此类
         *
         * 序列化时会使用其 [ProtocolMessageEnum.getNumber] 或 [PROTOBUF_ENUM_UNRECOGNIZED_NAME] 输出，
         * 反序列化时优先使用其 number，number 无法解析时使用name反序列化。
         */
        class ProtobufEnumNumber<E>(
            eClass: Class<E>, values: Array<E>
        ) : EnumNameString<E>(eClass, values) where E : Enum<E>, E : ProtocolMessageEnum {
            constructor(eClass: Class<E>) : this(eClass, eClass.enumConstants)

            //<editor-fold desc="枚举值转换与重写Object的方法" defaultstatus="collapsed">
            private val numberedValue = SparseArrayCompat<E>().apply {
                values.filter { !it.isUnrecognized }.forEach {
                    put(it.number, it)
                }
            }
            private val unrecognized: E = values.find { it.isUnrecognized }!!

            override fun forName(name: String?, defaultValue: E?): E {
                val number = name?.toIntOrNull()
                return if (number == null)
                    values.find { it.name == name } ?: defaultValue ?: unrecognized
                else numberedValue[number] ?: defaultValue ?: unrecognized
            }

            override fun toName(value: E?): String {
                return if (value == null || value.isUnrecognized) PROTOBUF_ENUM_UNRECOGNIZED_NAME
                else value.number.toString()
            }

            override fun toString(): String = "ProtobufEnumNumberOrName(eClass=$eClass)"
            //</editor-fold>

            companion object {
                inline operator fun <reified E> invoke(): ProtobufEnumNumber<E>
                        where E : Enum<E>, E : ProtocolMessageEnum =
                    ProtobufEnumNumber(E::class.java, enumValues<E>())
            }
        }
    }

    abstract class BaseEnumNameStringCollection<E : Enum<E>, C : Collection<E>>(
        protected val type: EnumNameString<E>,
    ) : PreferenceType {
        //<editor-fold desc="枚举值转换与重写Object的方法" defaultstatus="collapsed">
        open fun forNames(
            names: Collection<String?>,
            focusReturnType: Boolean,
            defaultValue: E? = null
        ): C = names.mapTo(createCollection(names.size)) { type.forName(it, defaultValue) }

        open fun forProtobufEnumValueDescriptors(
            enums: List<Descriptors.EnumValueDescriptor>,
            focusReturnType: Boolean,
            defaultValue: E? = null
        ): C = enums.mapTo(createCollection(enums.size)) { type.forName(it.name, defaultValue) }

        open fun toNames(enums: Collection<E?>, focusReturnType: Boolean): Collection<String> =
            enums.mapTo(createCollection(enums.size)) { type.toName(it) }

        protected abstract fun <MC> createCollection(size: Int): MC where MC : MutableCollection<E>

        override fun equals(other: Any?): Boolean {
            return if (this.javaClass != other?.javaClass) {
                false
            } else if (other is BaseEnumNameStringCollection<*, *>) {
                this.type == other.type
            } else false
        }

        override fun hashCode(): Int = type.hashCode()
        override fun toString(): String = "BaseEnumNameStringCollection(eClass=${type.eClass})"
        //</editor-fold>

        companion object {
            inline operator fun <reified E : Enum<E>, C : MutableCollection<E>> invoke(
                crossinline createCollection: (size: Int) -> C
            ) = object : BaseEnumNameStringCollection<E, C>(
                EnumNameString(E::class.java, enumValues<E>())
            ) {
                @Suppress("UNCHECKED_CAST")
                override fun <MC : MutableCollection<E>> createCollection(size: Int): MC =
                    createCollection(size) as MC
            }

            fun <E : Enum<E>> forEnumDescriptor(
                enumDescriptor: Descriptors.EnumDescriptor, eClass: Class<E>?
            ): BaseEnumNameStringCollection<E, MutableList<E>> {
                val eClass: Class<E> = eClass ?: enumDescriptor.enumClass()
                return object : BaseEnumNameStringCollection<E, MutableList<E>>(
                    EnumNameString(eClass)
                ) {
                    @Suppress("UNCHECKED_CAST")
                    override fun <MC : MutableCollection<E>> createCollection(size: Int): MC =
                        ArrayList<E>(size) as MC
                }
            }
        }
    }

    /**
     * 用于 [DataStoreFieldAccessorCache] 的 [Field] 的数据类型，
     * 在[DataStoreFieldAccessorCache]中对该类型进行判断
     */
    class EnumNameStringCollection<E : Enum<E>>(
        type: EnumNameString<E>,
        private val returnType: Class<out Collection<*>>,
    ) : BaseEnumNameStringCollection<E, Collection<E>>(type) {
        constructor(
            eClass: Class<E>,
            returnType: Class<out Collection<*>>,
        ) : this(EnumNameString<E>(eClass), returnType)

        //<editor-fold desc="枚举值转换与重写Object的方法" defaultstatus="collapsed">
        override fun forNames(
            names: Collection<String?>, focusReturnType: Boolean, defaultValue: E?
        ): Collection<E> = names.mapTo(
            if (focusReturnType) createCollection(names.size) else ArraySet(names.size),
        ) { type.forName(it, defaultValue) }

        override fun forProtobufEnumValueDescriptors(
            enums: List<Descriptors.EnumValueDescriptor>,
            focusReturnType: Boolean,
            defaultValue: E?
        ): Collection<E> = enums.mapTo(
            if (focusReturnType) createCollection(enums.size) else ArraySet(enums.size)
        ) { type.forName(it.name, defaultValue) }

        override fun toNames(
            enums: Collection<E?>, focusReturnType: Boolean
        ): Collection<String> = enums.mapTo(
            if (focusReturnType) createCollection(enums.size) else ArraySet(enums.size)
        ) { type.toName(it) }

        override fun equals(other: Any?): Boolean {
            return super.equals(other) && if (other is EnumNameStringCollection<*>) {
                this.returnType == other.returnType
            } else false
        }

        override fun hashCode(): Int {
            return Util.hashCode(super.hashCode(), returnType.hashCode())
        }

        override fun toString(): String =
            "EnumNameStringCollection(eClass=${type.eClass}, returnType=$returnType)"

        /**
         * 构建集合，查表 like Gson:
         * [com.google.gson.internal.ConstructorConstructor.newDefaultImplementationConstructor]
         */
        @Suppress("CyclomaticComplexMethod", "UNCHECKED_CAST")
        override fun <MC : MutableCollection<E>> createCollection(size: Int): MC = when {
            returnType.isSubclassOf(ArraySet::class.java) -> ArraySet(size)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && returnType.isSubclassOf(android.util.ArraySet::class.java) ->
                android.util.ArraySet(size)
            returnType.isSubclassOf(HashSet::class.java) -> HashSet(size)
            returnType.isSubclassOf(SortedSet::class.java) -> TreeSet()
            returnType.isSubclassOf(LinkedHashSet::class.java) -> LinkedHashSet(size)
            returnType.isSubclassOf(Set::class.java) -> ArraySet(size)
            returnType.isSubclassOf(Queue::class.java) -> ArrayDeque(size)
            returnType.isSubclassOf(LinkedList::class.java) -> LinkedList()
            returnType.isSubclassOf(List::class.java) -> ArrayList(size)
            // 抽象类或接口，返回arrayList
            returnType.isInterface
                    || Modifier.ABSTRACT in returnType.modifiers -> ArrayList(size)
            else -> try {
                // 反射创建实例
                @Suppress("UNCHECKED_CAST")
                returnType.getConstructor(Integer.TYPE).newInstance(size) as MutableCollection<E>
            } catch (e: ReflectiveOperationException) {
                Log.v(TAG, "createCollection by reflect: size int constructor $returnType", e)
                try {
                    @Suppress("UNCHECKED_CAST")
                    returnType.getConstructor().newInstance() as MutableCollection<E>
                } catch (e: ReflectiveOperationException) {
                    Log.v(TAG, "createCollection by reflect: empty constructor $returnType", e)
                    ArrayList(size)
                }
            }
        } as MC
        //</editor-fold>

        companion object {
            inline operator fun <reified E : Enum<E>, reified C : Collection<E>> invoke(): EnumNameStringCollection<E> =
                EnumNameStringCollection(
                    EnumNameString(E::class.java, enumValues<E>()), C::class.java
                )

            fun forType(type: ParameterizedType): EnumNameStringCollection<*> {
                //<editor-fold desc="通过反射获取returnType与eClass" defaultstatus="collapsed">
                @Suppress("UNCHECKED_CAST")
                val rawClass = type.rawType as Class<out Collection<*>>
                val arg0Type = type.actualTypeArguments[0]
                if (arg0Type is Class<*> && arg0Type.isSubclassOf(Enum::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return EnumNameStringCollection(
                        eClass = arg0Type as Class<out Enum<*>>,
                        returnType = rawClass,
                    )
                } else if (arg0Type is WildcardType) {
                    val arg0Class = arg0Type.jvmErasureClassOrNull<Enum<*>>()
                    if (arg0Class?.isSubclassOf(Enum::class.java) == true) {
                        return EnumNameStringCollection(
                            eClass = arg0Class, returnType = rawClass,
                        )
                    } else {
                        @Suppress("UseRequire")
                        throw IllegalArgumentException("Not support type: $type")
                    }
                } else {
                    @Suppress("UseRequire")
                    throw IllegalArgumentException("Not support type: $type")
                }
                //</editor-fold>
            }
        }
    }

    /**
     * 其它平台未原生支持的复杂类型，在当前类的各个 forType 中均不会返回该类型，
     * 仅用作 [BaseSpConvert] 的子类中使用
     */
    open class Struct<T> : LazyTypeToken<T>, PreferenceType {
        protected constructor(actualTypeIndex: Int) : super(actualTypeIndex)
        constructor(type: Type) : super(type)

        override fun toString(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            "Struct<${value.typeName}>"
        } else {
            "Struct<$value>"
        }

        companion object {
            inline operator fun <reified T> invoke() = object : Struct<T>(0) {}
        }
    }

    companion object {
        private const val TAG = "PreferenceType"

        fun forType(tClass: Class<*>, tTypeProvider: () -> Type): PreferenceType {
            return if (tClass.isSubclassOf(Enum::class.java)) {
                @Suppress("UNCHECKED_CAST")
                EnumNameString(tClass as Class<out Enum<*>>)
            } else Native.forTypeOrNull(tClass, tTypeProvider) ?: run {
                val type = tTypeProvider()
                require(type is ParameterizedType) { "Not support type: $type" }
                EnumNameStringCollection.forType(type)
            }
        }

        inline fun <reified T> forType(): PreferenceType =
            forType(tClass = T::class.java, LazyTypeToken<T>())

        /**
         * 用于给 [kotlin.reflect.KProperty] 的场景中获取其类型信息
         */
        fun forType(kType: KType): PreferenceType =
            forType(tClass = kType.jvmErasure.java) { kType.javaType }

        /**
         * 为 Protobuf full 的字段 fieldNumber 来获取类型信息
         */
        fun <T> forType(
            field: Descriptors.FieldDescriptor, tClass: Class<T>?
        ): PreferenceType = if (field.isRepeated) {
            // protobuf 不支持 Set<String> 类型，只有 List<String> 类型，不处理 Native.StringSet
            require(field.type == Descriptors.FieldDescriptor.Type.ENUM) {
                "Not support type: $field"
            }
            BaseEnumNameStringCollection.forEnumDescriptor(field.enumType, null)
        } else when (field.javaType) {
            Descriptors.FieldDescriptor.JavaType.INT -> Native.INT
            Descriptors.FieldDescriptor.JavaType.LONG -> Native.LONG
            Descriptors.FieldDescriptor.JavaType.FLOAT -> Native.FLOAT
            Descriptors.FieldDescriptor.JavaType.DOUBLE ->
                throw IllegalArgumentException("Not support type: $field")
            Descriptors.FieldDescriptor.JavaType.BOOLEAN -> Native.BOOLEAN
            Descriptors.FieldDescriptor.JavaType.STRING -> Native.STRING
            Descriptors.FieldDescriptor.JavaType.BYTE_STRING ->
                throw IllegalArgumentException("Not support type: $field")
            Descriptors.FieldDescriptor.JavaType.ENUM ->
                @Suppress("UNCHECKED_CAST") EnumNameString(
                    tClass as? Class<out Enum<*>> ?: field.enumType.enumClass()
                )
            Descriptors.FieldDescriptor.JavaType.MESSAGE ->
                throw IllegalArgumentException("Not support type: $field")
        }
    }
}
