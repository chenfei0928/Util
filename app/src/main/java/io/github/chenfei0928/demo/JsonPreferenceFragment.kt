package io.github.chenfei0928.demo

import android.os.Bundle
import android.util.Log
import androidx.preference.PreferenceFragmentCompat
import io.github.chenfei0928.concurrent.coroutines.coroutineScope
import io.github.chenfei0928.demo.MainActivity.Companion.jsonDataStore
import io.github.chenfei0928.preference.bindEnum
import io.github.chenfei0928.preference.datastore.DataStoreDataStore
import io.github.chenfei0928.preference.datastore.DataStorePreferenceGroupBuilder.Companion.buildPreferenceScreen
import io.github.chenfei0928.preference.datastore.FieldAccessorHelper.Companion.cacheCopyFunc
import io.github.chenfei0928.preference.datastore.FieldAccessorHelper.Companion.property
import kotlinx.coroutines.launch

/**
 * @author chenf()
 * @date 2024-07-29 11:15
 */
class JsonPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val jsonDataStore = requireContext().jsonDataStore
        coroutineScope.launch {
            jsonDataStore.data.collect { json ->
                Log.i(TAG, "onCreatePreferences: $json")
            }
        }
        val dataStore: DataStoreDataStore<JsonBean> =
            DataStoreDataStore(coroutineScope, jsonDataStore)
        preferenceManager.preferenceDataStore = dataStore
        dataStore.cacheCopyFunc(JsonBean::class, JsonBean::copy)
        dataStore.cacheCopyFunc<InnerJsonBean>(InnerJsonBean::copy)
        preferenceScreen = buildPreferenceScreen(dataStore) {
            checkBoxPreference(dataStore.property(JsonBean::boolean)) {
                title = "boolean"
            }
            checkBoxPreference(dataStore.property(JsonBean::inner, InnerJsonBean::boolean)) {
                title = "innerBoolean"
            }
            editTextPreference(dataStore.property(JsonBean::string)) {
                title = "string"
            }
            seekBarPreference(dataStore.property(JsonBean::int)) {
                title = "int"
            }
            dropDownPreference<JsonEnum>(dataStore.property(JsonBean::enum)) {
                title = "enum"
                bindEnum<JsonEnum> { it.name }
            }
            multiSelectListPreference<JsonEnum>(dataStore.property(JsonBean::enums)) {
                title = "enumList"
                bindEnum<JsonEnum> { it.name }
            }
        }
    }

    companion object {
        private const val TAG = "JsonPreferenceFragment"
    }
}
