package io.github.chenfei0928.preference

import android.content.Context
import androidx.preference.CheckBoxPreference
import androidx.preference.DropDownPreference
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceScreen
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import io.github.chenfei0928.content.sp.AbsSpSaver
import io.github.chenfei0928.content.sp.delegate.getPropertySpKeyName
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

/**
 * PreferenceScreen的构建器
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-01-26 15:02
 */
class PreferenceGroupBuilder<SpSaver : AbsSpSaver>(
    val context: Context,
    val spSaver: SpSaver,
    val preferenceGroup: PreferenceGroup
) {

    inline fun <P : Preference> preference(
        preference: P,
        block: P.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        preference.apply(block).apply { preferenceGroup.addPreference(this) }
        return this
    }

    inline fun preference(block: Preference.() -> Unit): PreferenceGroupBuilder<SpSaver> {
        return preference(Preference(context), block)
    }

    inline fun preferenceCategory(
        block: PreferenceCategory.() -> Unit,
        builder: PreferenceGroupBuilder<SpSaver>.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        PreferenceCategory(context).apply(block)
            .apply { preferenceGroup.addPreference(this) }
            .apply { PreferenceGroupBuilder(context, spSaver, this).apply(builder) }
        return this
    }

    inline fun preferenceCategory(
        title: String,
        builder: PreferenceGroupBuilder<SpSaver>.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        return preferenceCategory({
            this.title = title
        }, builder)
    }

    //<editor-fold desc="直接提供字段名" defaultstate="collapsed">
    inline fun checkBoxPreference(
        key: String,
        block: CheckBoxPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        return preference(CheckBoxPreference(context).apply {
            this.key = key
        }, block)
    }

    inline fun <reified E> dropDownPreference(
        key: String,
        block: DropDownPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed {
        return preference(DropDownPreference(context).apply {
            this.key = key
            bindEnum(enumValues<E>())
        }, block)
    }

    inline fun editTextPreference(
        key: String,
        block: EditTextPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        return preference(EditTextPreference(context).apply {
            this.key = key
        }, block)
    }

    inline fun <reified E> listPreference(
        key: String,
        block: ListPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed {
        return preference(ListPreference(context).apply {
            this.key = key
            bindEnum(enumValues<E>())
        }, block)
    }

    inline fun <reified E> multiSelectListPreference(
        key: String,
        block: MultiSelectListPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed {
        return preference(MultiSelectListPreference(context).apply {
            this.key = key
            bindEnum(enumValues<E>())
        }, block)
    }

    inline fun seekBarPreference(
        key: String,
        block: SeekBarPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        return preference(SeekBarPreference(context).apply {
            this.key = key
        }, block)
    }

    inline fun switchPreference(
        key: String,
        block: SwitchPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        return preference(SwitchPreference(context).apply {
            this.key = key
        }, block)
    }
    //</editor-fold>

    //<editor-fold desc="KProperty0来获取字段名" defaultstate="collapsed">
    inline fun checkBoxPreference(
        property: KProperty0<Boolean>,
        block: CheckBoxPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        return checkBoxPreference(spSaver.getPropertySpKeyName(property), block)
    }

    inline fun <reified E> dropDownPreference(
        property: KProperty0<E>,
        block: DropDownPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed {
        return dropDownPreference<E>(spSaver.getPropertySpKeyName(property), block)
    }

    inline fun editTextPreference(
        property: KProperty0<String?>,
        block: EditTextPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        return editTextPreference(spSaver.getPropertySpKeyName(property), block)
    }

    inline fun <reified E> listPreference(
        property: KProperty0<E>,
        block: ListPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed {
        return listPreference<E>(spSaver.getPropertySpKeyName(property), block)
    }

    inline fun <reified E> multiSelectListPreference(
        property: KProperty0<Set<E>>,
        block: MultiSelectListPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed {
        return multiSelectListPreference<E>(spSaver.getPropertySpKeyName(property), block)
    }

    inline fun seekBarPreference(
        property: KProperty0<Int>,
        block: SeekBarPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        return seekBarPreference(spSaver.getPropertySpKeyName(property), block)
    }

    inline fun switchPreference(
        property: KProperty0<Boolean>,
        block: SwitchPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        return switchPreference(spSaver.getPropertySpKeyName(property), block)
    }
    //</editor-fold>

    //<editor-fold desc="KProperty1来获取字段名" defaultstate="collapsed">
    inline fun checkBoxPreference(
        property: KProperty1<SpSaver, Boolean>,
        block: CheckBoxPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        return checkBoxPreference(spSaver.getPropertySpKeyName(property), block)
    }

    inline fun <reified E> dropDownPreference(
        property: KProperty1<SpSaver, E>,
        block: DropDownPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed {
        return dropDownPreference<E>(spSaver.getPropertySpKeyName(property), block)
    }

    inline fun editTextPreference(
        property: KProperty1<SpSaver, String?>,
        block: EditTextPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        return editTextPreference(spSaver.getPropertySpKeyName(property), block)
    }

    inline fun <reified E> listPreference(
        property: KProperty1<SpSaver, E>,
        block: ListPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed {
        return listPreference<E>(spSaver.getPropertySpKeyName(property), block)
    }

    inline fun <reified E> multiSelectListPreference(
        property: KProperty1<SpSaver, Set<E>>,
        block: MultiSelectListPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed {
        return multiSelectListPreference<E>(spSaver.getPropertySpKeyName(property), block)
    }

    inline fun seekBarPreference(
        property: KProperty1<SpSaver, Int>,
        block: SeekBarPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        return seekBarPreference(spSaver.getPropertySpKeyName(property), block)
    }

    inline fun switchPreference(
        property: KProperty1<SpSaver, Boolean>,
        block: SwitchPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        return switchPreference(spSaver.getPropertySpKeyName(property), block)
    }
    //</editor-fold>
}

inline fun <SpSaver : AbsSpSaver> PreferenceFragmentCompat.buildPreferenceScreen(
    spSaver: SpSaver,
    builder: PreferenceGroupBuilder<SpSaver>.() -> Unit
): PreferenceScreen {
    return preferenceManager.createPreferenceScreen(requireContext())
        .apply { PreferenceGroupBuilder(requireContext(), spSaver, this).builder() }
}
