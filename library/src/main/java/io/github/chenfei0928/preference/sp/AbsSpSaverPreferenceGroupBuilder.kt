package io.github.chenfei0928.preference.sp

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceScreen
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.getPropertySpKeyName
import io.github.chenfei0928.preference.AbsPreferenceGroupBuilder1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

/**
 * PreferenceScreen的构建器
 *
 * 通过 [spSaver] 与 [SpSaverPreferenceDataStore] 来存储值，
 * 要求实例化 [SpSaverPreferenceDataStore] 并设置给 [PreferenceManager.setPreferenceDataStore]
 *
 * @param spSaver 传入用于调用 [getPropertySpKeyName] 来获取字段名
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-01-26 15:02
 */
class AbsSpSaverPreferenceGroupBuilder<SpSaver : AbsSpSaver>(
    context: Context, spSaver: SpSaver, preferenceGroup: PreferenceGroup
) : AbsPreferenceGroupBuilder1<SpSaver, AbsSpSaverPreferenceGroupBuilder<SpSaver>>(
    context, spSaver, preferenceGroup
) {

    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    override fun createInstance(): AbsSpSaverPreferenceGroupBuilder<SpSaver> {
        return AbsSpSaverPreferenceGroupBuilder(context, spSaver, preferenceGroup)
    }

    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    override fun getPropertySpKeyName(property: KProperty0<*>): String {
        return spSaver.getPropertySpKeyName(property as KProperty<*>)
    }

    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    override fun SpSaver.getPropertySpKeyName(property: KProperty1<SpSaver, *>): String {
        return getPropertySpKeyName(property as KProperty<*>)
    }

    companion object {
        inline fun <SpSaver : AbsSpSaver> PreferenceFragmentCompat.buildPreferenceScreen(
            spSaver: SpSaver,
            builder: AbsSpSaverPreferenceGroupBuilder<SpSaver>.() -> Unit
        ): PreferenceScreen {
            return preferenceManager.createPreferenceScreen(requireContext())
                .apply {
                    AbsSpSaverPreferenceGroupBuilder(
                        requireContext(),
                        spSaver,
                        this
                    ).builder()
                }
        }
    }
}
