package io.github.chenfei0928.preference.base

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.annotation.ReturnThis
import androidx.preference.CheckBoxPreference
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceGroup
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import io.github.chenfei0928.preference.EnumDropDownPreference
import io.github.chenfei0928.preference.EnumListPreference
import io.github.chenfei0928.preference.EnumMultiSelectListPreference

/**
 * PreferenceScreen的构建器
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-01-26 15:02
 */
@Suppress("TooManyFunctions")
abstract class AbsPreferenceGroupBuilder<Builder>(
    val context: Context,
    val preferenceGroup: PreferenceGroup
) {
    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    abstract fun createInstance(): Builder

    @ReturnThis
    @Suppress("UNCHECKED_CAST")
    inline fun applyBuilder(block: Builder.() -> Unit): Builder {
        block(this@AbsPreferenceGroupBuilder as Builder)
        return this
    }

    inline fun <P : Preference> preference(
        preference: P,
        block: P.() -> Unit
    ): Builder = applyBuilder {
        preference.apply(block).apply { preferenceGroup.addPreference(this) }
    }

    inline fun preference(block: Preference.() -> Unit): Builder = applyBuilder {
        preference(Preference(context), block)
    }

    //<editor-fold desc="分类栏" defaultstatus="collapsed">
    inline fun preferenceCategory(
        block: PreferenceCategory.() -> Unit,
        builder: Builder.() -> Unit
    ): Builder = applyBuilder {
        PreferenceCategory(context).apply(block)
            .apply { preferenceGroup.addPreference(this) }
            .apply { createInstance().apply(builder) }
    }

    inline fun preferenceCategory(
        title: String,
        builder: Builder.() -> Unit
    ): Builder = applyBuilder {
        preferenceCategory({
            this.title = title
        }, builder)
    }
    //</editor-fold>

    //<editor-fold desc="直接提供字段名" defaultstate="collapsed">
    inline fun checkBoxPreference(
        key: String,
        block: CheckBoxPreference.() -> Unit
    ): Builder = applyBuilder {
        preference(CheckBoxPreference(context).apply {
            this.key = key
        }, block)
    }

    inline fun <reified E : Enum<E>> dropDownPreference(
        key: String,
        block: EnumDropDownPreference<E>.() -> Unit,
    ): Builder = applyBuilder {
        preference(EnumDropDownPreference<E>(context).apply {
            this.key = key
//            bindEnum(enumValues<E>())
        }, block)
    }

    inline fun editTextPreference(
        key: String,
        block: EditTextPreference.() -> Unit
    ): Builder = applyBuilder {
        preference(EditTextPreference(context).apply {
            this.key = key
        }, block)
    }

    inline fun <reified E : Enum<E>> listPreference(
        key: String,
        block: EnumListPreference<E>.() -> Unit
    ): Builder = applyBuilder {
        preference(EnumListPreference<E>(context).apply {
            this.key = key
//            bindEnum(enumValues<E>())
        }, block)
    }

    inline fun <reified E : Enum<E>> multiSelectListPreference(
        key: String,
        block: EnumMultiSelectListPreference<E>.() -> Unit
    ): Builder = applyBuilder {
        preference(EnumMultiSelectListPreference<E>(context).apply {
            this.key = key
//            bindEnum(enumValues<E>())
        }, block)
    }

    inline fun seekBarPreference(
        key: String,
        block: SeekBarPreference.() -> Unit
    ): Builder = applyBuilder {
        preference(SeekBarPreference(context).apply {
            this.key = key
        }, block)
    }

    inline fun switchPreference(
        key: String,
        block: SwitchPreference.() -> Unit
    ): Builder = applyBuilder {
        preference(SwitchPreference(context).apply {
            this.key = key
        }, block)
    }
    //</editor-fold>
}
