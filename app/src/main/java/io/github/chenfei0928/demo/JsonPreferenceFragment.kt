package io.github.chenfei0928.demo

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import io.github.chenfei0928.concurrent.coroutines.coroutineScope
import io.github.chenfei0928.demo.MainActivity.Companion.jsonDataStore
import io.github.chenfei0928.os.Debug
import io.github.chenfei0928.preference.bindEnum
import io.github.chenfei0928.preference.datastore.DataStorePreferenceDataStore
import io.github.chenfei0928.preference.datastore.DataStorePreferenceGroupBuilder.Companion.buildPreferenceScreen
import io.github.chenfei0928.preference.datastore.FieldAccessorHelper.Companion.cacheCopyFunc
import io.github.chenfei0928.preference.datastore.FieldAccessorHelper.Companion.property

/**
 * @author chenf()
 * @date 2024-07-29 11:15
 */
class JsonPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(
        savedInstanceState: Bundle?, rootKey: String?
    ) = Debug.countTime(TAG, "json") {
        val jsonDataStore = requireContext().jsonDataStore
        val dataStore: DataStorePreferenceDataStore<JsonBean> =
            DataStorePreferenceDataStore(coroutineScope, jsonDataStore)
        preferenceManager.preferenceDataStore = dataStore
        // 缓存data class copy方法
        dataStore.cacheCopyFunc(JsonBean::class, JsonBean::copy)
        dataStore.cacheCopyFunc<JsonBean.InnerJsonBean>(JsonBean.InnerJsonBean::copy)
        preferenceScreen = buildPreferenceScreen(dataStore) {
            // 字段引用
            checkBoxPreference(dataStore.property(JsonBean::boolean)) {
                title = "boolean"
            }
            // 二层字段引用
            checkBoxPreference(
                dataStore.property(JsonBean::inner, JsonBean.InnerJsonBean::boolean)
            ) {
                title = "innerBoolean"
            }
            editTextPreference(dataStore.property(JsonBean::string)) {
                title = "string"
            }
            seekBarPreference(dataStore.property(JsonBean::int)) {
                title = "int"
            }
            dropDownPreference<JsonBean.JsonEnum>(dataStore.property(JsonBean::enum)) {
                title = "enum"
                bindEnum<JsonBean.JsonEnum> { it.name }
            }
            multiSelectListPreference<JsonBean.JsonEnum>(dataStore.property(JsonBean::enums)) {
                title = "enumList"
                bindEnum<JsonBean.JsonEnum> { it.name }
            }
        }
    }

    companion object {
        private const val TAG = "JsonPreferenceFragment"
    }
}
