package io.github.chenfei0928.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DropDownPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import com.google.protobuf.ProtocolMessageEnum
import com.google.protobuf.protobufEnumAvailableValues
import io.github.chenfei0928.preference.base.VisibleNamed
import io.github.chenfei0928.preference.base.bindEnum

/**
 * @author chenf()
 * @date 2025-02-26 15:12
 */
class EnumDropDownPreference<E : Enum<E>> : DropDownPreference {
    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context)
}

inline fun <reified E> EnumDropDownPreference<E>.bindEnum() where E : Enum<E>, E : VisibleNamed {
    bindEnum(enumValues<E>())
}

inline fun <reified E : Enum<E>> EnumDropDownPreference<E>.bindEnum(visibleNamed: Context.(enum: E) -> CharSequence) {
    bindEnum<E>(enumValues<E>(), visibleNamed)
}

inline fun <reified E> EnumDropDownPreference<E>.bindProtobufEnum(
    visibleNamed: Context.(enum: E) -> CharSequence
) where E : Enum<E>, E : ProtocolMessageEnum {
    bindEnum<E>(protobufEnumAvailableValues<E>(), visibleNamed)
}

class EnumListPreference<E : Enum<E>> : ListPreference {
    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context)
}

inline fun <reified E> EnumListPreference<E>.bindEnum() where E : Enum<E>, E : VisibleNamed {
    bindEnum(enumValues<E>())
}

inline fun <reified E : Enum<E>> EnumListPreference<E>.bindEnum(visibleNamed: Context.(enum: E) -> CharSequence) {
    bindEnum<E>(enumValues<E>(), visibleNamed)
}

inline fun <reified E> EnumListPreference<E>.bindProtobufEnum(
    visibleNamed: Context.(enum: E) -> CharSequence
) where E : Enum<E>, E : ProtocolMessageEnum {
    bindEnum<E>(protobufEnumAvailableValues<E>(), visibleNamed)
}

class EnumMultiSelectListPreference<E : Enum<E>> : MultiSelectListPreference {
    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context)
}

inline fun <reified E> EnumMultiSelectListPreference<E>.bindEnum() where E : Enum<E>, E : VisibleNamed {
    bindEnum(enumValues<E>())
}

inline fun <reified E : Enum<E>> EnumMultiSelectListPreference<E>.bindEnum(
    visibleNamed: Context.(enum: E) -> CharSequence
) {
    bindEnum<E>(enumValues<E>(), visibleNamed)
}

inline fun <reified E> EnumMultiSelectListPreference<E>.bindProtobufEnum(
    visibleNamed: Context.(enum: E) -> CharSequence
) where E : Enum<E>, E : ProtocolMessageEnum {
    bindEnum<E>(protobufEnumAvailableValues<E>(), visibleNamed)
}
