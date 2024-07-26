package io.github.chenfei0928.preference

import android.content.Context
import androidx.annotation.ReturnThis
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

    @ReturnThis
    inline fun <P : Preference> preference(
        preference: P,
        block: P.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        preference.apply(block).apply { preferenceGroup.addPreference(this) }
        return this
    }

    @ReturnThis
    inline fun preference(block: Preference.() -> Unit): PreferenceGroupBuilder<SpSaver> {
        preference(Preference(context), block)
        return this
    }

    @ReturnThis
    inline fun preferenceCategory(
        block: PreferenceCategory.() -> Unit,
        builder: PreferenceGroupBuilder<SpSaver>.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        PreferenceCategory(context).apply(block)
            .apply { preferenceGroup.addPreference(this) }
            .apply { PreferenceGroupBuilder(context, spSaver, this).apply(builder) }
        return this
    }

    @ReturnThis
    inline fun preferenceCategory(
        title: String,
        builder: PreferenceGroupBuilder<SpSaver>.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        preferenceCategory({
            this.title = title
        }, builder)
        return this
    }

    //<editor-fold desc="直接提供字段名" defaultstate="collapsed">
    @ReturnThis
    inline fun checkBoxPreference(
        key: String,
        block: CheckBoxPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        preference(CheckBoxPreference(context).apply {
            this.key = key
        }, block)
        return this
    }

    @ReturnThis
    inline fun <reified E> dropDownPreference(
        key: String,
        block: DropDownPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed {
        preference(DropDownPreference(context).apply {
            this.key = key
            bindEnum(enumValues<E>())
        }, block)
        return this
    }

    @ReturnThis
    inline fun editTextPreference(
        key: String,
        block: EditTextPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        preference(EditTextPreference(context).apply {
            this.key = key
        }, block)
        return this
    }

    @ReturnThis
    inline fun <reified E> listPreference(
        key: String,
        block: ListPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed {
        preference(ListPreference(context).apply {
            this.key = key
            bindEnum(enumValues<E>())
        }, block)
        return this
    }

    @ReturnThis
    inline fun <reified E> multiSelectListPreference(
        key: String,
        block: MultiSelectListPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed {
        preference(MultiSelectListPreference(context).apply {
            this.key = key
            bindEnum(enumValues<E>())
        }, block)
        return this
    }

    @ReturnThis
    inline fun seekBarPreference(
        key: String,
        block: SeekBarPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        preference(SeekBarPreference(context).apply {
            this.key = key
        }, block)
        return this
    }

    @ReturnThis
    inline fun switchPreference(
        key: String,
        block: SwitchPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        preference(SwitchPreference(context).apply {
            this.key = key
        }, block)
        return this
    }
    //</editor-fold>

    //<editor-fold desc="KProperty0来获取字段名" defaultstate="collapsed">
    @ReturnThis
    inline fun checkBoxPreference(
        property: KProperty0<Boolean>,
        block: CheckBoxPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        checkBoxPreference(spSaver.getPropertySpKeyName(property), block)
        return this
    }

    @ReturnThis
    inline fun <reified E> dropDownPreference(
        property: KProperty0<E>,
        block: DropDownPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed {
        dropDownPreference<E>(spSaver.getPropertySpKeyName(property), block)
        return this
    }

    @ReturnThis
    inline fun editTextPreference(
        property: KProperty0<String?>,
        block: EditTextPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        editTextPreference(spSaver.getPropertySpKeyName(property), block)
        return this
    }

    @ReturnThis
    inline fun <reified E> listPreference(
        property: KProperty0<E>,
        block: ListPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed {
        listPreference<E>(spSaver.getPropertySpKeyName(property), block)
        return this
    }

    @ReturnThis
    inline fun <reified E> multiSelectListPreference(
        property: KProperty0<Set<E>>,
        block: MultiSelectListPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed {
        multiSelectListPreference<E>(spSaver.getPropertySpKeyName(property), block)
        return this
    }

    @ReturnThis
    inline fun seekBarPreference(
        property: KProperty0<Int>,
        block: SeekBarPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        seekBarPreference(spSaver.getPropertySpKeyName(property), block)
        return this
    }

    @ReturnThis
    inline fun switchPreference(
        property: KProperty0<Boolean>,
        block: SwitchPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        switchPreference(spSaver.getPropertySpKeyName(property), block)
        return this
    }
    //</editor-fold>

    //<editor-fold desc="KProperty1来获取字段名" defaultstate="collapsed">
    @ReturnThis
    inline fun checkBoxPreference(
        property: KProperty1<SpSaver, Boolean>,
        block: CheckBoxPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        checkBoxPreference(spSaver.getPropertySpKeyName(property), block)
        return this
    }

    @ReturnThis
    inline fun <reified E> dropDownPreference(
        property: KProperty1<SpSaver, E>,
        block: DropDownPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed {
        dropDownPreference<E>(spSaver.getPropertySpKeyName(property), block)
        return this
    }

    @ReturnThis
    inline fun editTextPreference(
        property: KProperty1<SpSaver, String?>,
        block: EditTextPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        editTextPreference(spSaver.getPropertySpKeyName(property), block)
        return this
    }

    @ReturnThis
    inline fun <reified E> listPreference(
        property: KProperty1<SpSaver, E>,
        block: ListPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed {
        listPreference<E>(spSaver.getPropertySpKeyName(property), block)
        return this
    }

    @ReturnThis
    inline fun <reified E> multiSelectListPreference(
        property: KProperty1<SpSaver, Set<E>>,
        block: MultiSelectListPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed {
        multiSelectListPreference<E>(spSaver.getPropertySpKeyName(property), block)
        return this
    }

    @ReturnThis
    inline fun seekBarPreference(
        property: KProperty1<SpSaver, Int>,
        block: SeekBarPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        seekBarPreference(spSaver.getPropertySpKeyName(property), block)
        return this
    }

    @ReturnThis
    inline fun switchPreference(
        property: KProperty1<SpSaver, Boolean>,
        block: SwitchPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        switchPreference(spSaver.getPropertySpKeyName(property), block)
        return this
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
