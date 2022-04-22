package io.github.chenfei0928.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.*
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

    //<editor-fold defaultstate="collapsed" desc="默认值检查">
    inline fun <T> checkDefaultValue(
        name: String,
        block: () -> T?,
        action: SharedPreferences.Editor.(T) -> Unit
    ) = preferenceGroup.sharedPreferences.run {
        if (contains(name)) {
            return
        }
        val t = block()
            ?: return@run
        edit {
            action(t)
        }
    }

    inline fun checkDefaultBooleanValue(name: String, block: () -> Boolean) =
        checkDefaultValue(name, block) {
            putBoolean(name, it)
        }

    inline fun checkDefaultStringValue(name: String, block: () -> String?) =
        checkDefaultValue(name, block) {
            putString(name, it)
        }

    inline fun checkDefaultIntValue(name: String, block: () -> Int) =
        checkDefaultValue(name, block) {
            putInt(name, it)
        }

    inline fun <reified E> checkDefaultEnumValue(
        name: String, block: () -> E?
    ) where E : Enum<E>, E : VisibleNamed = checkDefaultValue(name, block) {
        putString(name, it.name)
    }

    inline fun <reified E> checkDefaultEnumSetValue(
        name: String, block: () -> Set<E>
    ) where E : Enum<E>, E : VisibleNamed = checkDefaultValue(name, block) {
        putStringSet(name, it.mapTo(mutableSetOf(), Enum<E>::name))
    }
    //</editor-fold>

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
        val name = spSaver.getPropertySpKeyName(property)
        checkDefaultBooleanValue(name) { property.get() }
        return checkBoxPreference(name, block)
    }

    inline fun <reified E> dropDownPreference(
        property: KProperty0<E>,
        block: DropDownPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed {
        val name = spSaver.getPropertySpKeyName(property)
        checkDefaultEnumValue(name) { property.get() }
        return dropDownPreference<E>(name, block)
    }

    inline fun editTextPreference(
        property: KProperty0<String?>,
        block: EditTextPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        val name = spSaver.getPropertySpKeyName(property)
        checkDefaultStringValue(name) { property.get() }
        return editTextPreference(name, block)
    }

    inline fun <reified E> listPreference(
        property: KProperty0<E>,
        block: ListPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed {
        val name = spSaver.getPropertySpKeyName(property)
        checkDefaultEnumValue(name) { property.get() }
        return listPreference<E>(name, block)
    }

    inline fun <reified E> multiSelectListPreference(
        property: KProperty0<Set<E>>,
        block: MultiSelectListPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed {
        val name = spSaver.getPropertySpKeyName(property)
        checkDefaultEnumSetValue(name) { property.get() }
        return multiSelectListPreference<E>(name, block)
    }

    inline fun seekBarPreference(
        property: KProperty0<Int>,
        block: SeekBarPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        val name = spSaver.getPropertySpKeyName(property)
        checkDefaultIntValue(name) { property.get() }
        return seekBarPreference(name, block)
    }

    inline fun switchPreference(
        property: KProperty0<Boolean>,
        block: SwitchPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        val name = spSaver.getPropertySpKeyName(property)
        checkDefaultBooleanValue(name) { property.get() }
        return switchPreference(name, block)
    }
    //</editor-fold>

    //<editor-fold desc="KProperty1来获取字段名" defaultstate="collapsed">
    inline fun checkBoxPreference(
        property: KProperty1<SpSaver, Boolean>,
        block: CheckBoxPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        val name = spSaver.getPropertySpKeyName(property)
        checkDefaultBooleanValue(name) { property.get(spSaver) }
        return checkBoxPreference(name, block)
    }

    inline fun <reified E> dropDownPreference(
        property: KProperty1<SpSaver, E>,
        block: DropDownPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed {
        val name = spSaver.getPropertySpKeyName(property)
        checkDefaultEnumValue(name) { property.get(spSaver) }
        return dropDownPreference<E>(name, block)
    }

    inline fun editTextPreference(
        property: KProperty1<SpSaver, String?>,
        block: EditTextPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        val name = spSaver.getPropertySpKeyName(property)
        checkDefaultStringValue(name) { property.get(spSaver) }
        return editTextPreference(name, block)
    }

    inline fun <reified E> listPreference(
        property: KProperty1<SpSaver, E>,
        block: ListPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed {
        val name = spSaver.getPropertySpKeyName(property)
        checkDefaultEnumValue(name) { property.get(spSaver) }
        return listPreference<E>(name, block)
    }

    inline fun <reified E> multiSelectListPreference(
        property: KProperty1<SpSaver, Set<E>>,
        block: MultiSelectListPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> where E : Enum<E>, E : VisibleNamed {
        val name = spSaver.getPropertySpKeyName(property)
        checkDefaultEnumSetValue(name) { property.get(spSaver) }
        return multiSelectListPreference<E>(name, block)
    }

    inline fun seekBarPreference(
        property: KProperty1<SpSaver, Int>,
        block: SeekBarPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        val name = spSaver.getPropertySpKeyName(property)
        checkDefaultIntValue(name) { property.get(spSaver) }
        return seekBarPreference(name, block)
    }

    inline fun switchPreference(
        property: KProperty1<SpSaver, Boolean>,
        block: SwitchPreference.() -> Unit
    ): PreferenceGroupBuilder<SpSaver> {
        val name = spSaver.getPropertySpKeyName(property)
        checkDefaultBooleanValue(name) { property.get(spSaver) }
        return switchPreference(name, block)
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
