package io.github.chenfei0928.preference

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.preference.CheckBoxPreference
import androidx.preference.DropDownPreference
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceScreen
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import io.github.chenfei0928.preference.base.AbsPreferenceGroupBuilder
import io.github.chenfei0928.preference.base.FieldAccessor

/**
 * 使用 [fieldAccessor] 字段并通过 [PreferenceManager.setPreferenceDataStore] 设置来存储值
 *
 * @author chenf()
 * @date 2024-08-13 18:54
 */
class FieldAccessorPreferenceGroupBuilder<T : Any>
constructor(
    context: Context,
    preferenceGroup: PreferenceGroup,
    val fieldAccessor: FieldAccessor<T>,
) : AbsPreferenceGroupBuilder<FieldAccessorPreferenceGroupBuilder<T>>(context, preferenceGroup) {

    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    override fun createInstance(): FieldAccessorPreferenceGroupBuilder<T> {
        return FieldAccessorPreferenceGroupBuilder(context, preferenceGroup, fieldAccessor)
    }

    //<editor-fold desc="Field来获取字段名" defaultstate="collapsed">
    inline fun checkBoxPreference(
        property: FieldAccessor.Field<T, Boolean>,
        block: CheckBoxPreference.() -> Unit
    ): FieldAccessorPreferenceGroupBuilder<T> = applyBuilder {
        require(property in fieldAccessor) {
            "property ${property.pdsKey} must in dataStore"
        }
        checkBoxPreference(property.pdsKey, block)
    }

    inline fun <reified E : Enum<E>> dropDownPreference(
        property: FieldAccessor.Field<T, E>,
        block: DropDownPreference.() -> Unit
    ): FieldAccessorPreferenceGroupBuilder<T> = applyBuilder {
        require(property in fieldAccessor) {
            "property ${property.pdsKey} must in dataStore"
        }
        dropDownPreference<E>(property.pdsKey, block)
    }

    inline fun editTextPreference(
        property: FieldAccessor.Field<T, String>,
        block: EditTextPreference.() -> Unit
    ): FieldAccessorPreferenceGroupBuilder<T> = applyBuilder {
        require(property in fieldAccessor) {
            "property ${property.pdsKey} must in dataStore"
        }
        editTextPreference(property.pdsKey, block)
    }

    inline fun <reified E : Enum<E>> listPreference(
        property: FieldAccessor.Field<T, E>,
        block: ListPreference.() -> Unit
    ): FieldAccessorPreferenceGroupBuilder<T> = applyBuilder {
        require(property in fieldAccessor) {
            "property ${property.pdsKey} must in dataStore"
        }
        listPreference<E>(property.pdsKey, block)
    }

    inline fun <reified E : Enum<E>> multiSelectListPreference(
        property: FieldAccessor.Field<T, Set<E>>,
        block: MultiSelectListPreference.() -> Unit
    ): FieldAccessorPreferenceGroupBuilder<T> = applyBuilder {
        require(property in fieldAccessor) {
            "property ${property.pdsKey} must in dataStore"
        }
        multiSelectListPreference<E>(property.pdsKey, block)
    }

    inline fun seekBarPreference(
        property: FieldAccessor.Field<T, Int>,
        block: SeekBarPreference.() -> Unit
    ): FieldAccessorPreferenceGroupBuilder<T> = applyBuilder {
        require(property in fieldAccessor) {
            "property ${property.pdsKey} must in dataStore"
        }
        seekBarPreference(property.pdsKey, block)
    }

    inline fun switchPreference(
        property: FieldAccessor.Field<T, Boolean>,
        block: SwitchPreference.() -> Unit
    ): FieldAccessorPreferenceGroupBuilder<T> = applyBuilder {
        require(property in fieldAccessor) {
            "property ${property.pdsKey} must in dataStore"
        }
        switchPreference(property.pdsKey, block)
    }
    //</editor-fold>

    companion object {
        inline fun <T : Any> PreferenceFragmentCompat.buildPreferenceScreen(
            fieldAccessor: FieldAccessor<T>,
            builder: FieldAccessorPreferenceGroupBuilder<T>.() -> Unit
        ): PreferenceScreen = preferenceManager.createPreferenceScreen(requireContext()).also {
            FieldAccessorPreferenceGroupBuilder(requireContext(), it, fieldAccessor).builder()
        }
    }
}
