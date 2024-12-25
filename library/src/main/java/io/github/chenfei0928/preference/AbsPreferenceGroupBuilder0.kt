package io.github.chenfei0928.preference

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.preference.CheckBoxPreference
import androidx.preference.DropDownPreference
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceManager
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import kotlin.reflect.KProperty0

/**
 * PreferenceScreen的构建器
 *
 * 通过 [KProperty0] 来存储值，要求实现的 [PreferenceManager.setPreferenceDataStore]
 * 可以实现对所有传入[KProperty0]字段的读写，其字段名由 [getPropertyKey] 获取
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-01-26 15:02
 */
abstract class AbsPreferenceGroupBuilder0<Builder>(
    context: Context, preferenceGroup: PreferenceGroup
) : AbsPreferenceGroupBuilder<Builder>(context, preferenceGroup) {

    //<editor-fold desc="KProperty0来获取字段名" defaultstate="collapsed">
    /**
     * 用于获取[property]的[PreferenceDataStore]序列化保存字段名
     *
     * 由于所有build方法为inline且需要调用该方法，所以此方法需要与build方法访问权限相同
     */
    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    abstract fun getPropertyKey(property: KProperty0<*>): String

    inline fun checkBoxPreference(
        property: KProperty0<Boolean>,
        block: CheckBoxPreference.() -> Unit
    ): Builder = applyBuilder {
        checkBoxPreference(getPropertyKey(property), block)
    }

    inline fun <reified E : Enum<E>> dropDownPreference(
        property: KProperty0<E>,
        block: DropDownPreference.() -> Unit
    ): Builder = applyBuilder {
        dropDownPreference<E>(getPropertyKey(property), block)
    }

    inline fun editTextPreference(
        property: KProperty0<String?>,
        block: EditTextPreference.() -> Unit
    ): Builder = applyBuilder {
        editTextPreference(getPropertyKey(property), block)
    }

    inline fun <reified E : Enum<E>> listPreference(
        property: KProperty0<E>,
        block: ListPreference.() -> Unit
    ): Builder = applyBuilder {
        listPreference<E>(getPropertyKey(property), block)
    }

    inline fun <reified E : Enum<E>> multiSelectListPreference(
        property: KProperty0<Set<E>>,
        block: MultiSelectListPreference.() -> Unit
    ): Builder = applyBuilder {
        multiSelectListPreference<E>(getPropertyKey(property), block)
    }

    inline fun seekBarPreference(
        property: KProperty0<Int>,
        block: SeekBarPreference.() -> Unit
    ): Builder = applyBuilder {
        seekBarPreference(getPropertyKey(property), block)
    }

    inline fun switchPreference(
        property: KProperty0<Boolean>,
        block: SwitchPreference.() -> Unit
    ): Builder = applyBuilder {
        switchPreference(getPropertyKey(property), block)
    }
    //</editor-fold>
}
