package io.github.chenfei0928.content.sp.saver

import android.content.SharedPreferences
import android.os.Build
import androidx.collection.ArraySet
import androidx.preference.PreferenceDataStore
import com.google.protobuf.Descriptors
import io.github.chenfei0928.content.sp.saver.PreferenceType.EnumNameString
import io.github.chenfei0928.preference.DataStorePreferenceDataStore
import io.github.chenfei0928.preference.base.FieldAccessor.Field
import io.github.chenfei0928.reflect.isSubclassOf
import io.github.chenfei0928.reflect.jTypeOf
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import java.util.ArrayList
import java.util.LinkedList
import java.util.Queue
import java.util.SortedSet
import java.util.TreeSet
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure

/**
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
        STRING_SET(jTypeOf<Set<String>>(), null),
        INT(Int::class.javaObjectType, Int::class.javaPrimitiveType),
        LONG(Long::class.javaObjectType, Long::class.javaPrimitiveType),
        FLOAT(Float::class.javaObjectType, Float::class.javaPrimitiveType),
        BOOLEAN(Boolean::class.javaObjectType, Boolean::class.javaPrimitiveType);

        companion object {
            fun forType(tClass: Class<*>, tTypeProvider: () -> Type): Native {
                return entries.find { it.type == tClass || it.primitiveType == tClass }
                    ?: tTypeProvider().let { tType ->
                        STRING_SET.takeIf { it.type == tType }
                            ?: throw IllegalArgumentException("Not support type: $tType")
                    }
            }
        }
    }

    /**
     * 用于 [DataStorePreferenceDataStore] 的 [Field] 的数据类型，
     * 在[DataStorePreferenceDataStore]中对该类型进行判断
     */
    class EnumNameString<E : Enum<E>>(
        private val values: Array<E>
    ) : PreferenceType {
        constructor(eClass: Class<E>) : this(eClass.enumConstants)

        fun forName(name: String): E = values.find { it.name == name }!!
    }

    /**
     * 用于 [DataStorePreferenceDataStore] 的 [Field] 的数据类型，
     * 在[DataStorePreferenceDataStore]中对该类型进行判断
     */
    class EnumNameStringSet<E : Enum<E>>(
        private val values: Array<E>,
        private val returnType: Class<out Collection<*>>,
    ) : PreferenceType {
        constructor(
            eClass: Class<E>,
            returnType: Class<out Collection<*>>,
        ) : this(eClass.enumConstants, returnType)

        fun forName(names: Collection<String>, toSet: Boolean): Collection<E> =
            names.mapTo(if (toSet) ArraySet(names.size) else createCollection(names.size)) { name ->
                values.find { it.name == name }!!
            }

        private fun createCollection(size: Int): MutableCollection<E> = when {
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
            else -> ArrayList(size)
        }

        companion object {
            @Suppress("UNCHECKED_CAST")
            fun forType(type: ParameterizedType): EnumNameStringSet<*> {
                val rawClass = type.rawType as Class<out Collection<*>>
                val arg0Type = type.actualTypeArguments[0]
                return if (arg0Type is Class<*> && arg0Type.isSubclassOf(Enum::class.java)) {
                    EnumNameStringSet(
                        eClass = arg0Type as Class<out Enum<*>>,
                        returnType = rawClass,
                    )
                } else if (arg0Type is WildcardType &&
                    arg0Type.upperBounds[0].let { it is Class<*> && it.isSubclassOf(Enum::class.java) }
                ) {
                    EnumNameStringSet(
                        eClass = arg0Type.upperBounds[0] as Class<out Enum<*>>,
                        returnType = rawClass,
                    )
                } else {
                    throw IllegalArgumentException("Not support type: $type")
                }
            }
        }
    }

    companion object {
        fun forType(tClass: Class<*>, tTypeProvider: () -> Type): PreferenceType {
            return if (tClass.isSubclassOf(Enum::class.java)) {
                EnumNameString(tClass as Class<out Enum<*>>)
            } else if (!tClass.isSubclassOf(Collection::class.java)) {
                Native.forType(tClass, tTypeProvider)
            } else {
                val type = tTypeProvider()
                require(type is ParameterizedType) { "Not support type: $type" }
                EnumNameStringSet.forType(type)
            }
        }

        inline fun <reified T> forType(): PreferenceType =
            forType(tClass = T::class.java) { jTypeOf<T>() }

        fun forType(kType: KType): PreferenceType =
            forType(tClass = kType.jvmErasure.java) { kType.javaType }

        /**
         * 为 Protobuf full 的字段 fieldNumber 来获取类型信息
         */
        fun forType(
            field: Descriptors.FieldDescriptor, tTypeProvider: () -> Type
        ): PreferenceType = if (field.isRepeated) {
            val tType = tTypeProvider()
            require(field.type == Descriptors.FieldDescriptor.Type.ENUM && tType is ParameterizedType) {
                "Not support type: $tType $field"
            }
            EnumNameStringSet.forType(tType)
        } else when (field.javaType) {
            Descriptors.FieldDescriptor.JavaType.INT -> Native.INT
            Descriptors.FieldDescriptor.JavaType.LONG -> Native.LONG
            Descriptors.FieldDescriptor.JavaType.FLOAT -> Native.FLOAT
            Descriptors.FieldDescriptor.JavaType.DOUBLE ->
                throw IllegalArgumentException("Not support type: $field")
            Descriptors.FieldDescriptor.JavaType.BOOLEAN -> Native.BOOLEAN
            Descriptors.FieldDescriptor.JavaType.STRING -> Native.STRING
            Descriptors.FieldDescriptor.JavaType.BYTE_STRING ->
                throw IllegalArgumentException("Not support type:ype $field")
            Descriptors.FieldDescriptor.JavaType.ENUM ->
                EnumNameString(eClass = tTypeProvider() as Class<out Enum<*>>)
            Descriptors.FieldDescriptor.JavaType.MESSAGE ->
                throw IllegalArgumentException("Not support type: $field")
        }
    }
}
