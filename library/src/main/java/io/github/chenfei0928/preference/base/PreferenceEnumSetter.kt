package io.github.chenfei0928.preference.base

import android.content.Context
import com.google.protobuf.ProtocolMessageEnum
import com.google.protobuf.protobufEnumAvailableValues
import io.github.chenfei0928.collection.mapToArray
import io.github.chenfei0928.reflect.isSubtypeOf

/**
 * @author chenf()
 * @date 2026-01-22 16:07
 */
fun <E> PreferenceEnumSetter<E>.bindEnum(enumValues: Array<E>) where  E : Enum<E>, E : VisibleNamed {
    setEntries(enumValues.mapToArray { it.getVisibleName(context) })
    setEntryValues(enumValues.mapToArray { it.name })
}

inline fun <reified E> PreferenceEnumSetter<E>.bindEnum() where E : Enum<E>, E : VisibleNamed {
    bindEnum(enumValues<E>())
}

inline fun <reified E : Enum<E>> PreferenceEnumSetter<E>.bindEnum(
    enumValues: Array<E> = enumValues<E>(),
    visibleNamed: Context.(enum: E) -> CharSequence
) {
    setEntries(enumValues.mapToArray { visibleNamed(context, it) })
    setEntryValues(enumValues.mapToArray { it.name })
}

inline fun <reified E> PreferenceEnumSetter<E>.bindProtobufEnum(
    visibleNamed: Context.(enum: E) -> CharSequence
) where E : Enum<E>, E : ProtocolMessageEnum {
    bindEnum(protobufEnumAvailableValues<E>(), visibleNamed)
}

inline fun <reified E : Enum<E>> PreferenceEnumSetter<E>.tryBindEnum() {
    if (E::class.java.isSubtypeOf(VisibleNamed::class.java)) {
        bindVisibleNamedEnumUnchecked(enumValues<E>())
    }
}

fun <E : Enum<E>> PreferenceEnumSetter<E>.bindVisibleNamedEnumUnchecked(enumValues: Array<E>) {
    setEntryValues(enumValues.mapToArray { it.name })
    @Suppress("UNCHECKED_CAST")
    enumValues as Array<VisibleNamed>
    setEntries(enumValues.mapToArray { it.getVisibleName(context) })
}
