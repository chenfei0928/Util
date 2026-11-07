package io.github.chenfei0928.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DropDownPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import io.github.chenfei0928.preference.base.PreferenceEnumSetter

/**
 * @author chenf()
 * @date 2025-02-26 15:12
 */
class EnumDropDownPreference<E : Enum<E>>
    : DropDownPreference, PreferenceEnumSetter<E> {
    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context)
}

class EnumListPreference<E : Enum<E>>
    : ListPreference, PreferenceEnumSetter<E> {
    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context)
}

class EnumMultiSelectListPreference<E : Enum<E>>
    : MultiSelectListPreference, PreferenceEnumSetter<E> {
    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context)
}
