package io.github.chenfei0928.demo.storage

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.postDelayed
import androidx.preference.PreferenceFragmentCompat
import io.github.chenfei0928.content.sp.saver.registerOnSpPropertyChangeListener
import io.github.chenfei0928.content.sp.saver.toLiveData
import io.github.chenfei0928.demo.bean.JsonBean
import io.github.chenfei0928.demo.bean.Test
import io.github.chenfei0928.os.Debug
import io.github.chenfei0928.os.safeHandler
import io.github.chenfei0928.preference.base.bindEnum
import io.github.chenfei0928.preference.sp.SpSaverPreferenceGroupBuilder.Companion.buildPreferenceScreen
import kotlin.random.Random

/**
 * @author chenf()
 * @date 2024-12-20 11:42
 */
class SpSaverPreferenceFragment : PreferenceFragmentCompat() {
    private val spSaver by lazy { TestSpSaver(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        spSaver.toLiveData(spSaver::int).observe(viewLifecycleOwner) {
            Log.v(TAG, "onCreate: spSaver int newValue is $it")
        }
        spSaver.registerOnSpPropertyChangeListener(viewLifecycleOwner, spSaver::enum) {
            Log.v(TAG, "onCreate: spSaver enum newValue is $it")
        }
        spSaver.registerOnSpPropertyChangeListener(viewLifecycleOwner) {
            Log.v(TAG, "onCreate: spSaver onSpPropertyChange $it")
        }
        safeHandler.postDelayed(100L) {
            Log.i(TAG, "onViewCreated: set obj, before is $spSaver")
            spSaver.intArray = intArrayOf(Random.nextInt(), Random.nextInt())
            spSaver.json = JsonBean.InnerJsonBean(Random.nextBoolean())
            spSaver.test = Test.newBuilder().setInt(Random.nextInt()).build()
            spSaver.apply()
        }
    }

    override fun onCreatePreferences(
        savedInstanceState: Bundle?, rootKey: String?
    ) = Debug.countTime(TAG, "spSaver") {
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
