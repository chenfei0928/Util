package io.github.chenfei0928.content.sp.saver

import android.content.SharedPreferences
import android.os.Build
import androidx.collection.ArraySet
import androidx.preference.PreferenceDataStore
import com.google.protobuf.Descriptors
import io.github.chenfei0928.content.sp.saver.PreferenceType.EnumNameString
import io.github.chenfei0928.preference.FieldAccessor.Field
import io.github.chenfei0928.preference.datastore.DataStoreDataStore
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
            fun forType(type: Type): Native {
                return entries.find { it.type == type || it.primitiveType == type }
                    ?: throw IllegalArgumentException("Not support type: $type")
            }
        }
    }

    /**
     * 用于 [DataStoreDataStore] 的 [Field] 的数据类型，
     * 在[DataStoreDataStore]中对该类型进行判断
     */
    class EnumNameString<E : Enum<E>>(
        private val values: Array<E>
    ) : PreferenceType {
        constructor(eClass: Class<E>) : this(eClass.enumConstants)

        fun forName(name: String): E = values.find { it.name == name }!!
    }

    /**
     * 用于 [DataStoreDataStore] 的 [Field] 的数据类型，
     * 在[DataStoreDataStore]中对该类型进行判断
     */
    class EnumNameStringSet<E : Enum<E>>(
        private val values: Array<E>,
        private val returnType: Class<out Collection<*>>,
    ) : PreferenceType {
        constructor(
            eClass: Class<E>,
            returnType: Class<out Collection<*>>,
        ) : this(eClass.enumConstants, returnType)

        fun forName(names: Collection<String>): Collection<E> =
            names.mapTo(createCollection()) { name ->
                values.find { it.name == name }!!
            }

        private fun createCollection(): MutableCollection<E> = when {
            returnType.isSubclassOf(ArraySet::class.java) -> ArraySet()
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && returnType.isSubclassOf(android.util.ArraySet::class.java) -> android.util.ArraySet()
            returnType.isSubclassOf(HashSet::class.java) -> HashSet()
            returnType.isSubclassOf(SortedSet::class.java) -> TreeSet()
            returnType.isSubclassOf(LinkedHashSet::class.java) -> LinkedHashSet()
            returnType.isSubclassOf(Set::class.java) -> ArraySet()
            returnType.isSubclassOf(Queue::class.java) -> ArrayDeque()
            returnType.isSubclassOf(LinkedList::class.java) -> LinkedList()
            returnType.isSubclassOf(List::class.java) -> ArrayList()
            else -> mutableListOf()
        }

        companion object {
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
        fun forType(type: Type): PreferenceType {
            return if (type is Class<*> && type.isSubclassOf(Enum::class.java)) {
                EnumNameString(type as Class<out Enum<*>>)
            } else if (type !is ParameterizedType) {
                Native.forType(type)
            } else if (type.rawType.let { it is Class<*> && it.isSubclassOf(Collection::class.java) }) {
                EnumNameStringSet.forType(type)
            } else {
                throw IllegalArgumentException("Not support type: $type")
            }
        }

        inline fun <reified T> forType(): PreferenceType =
            forType(jTypeOf<T>())

        fun forType(
            field: Descriptors.FieldDescriptor, tType: Type
        ): PreferenceType = if (field.isRepeated) {
            if (field.type != Descriptors.FieldDescriptor.Type.ENUM || tType !is ParameterizedType) {
                throw IllegalArgumentException("Not support type: $tType $field")
            } else {
                EnumNameStringSet.forType(tType)
            }
        } else when (field.javaType) {
            Descriptors.FieldDescriptor.JavaType.INT -> Native.INT
            Descriptors.FieldDescriptor.JavaType.LONG -> Native.LONG
            Descriptors.FieldDescriptor.JavaType.FLOAT -> Native.FLOAT
            Descriptors.FieldDescriptor.JavaType.DOUBLE ->
                throw IllegalArgumentException("Not support type: $tType $field")
            Descriptors.FieldDescriptor.JavaType.BOOLEAN -> Native.BOOLEAN
            Descriptors.FieldDescriptor.JavaType.STRING -> Native.STRING
            Descriptors.FieldDescriptor.JavaType.BYTE_STRING ->
                throw IllegalArgumentException("Not support type: $tType $field")
            Descriptors.FieldDescriptor.JavaType.ENUM ->
                EnumNameString(tType as Class<out Enum<*>>)
            Descriptors.FieldDescriptor.JavaType.MESSAGE ->
                throw IllegalArgumentException("Not support type: $tType $field")
        }
    }
}
