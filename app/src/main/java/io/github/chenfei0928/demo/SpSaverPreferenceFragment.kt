package io.github.chenfei0928.demo

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import io.github.chenfei0928.preference.bindEnum
import io.github.chenfei0928.preference.sp.SpSaverPreferenceGroupBuilder.Companion.buildPreferenceScreen

/**
 * @author chenf()
 * @date 2024-12-20 11:42
 */
class SpSaverPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val spSaver = TestSpSaver(requireContext())
        preferenceManager.preferenceDataStore = spSaver.dataStore
        preferenceScreen = buildPreferenceScreen<TestSpSaver>(spSaver.dataStore) {
            checkBoxPreference(TestSpSaver::boolean) {
                title = "boolean"
            }
            editTextPreference(TestSpSaver::string) {
                title = "string"
            }
            seekBarPreference(TestSpSaver::int) {
                title = "int"
            }
            dropDownPreference<JsonEnum>(TestSpSaver::enum) {
                title = "enum"
                bindEnum<JsonEnum> { it.name }
            }
            multiSelectListPreference<JsonEnum>(TestSpSaver::enums) {
                title = "enumList"
                bindEnum<JsonEnum> { it.name }
            }
        }
    }

    companion object {
        private const val TAG = "SpSaverPreferenceFragme"
    }
}
