package io.github.chenfei0928.preference

import android.content.SharedPreferences
import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceHelper
import androidx.preference.PreferenceScreen
import io.github.chenfei0928.content.sp.saver.BaseMmkvSaver
import io.github.chenfei0928.preference.sp.SpSaverFieldAccessor

/**
 * 可用于 [SharedPreferences.registerOnSharedPreferenceChangeListener] 来注册sp变化监听
 * 或 [BaseMmkvSaver.anyPropertySetCallback] 来注册mmkv变化监听，
 * 来自动向 [preferenceScreen] 刷新关联项
 *
 * @author chenf()
 * @date 2025-02-14 17:24
 */
class PropertyNotifyUiChangeCallback(
    private val preferenceScreen: PreferenceScreen
) : (Pair<SpSaverFieldAccessor.Field<*, *>, *>) -> Unit,
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun invoke(pair: Pair<SpSaverFieldAccessor.Field<*, *>, *>) {
        preferenceScreen.findPreference<Preference>(pair.first.pdsKey)
            ?.let(PreferenceHelper::notifyValueChanged)
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences, key: String?
    ) {
        if (key != null) {
            preferenceScreen.findPreference<Preference>(key)
                ?.let(PreferenceHelper::notifyValueChanged)
        } else {
            preferenceScreen.forEach { p ->
                if (p.key == key) {
                    PreferenceHelper.notifyValueChanged(p)
                }
            }
        }
    }

    private fun PreferenceGroup.forEach(block: (Preference) -> Unit) {
        for (i in 0 until preferenceCount) {
            val p = getPreference(i)
            block(p)
            if (p is PreferenceGroup) {
                p.forEach(block)
            }
        }
    }
}
