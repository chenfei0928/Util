package io.github.chenfei0928.demo

import android.os.Bundle
import android.util.Log
import androidx.preference.PreferenceFragmentCompat
import io.github.chenfei0928.os.Debug
import io.github.chenfei0928.preference.base.bindEnum
import io.github.chenfei0928.preference.sp.SpSaverPreferenceGroupBuilder.Companion.buildPreferenceScreen

/**
 * @author chenf()
 * @date 2024-12-20 11:42
 */
class SpSaverPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(
        savedInstanceState: Bundle?, rootKey: String?
    ) = Debug.countTime(TAG, "spSaver") {
        val spSaver = TestSpSaver(requireContext())
        preferenceManager.preferenceDataStore = spSaver.dataStore
        Log.i(TAG, "onCreatePreferences: buildPreferenceScreen")
        preferenceScreen = buildPreferenceScreen<TestSpSaver>(spSaver.dataStore) {
            // sp属性引用
            checkBoxPreference(TestSpSaver::boolean) {
                title = "boolean"
            }
            editTextPreference(TestSpSaver::string) {
                title = "string"
            }
            seekBarPreference(TestSpSaver::int) {
                title = "int"
            }
            dropDownPreference<JsonBean.JsonEnum>(TestSpSaver::enum) {
                title = "enum"
                bindEnum<JsonBean.JsonEnum> { it.name }
            }
            multiSelectListPreference<JsonBean.JsonEnum>(TestSpSaver::enums) {
                title = "enumList"
                bindEnum<JsonBean.JsonEnum> { it.name }
            }
        }
    }

    companion object {
        private const val TAG = "SpSaverPreferenceFragme"
    }
}
