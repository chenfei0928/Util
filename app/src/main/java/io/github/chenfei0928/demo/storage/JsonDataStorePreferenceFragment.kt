package io.github.chenfei0928.demo.storage

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import io.github.chenfei0928.concurrent.coroutines.coroutineScope
import io.github.chenfei0928.demo.MainActivity.Companion.jsonDataStore
import io.github.chenfei0928.demo.bean.JsonBean
import io.github.chenfei0928.os.Debug
import io.github.chenfei0928.preference.DataStoreFieldAccessorCache
import io.github.chenfei0928.preference.FieldAccessorPreferenceGroupBuilder.Companion.buildPreferenceScreen
import io.github.chenfei0928.preference.base.DataCopyClassFieldAccessor.Companion.cacheCopyFunc
import io.github.chenfei0928.preference.base.DataCopyClassFieldAccessor.Companion.property
import io.github.chenfei0928.preference.bindEnum

/**
 * @author chenf()
 * @date 2024-07-29 11:15
 */
class JsonDataStorePreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(
        savedInstanceState: Bundle?, rootKey: String?
    ) = Debug.countTime(TAG, "json") {
        val jsonDataStore = requireContext().jsonDataStore
        val dataStore: DataStoreFieldAccessorCache<JsonBean> =
            DataStoreFieldAccessorCache(coroutineScope, jsonDataStore)
        preferenceManager.preferenceDataStore = dataStore.preferenceDataStore
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
                bindEnum()
            }
            multiSelectListPreference<JsonBean.JsonEnum>(dataStore.property(JsonBean::enums)) {
                title = "enumList"
                bindEnum()
            }
        }
    }

    companion object {
        private const val TAG = "JsonPreferenceFragment"
    }
}
