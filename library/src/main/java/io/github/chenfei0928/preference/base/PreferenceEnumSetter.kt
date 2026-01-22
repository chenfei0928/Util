package io.github.chenfei0928.preference.base

import android.content.Context
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import com.google.protobuf.ProtocolMessageEnum
import com.google.protobuf.protobufEnumAvailableValues
import io.github.chenfei0928.collection.mapToArray
import io.github.chenfei0928.reflect.isSubtypeOf

/**
 * @author chenf()
 * @date 2026-01-22 16:07
 */
abstract class PreferenceEnumSetter<E : Enum<E>> {
    abstract val context: Context
    abstract var entries: Array<CharSequence>?
    abstract var entryValues: Array<CharSequence>?

    class ListPreferenceImpl<E : Enum<E>>(
        private val preference: ListPreference
    ) : PreferenceEnumSetter<E>() {
        override val context: Context = preference.context
        override var entries: Array<CharSequence>? by preference::entries
        override var entryValues: Array<CharSequence>? by preference::entryValues
    }

    class MultiSelectListPreferenceImpl<E : Enum<E>>(
        private val preference: MultiSelectListPreference
    ) : PreferenceEnumSetter<E>() {
        override val context: Context = preference.context
        override var entries: Array<CharSequence>? by preference::entries
        override var entryValues: Array<CharSequence>? by preference::entryValues
    }
}

fun <E> PreferenceEnumSetter<E>.bindEnum(enumValues: Array<E>) where  E : Enum<E>, E : VisibleNamed {
    entries = enumValues.mapToArray { it.getVisibleName(context) }
    entryValues = enumValues.mapToArray { it.name }
}

inline fun <reified E> PreferenceEnumSetter<E>.bindEnum() where E : Enum<E>, E : VisibleNamed {
    bindEnum(enumValues<E>())
}

inline fun <reified E : Enum<E>> PreferenceEnumSetter<E>.bindEnum(
    enumValues: Array<E> = enumValues<E>(),
    visibleNamed: Context.(enum: E) -> CharSequence
) {
    entries = enumValues.mapToArray { visibleNamed(context, it) }
    entryValues = enumValues.mapToArray { it.name }
}

inline fun <reified E> PreferenceEnumSetter<E>.bindProtobufEnum(
    visibleNamed: Context.(enum: E) -> CharSequence
) where E : Enum<E>, E : ProtocolMessageEnum {
    bindEnum(protobufEnumAvailableValues<E>(), visibleNamed)
}

inline fun <reified E : Enum<E>> PreferenceEnumSetter<E>.tryBindEnum() {
    if (E::class.java.isSubtypeOf(VisibleNamed::class.java)) {
        bindVisibleNamedEnum(enumValues<E>())
    }
}

fun <E : Enum<E>> PreferenceEnumSetter<E>.bindVisibleNamedEnum(enumValues: Array<E>) {
    entryValues = enumValues.mapToArray { it.name }
    @Suppress("UNCHECKED_CAST")
    enumValues as Array<VisibleNamed>
    entries = enumValues.mapToArray { it.getVisibleName(context) }
}
