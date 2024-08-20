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

/**
 * 使用 [dataStore] 字段并通过 [PreferenceManager.setPreferenceDataStore] 设置来存储值
 *
 * @author chenf()
 * @date 2024-08-13 18:54
 */
class DataStorePreferenceGroupBuilder<SpSaver : Any>(
    context: Context,
    preferenceGroup: PreferenceGroup,
    val dataStore: DataStoreDataStore<SpSaver>,
) : AbsPreferenceGroupBuilder<DataStorePreferenceGroupBuilder<SpSaver>>(context, preferenceGroup) {

    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    override fun createInstance(): DataStorePreferenceGroupBuilder<SpSaver> {
        return DataStorePreferenceGroupBuilder(context, preferenceGroup, dataStore)
    }

    //<editor-fold desc="Field来获取字段名" defaultstate="collapsed">
    inline fun checkBoxPreference(
        property: DataStoreDataStore.Field<SpSaver, Boolean>,
        block: CheckBoxPreference.() -> Unit
    ): DataStorePreferenceGroupBuilder<SpSaver> = applyBuilder {
        require(property in dataStore) {
            "property ${property.name} must in dataStore"
        }
        checkBoxPreference(property.name, block)
    }

    inline fun <reified E> dropDownPreference(
        property: DataStoreDataStore.Field<SpSaver, E>,
        block: DropDownPreference.() -> Unit
    ): DataStorePreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed = applyBuilder {
        require(property in dataStore) {
            "property ${property.name} must in dataStore"
        }
        dropDownPreference<E>(property.name, block)
    }

    inline fun editTextPreference(
        property: DataStoreDataStore.Field<SpSaver, String?>,
        block: EditTextPreference.() -> Unit
    ): DataStorePreferenceGroupBuilder<SpSaver> = applyBuilder {
        require(property in dataStore) {
            "property ${property.name} must in dataStore"
        }
        editTextPreference(property.name, block)
    }

    inline fun <reified E> listPreference(
        property: DataStoreDataStore.Field<SpSaver, E>,
        block: ListPreference.() -> Unit
    ): DataStorePreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed = applyBuilder {
        require(property in dataStore) {
            "property ${property.name} must in dataStore"
        }
        listPreference<E>(property.name, block)
    }

    inline fun <reified E> multiSelectListPreference(
        property: DataStoreDataStore.Field<SpSaver, Set<E>>,
        block: MultiSelectListPreference.() -> Unit
    ): DataStorePreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed = applyBuilder {
        require(property in dataStore) {
            "property ${property.name} must in dataStore"
        }
        multiSelectListPreference<E>(property.name, block)
    }

    inline fun seekBarPreference(
        property: DataStoreDataStore.Field<SpSaver, Int>,
        block: SeekBarPreference.() -> Unit
    ): DataStorePreferenceGroupBuilder<SpSaver> = applyBuilder {
        require(property in dataStore) {
            "property ${property.name} must in dataStore"
        }
        seekBarPreference(property.name, block)
    }

    inline fun switchPreference(
        property: DataStoreDataStore.Field<SpSaver, Boolean>,
        block: SwitchPreference.() -> Unit
    ): DataStorePreferenceGroupBuilder<SpSaver> = applyBuilder {
        require(property in dataStore) {
            "property ${property.name} must in dataStore"
        }
        switchPreference(property.name, block)
    }
    //</editor-fold>

    companion object {
        inline fun <T : Any> PreferenceFragmentCompat.buildPreferenceScreen(
            dataStore: DataStoreDataStore<T>,
            builder: DataStorePreferenceGroupBuilder<T>.() -> Unit
        ): PreferenceScreen {
            return preferenceManager.createPreferenceScreen(requireContext()).apply {
                DataStorePreferenceGroupBuilder(
                    requireContext(), this, dataStore
                ).builder()
            }
        }
    }
}
