package io.github.chenfei0928.preference.sp

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.preference.CheckBoxPreference
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceScreen
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.preference.EnumDropDownPreference
import io.github.chenfei0928.preference.EnumListPreference
import io.github.chenfei0928.preference.EnumMultiSelectListPreference
import io.github.chenfei0928.preference.base.AbsPreferenceGroupBuilder1
import io.github.chenfei0928.preference.base.FieldAccessor
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

/**
 * PreferenceScreen的构建器
 *
 * 通过 [spSaverDataStore] 与 [SpSaverFieldAccessorCache] 来存储值，
 * 要求实例化 [SpSaverFieldAccessorCache] 并设置给 [PreferenceManager.setPreferenceDataStore]
 *
 * @param spSaverDataStore 传入用于调用 [getPropertyKey] 来获取字段名
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-01-26 15:02
 */
class SpSaverPreferenceGroupBuilder<SpSaver : AbsSpSaver<SpSaver, *, *>>(
    context: Context,
    private val spSaverDataStore: SpSaverFieldAccessorCache<SpSaver>,
    preferenceGroup: PreferenceGroup,
) : AbsPreferenceGroupBuilder1<SpSaver, SpSaverPreferenceGroupBuilder<SpSaver>>(
    context, spSaverDataStore.saver, preferenceGroup
) {

    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    override fun createInstance(): SpSaverPreferenceGroupBuilder<SpSaver> {
        return SpSaverPreferenceGroupBuilder(context, spSaverDataStore, preferenceGroup)
    }

    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    override fun getPropertyKey(property: KProperty0<*>): String {
        return spSaverDataStore.findFieldByPropertyOrThrow(property).pdsKey
    }

    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    override fun SpSaver.getPropertyKey(property: KProperty1<SpSaver, *>): String {
        return fieldAccessorCache.findFieldByPropertyOrThrow(property).pdsKey
    }

    //<editor-fold desc="使用Field创建" defaultstate="collapsed">
    inline fun checkBoxPreference(
        field: FieldAccessor.Field<SpSaver, Boolean>,
        block: CheckBoxPreference.() -> Unit
    ): SpSaverPreferenceGroupBuilder<SpSaver> = applyBuilder {
        checkBoxPreference(field.pdsKey, block)
    }

    inline fun <reified E : Enum<E>> dropDownPreference(
        field: FieldAccessor.Field<SpSaver, E>,
        block: EnumDropDownPreference<E>.() -> Unit,
    ): SpSaverPreferenceGroupBuilder<SpSaver> = applyBuilder {
        dropDownPreference(field.pdsKey, block)
    }

    inline fun editTextPreference(
        field: FieldAccessor.Field<SpSaver, String>,
        block: EditTextPreference.() -> Unit
    ): SpSaverPreferenceGroupBuilder<SpSaver> = applyBuilder {
        editTextPreference(field.pdsKey, block)
    }

    inline fun <reified E : Enum<E>> listPreference(
        field: FieldAccessor.Field<SpSaver, E>,
        block: EnumListPreference<E>.() -> Unit
    ): SpSaverPreferenceGroupBuilder<SpSaver> = applyBuilder {
        listPreference(field.pdsKey, block)
    }

    inline fun <reified E : Enum<E>> multiSelectListPreference(
        field: FieldAccessor.Field<SpSaver, E>,
        block: EnumMultiSelectListPreference<E>.() -> Unit
    ): SpSaverPreferenceGroupBuilder<SpSaver> = applyBuilder {
        multiSelectListPreference(field.pdsKey, block)
    }

    inline fun seekBarPreference(
        field: FieldAccessor.Field<SpSaver, Int>,
        block: SeekBarPreference.() -> Unit
    ): SpSaverPreferenceGroupBuilder<SpSaver> = applyBuilder {
        seekBarPreference(field.pdsKey, block)
    }

    inline fun switchPreference(
        field: FieldAccessor.Field<SpSaver, Boolean>,
        block: SwitchPreference.() -> Unit
    ): SpSaverPreferenceGroupBuilder<SpSaver> = applyBuilder {
        switchPreference(field.pdsKey, block)
    }
    //</editor-fold>

    companion object {
        inline fun <SpSaver : AbsSpSaver<SpSaver, *, *>> PreferenceFragmentCompat.buildPreferenceScreen(
            dataStore: SpSaverFieldAccessorCache<SpSaver>,
            builder: SpSaverPreferenceGroupBuilder<SpSaver>.() -> Unit
        ): PreferenceScreen = preferenceManager.createPreferenceScreen(requireContext()).also {
            SpSaverPreferenceGroupBuilder(requireContext(), dataStore, it).builder()
        }
    }
}
