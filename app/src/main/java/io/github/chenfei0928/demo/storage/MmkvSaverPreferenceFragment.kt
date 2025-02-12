package io.github.chenfei0928.demo.storage

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.postDelayed
import androidx.preference.PreferenceFragmentCompat
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.demo.bean.JsonBean
import io.github.chenfei0928.demo.bean.Test
import io.github.chenfei0928.lang.toStr
import io.github.chenfei0928.os.Debug
import io.github.chenfei0928.os.safeHandler
import io.github.chenfei0928.preference.base.FieldAccessor
import io.github.chenfei0928.preference.base.bindEnum
import io.github.chenfei0928.preference.sp.SpSaverPreferenceGroupBuilder.Companion.buildPreferenceScreen
import kotlin.random.Random

/**
 * @author chenf()
 * @date 2024-12-20 11:42
 */
class MmkvSaverPreferenceFragment : PreferenceFragmentCompat() {
    private val spSaver by lazy { TestMmkvSaver }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        spSaver.anyPropertySetCallback.observe(viewLifecycleOwner) {
            Log.v(TAG, buildString {
                append("onCreate: mmkvSaver anyPropertySetCallback ")
                append(it)
                append(' ')
                append(it.second?.toStr())
            })
        }
        spSaver.getPropertyObservable(TestMmkvSaver::int).observe(viewLifecycleOwner) {
            Log.v(TAG, "onCreate: mmkvSaver int newValue is $it")
        }
        safeHandler.postDelayed(100L) {
            Log.i(TAG, "onViewCreated: set obj, before is $spSaver")
            spSaver.intArray = intArrayOf(Random.nextInt(), Random.nextInt())
            spSaver.json = JsonBean.InnerJsonBean(Random.nextBoolean())
            spSaver.test = Test.newBuilder().setInt(Random.nextInt()).build()
            Log.i(TAG, "onViewCreated: set obj, after is $spSaver")
        }
    }

    override fun onCreatePreferences(
        savedInstanceState: Bundle?, rootKey: String?
    ) = Debug.countTime(TAG, "mmkvSaver onCreatePreferences") {
        preferenceManager.preferenceDataStore = spSaver.dataStore
        Log.i(TAG, "onCreatePreferences: buildPreferenceScreen")
        preferenceScreen = buildPreferenceScreen<TestMmkvSaver>(spSaver.dataStore) {
            // sp属性引用
            checkBoxPreference(TestMmkvSaver::boolean) {
                title = "boolean"
            }
            editTextPreference(TestMmkvSaver::string) {
                title = "string"
            }
            seekBarPreference(TestMmkvSaver::int) {
                title = "int"
            }
            dropDownPreference<JsonBean.JsonEnum>(TestMmkvSaver::enum) {
                title = "enum"
                bindEnum<JsonBean.JsonEnum> { it.name }
            }
            multiSelectListPreference<JsonBean.JsonEnum>(TestMmkvSaver::enums) {
                title = "enumList"
                bindEnum<JsonBean.JsonEnum> { it.name }
            }
            // 以下方式可以达到引用sp中结构体类型的字段
            val innerField = if ("json_boolean" in spSaver.dataStore.properties) {
                "json_boolean"
            } else {
                spSaver.dataStore.property(
                    object : FieldAccessor.Field<TestMmkvSaver, Boolean> {
                        override val pdsKey: String = "json_boolean"
                        override val vType: PreferenceType = PreferenceType.Native.BOOLEAN
                        override fun get(data: TestMmkvSaver): Boolean = data.json?.boolean == true
                        override fun set(data: TestMmkvSaver, value: Boolean): TestMmkvSaver {
                            val json = data.json ?: JsonBean.InnerJsonBean()
                            json.boolean = value
                            data.json = json
                            return data
                        }

                        override fun toString(): String = "field($pdsKey $vType)"
                    }
                ).pdsKey
            }
            checkBoxPreference(innerField) {
                title = "json_boolean"
            }
        }
    }

    companion object {
        private const val TAG = "MmkvSaverPreferenceF"
    }
}
