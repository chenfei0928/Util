package io.github.chenfei0928.preference

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.preference.CheckBoxPreference
import androidx.preference.DropDownPreference
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceGroup
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import kotlin.reflect.KProperty0

/**
 * PreferenceScreen的构建器
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-01-26 15:02
 */
abstract class AbsPreferenceGroupBuilder0<Builder>(
    context: Context, preferenceGroup: PreferenceGroup
) : AbsPreferenceGroupBuilder<Builder>(context, preferenceGroup) {

    //<editor-fold desc="KProperty0来获取字段名" defaultstate="collapsed">
    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    abstract fun getPropertySpKeyName(property: KProperty0<*>): String

    inline fun checkBoxPreference(
        property: KProperty0<Boolean>,
        block: CheckBoxPreference.() -> Unit
    ): Builder = applyBuilder {
        checkBoxPreference(getPropertySpKeyName(property), block)
    }

    inline fun <reified E> dropDownPreference(
        property: KProperty0<E>,
        block: DropDownPreference.() -> Unit
    ): Builder where E : Enum<E>, E : VisibleNamed = applyBuilder {
        dropDownPreference<E>(getPropertySpKeyName(property), block)
    }

    inline fun editTextPreference(
        property: KProperty0<String?>,
        block: EditTextPreference.() -> Unit
    ): Builder = applyBuilder {
        editTextPreference(getPropertySpKeyName(property), block)
    }

    inline fun <reified E> listPreference(
        property: KProperty0<E>,
        block: ListPreference.() -> Unit
    ): Builder where E : Enum<E>, E : VisibleNamed = applyBuilder {
        listPreference<E>(getPropertySpKeyName(property), block)
    }

    inline fun <reified E> multiSelectListPreference(
        property: KProperty0<Set<E>>,
        block: MultiSelectListPreference.() -> Unit
    ): Builder where E : Enum<E>, E : VisibleNamed = applyBuilder {
        multiSelectListPreference<E>(getPropertySpKeyName(property), block)
    }

    inline fun seekBarPreference(
        property: KProperty0<Int>,
        block: SeekBarPreference.() -> Unit
    ): Builder = applyBuilder {
        seekBarPreference(getPropertySpKeyName(property), block)
    }

    inline fun switchPreference(
        property: KProperty0<Boolean>,
        block: SwitchPreference.() -> Unit
    ): Builder = applyBuilder {
        switchPreference(getPropertySpKeyName(property), block)
    }
    //</editor-fold>
}
