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
import kotlin.reflect.KProperty1

/**
 * PreferenceScreen的构建器
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-01-26 15:02
 */
abstract class AbsPreferenceGroupBuilder1<SpSaver, Builder>(
    context: Context,
    val spSaver: SpSaver,
    preferenceGroup: PreferenceGroup
) : AbsPreferenceGroupBuilder0<Builder>(context, preferenceGroup) {

    //<editor-fold desc="KProperty1来获取字段名" defaultstate="collapsed">
    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    abstract fun SpSaver.getPropertySpKeyName(property: KProperty1<SpSaver, *>): String

    inline fun checkBoxPreference(
        property: KProperty1<SpSaver, Boolean>,
        block: CheckBoxPreference.() -> Unit
    ): Builder = applyBuilder {
        checkBoxPreference(spSaver.getPropertySpKeyName(property), block)
    }

    inline fun <reified E> dropDownPreference(
        property: KProperty1<SpSaver, E>,
        block: DropDownPreference.() -> Unit
    ): Builder where E : Enum<E>, E : VisibleNamed = applyBuilder {
        dropDownPreference<E>(spSaver.getPropertySpKeyName(property), block)
    }

    inline fun editTextPreference(
        property: KProperty1<SpSaver, String?>,
        block: EditTextPreference.() -> Unit
    ): Builder = applyBuilder {
        editTextPreference(spSaver.getPropertySpKeyName(property), block)
    }

    inline fun <reified E> listPreference(
        property: KProperty1<SpSaver, E>,
        block: ListPreference.() -> Unit
    ): Builder where E : Enum<E>, E : VisibleNamed = applyBuilder {
        listPreference<E>(spSaver.getPropertySpKeyName(property), block)
    }

    inline fun <reified E> multiSelectListPreference(
        property: KProperty1<SpSaver, Set<E>>,
        block: MultiSelectListPreference.() -> Unit
    ): Builder where E : Enum<E>, E : VisibleNamed = applyBuilder {
        multiSelectListPreference<E>(spSaver.getPropertySpKeyName(property), block)
    }

    inline fun seekBarPreference(
        property: KProperty1<SpSaver, Int>,
        block: SeekBarPreference.() -> Unit
    ): Builder = applyBuilder {
        seekBarPreference(spSaver.getPropertySpKeyName(property), block)
    }

    inline fun switchPreference(
        property: KProperty1<SpSaver, Boolean>,
        block: SwitchPreference.() -> Unit
    ): Builder = applyBuilder {
        switchPreference(spSaver.getPropertySpKeyName(property), block)
    }
    //</editor-fold>
}
