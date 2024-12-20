package io.github.chenfei0928.preference.datastore

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
import io.github.chenfei0928.preference.AbsPreferenceGroupBuilder
import io.github.chenfei0928.preference.FieldAccessor

/**
 * 使用 [dataStore] 字段并通过 [PreferenceManager.setPreferenceDataStore] 设置来存储值
 *
 * @author chenf()
 * @date 2024-08-13 18:54
 */
class DataStorePreferenceGroupBuilder<T : Any>(
    context: Context,
    preferenceGroup: PreferenceGroup,
    val dataStore: DataStorePreferenceDataStore<T>,
) : AbsPreferenceGroupBuilder<DataStorePreferenceGroupBuilder<T>>(context, preferenceGroup) {

    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    override fun createInstance(): DataStorePreferenceGroupBuilder<T> {
        return DataStorePreferenceGroupBuilder(context, preferenceGroup, dataStore)
    }

    //<editor-fold desc="Field来获取字段名" defaultstate="collapsed">
    inline fun checkBoxPreference(
        property: FieldAccessor.Field<T, Boolean>,
        block: CheckBoxPreference.() -> Unit
    ): DataStorePreferenceGroupBuilder<T> = applyBuilder {
        require(property in dataStore) {
            "property ${property.name} must in dataStore"
        }
        checkBoxPreference(property.name, block)
    }

    inline fun <reified E : Enum<E>> dropDownPreference(
        property: FieldAccessor.Field<T, E>,
        block: DropDownPreference.() -> Unit
    ): DataStorePreferenceGroupBuilder<T> = applyBuilder {
        require(property in dataStore) {
            "property ${property.name} must in dataStore"
        }
        dropDownPreference<E>(property.name, block)
    }

    inline fun editTextPreference(
        property: FieldAccessor.Field<T, String>,
        block: EditTextPreference.() -> Unit
    ): DataStorePreferenceGroupBuilder<T> = applyBuilder {
        require(property in dataStore) {
            "property ${property.name} must in dataStore"
        }
        editTextPreference(property.name, block)
    }

    inline fun <reified E : Enum<E>> listPreference(
        property: FieldAccessor.Field<T, E>,
        block: ListPreference.() -> Unit
    ): DataStorePreferenceGroupBuilder<T> = applyBuilder {
        require(property in dataStore) {
            "property ${property.name} must in dataStore"
        }
        listPreference<E>(property.name, block)
    }

    inline fun <reified E : Enum<E>> multiSelectListPreference(
        property: FieldAccessor.Field<T, Set<E>>,
        block: MultiSelectListPreference.() -> Unit
    ): DataStorePreferenceGroupBuilder<T> = applyBuilder {
        require(property in dataStore) {
            "property ${property.name} must in dataStore"
        }
        multiSelectListPreference<E>(property.name, block)
    }

    inline fun seekBarPreference(
        property: FieldAccessor.Field<T, Int>,
        block: SeekBarPreference.() -> Unit
    ): DataStorePreferenceGroupBuilder<T> = applyBuilder {
        require(property in dataStore) {
            "property ${property.name} must in dataStore"
        }
        seekBarPreference(property.name, block)
    }

    inline fun switchPreference(
        property: FieldAccessor.Field<T, Boolean>,
        block: SwitchPreference.() -> Unit
    ): DataStorePreferenceGroupBuilder<T> = applyBuilder {
        require(property in dataStore) {
            "property ${property.name} must in dataStore"
        }
        switchPreference(property.name, block)
    }
    //</editor-fold>

    companion object {
        inline fun <T : Any> PreferenceFragmentCompat.buildPreferenceScreen(
            dataStore: DataStorePreferenceDataStore<T>,
            builder: DataStorePreferenceGroupBuilder<T>.() -> Unit
        ): PreferenceScreen = preferenceManager.createPreferenceScreen(requireContext()).also {
            DataStorePreferenceGroupBuilder(requireContext(), it, dataStore).builder()
        }
    }
}
