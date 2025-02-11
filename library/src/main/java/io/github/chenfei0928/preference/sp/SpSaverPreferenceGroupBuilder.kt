package io.github.chenfei0928.preference.sp

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceScreen
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.preference.base.AbsPreferenceGroupBuilder1
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

/**
 * PreferenceScreen的构建器
 *
 * 通过 [spSaverDataStore] 与 [SpSaverPreferenceDataStore] 来存储值，
 * 要求实例化 [SpSaverPreferenceDataStore] 并设置给 [PreferenceManager.setPreferenceDataStore]
 *
 * @param spSaverDataStore 传入用于调用 [getPropertyKey] 来获取字段名
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-01-26 15:02
 */
class SpSaverPreferenceGroupBuilder<SpSaver : AbsSpSaver<SpSaver, *, *>>(
    context: Context,
    private val spSaverDataStore: SpSaverPreferenceDataStore<SpSaver>,
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
        return spSaverDataStore.findPdsKeyByPropertyOrThrow(property)
    }

    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    override fun SpSaver.getPropertyKey(property: KProperty1<SpSaver, *>): String {
        return dataStore.findPdsKeyByPropertyOrThrow(property)
    }

    companion object {
        inline fun <SpSaver : AbsSpSaver<SpSaver, *, *>> PreferenceFragmentCompat.buildPreferenceScreen(
            dataStore: SpSaverPreferenceDataStore<SpSaver>,
            builder: SpSaverPreferenceGroupBuilder<SpSaver>.() -> Unit
        ): PreferenceScreen = preferenceManager.createPreferenceScreen(requireContext()).also {
            SpSaverPreferenceGroupBuilder(requireContext(), dataStore, it).builder()
        }
    }
}
