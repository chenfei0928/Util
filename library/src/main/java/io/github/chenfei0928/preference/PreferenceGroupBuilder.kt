package io.github.chenfei0928.preference

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceScreen
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
    context: Context, spSaver: SpSaver, preferenceGroup: PreferenceGroup
) : AbsPreferenceGroupBuilder1<SpSaver, PreferenceGroupBuilder<SpSaver>>(
    context, spSaver, preferenceGroup
) {

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun createInstance(): PreferenceGroupBuilder<SpSaver> {
        return PreferenceGroupBuilder(context, spSaver, preferenceGroup)
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun getPropertySpKeyName(property: KProperty0<*>): String {
        return spSaver.getPropertySpKeyName(property)
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun SpSaver.getPropertySpKeyName(property: KProperty1<SpSaver, *>): String {
        return getPropertySpKeyName(property)
    }
}

inline fun <SpSaver : AbsSpSaver> PreferenceFragmentCompat.buildPreferenceScreen(
    spSaver: SpSaver,
    builder: PreferenceGroupBuilder<SpSaver>.() -> Unit
): PreferenceScreen {
    return preferenceManager.createPreferenceScreen(requireContext())
        .apply { PreferenceGroupBuilder(requireContext(), spSaver, this).builder() }
}
