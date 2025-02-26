package io.github.chenfei0928.demo.storage

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import io.github.chenfei0928.demo.bean.JsonBean
import io.github.chenfei0928.demo.storage.JsonLocalFileStorage0.Companion.jsonLocalStorage0
import io.github.chenfei0928.os.Debug
import io.github.chenfei0928.preference.FieldAccessorPreferenceGroupBuilder.Companion.buildPreferenceScreen
import io.github.chenfei0928.preference.LocalStoragePreferenceDataStore
import io.github.chenfei0928.preference.base.DataCopyClassFieldAccessor.Companion.cacheCopyFunc
import io.github.chenfei0928.preference.base.DataCopyClassFieldAccessor.Companion.property
import io.github.chenfei0928.preference.base.MutableFieldAccessor.Companion.property
import io.github.chenfei0928.preference.bindEnum

/**
 * @author chenf()
 * @date 2024-07-29 11:15
 */
abstract class JsonLocalFileStoragePreferenceFragment(
    private val name: String
) : PreferenceFragmentCompat() {

    override fun onCreatePreferences(
        savedInstanceState: Bundle?, rootKey: String?
    ) = Debug.countTime(TAG, name) {
        val dataStore: LocalStoragePreferenceDataStore<JsonBean> =
            createPreferenceDataStore()
        preferenceManager.preferenceDataStore = dataStore
        // 缓存data class copy方法
        dataStore.cacheCopyFunc(JsonBean::class, JsonBean::copy)
        dataStore.cacheCopyFunc<JsonBean.InnerJsonBean>(JsonBean.InnerJsonBean::copy)
        dataStore.property(JsonBean::strings)
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

    abstract fun createPreferenceDataStore(): LocalStoragePreferenceDataStore<JsonBean>

    class StorageFragment : JsonLocalFileStoragePreferenceFragment("json") {
        override fun createPreferenceDataStore(): LocalStoragePreferenceDataStore<JsonBean> {
            return LocalStoragePreferenceDataStore(JsonLocalFileStorage.storage(requireContext()))
        }
    }

    class Storage0Fragment : JsonLocalFileStoragePreferenceFragment("json0") {
        override fun createPreferenceDataStore(): LocalStoragePreferenceDataStore<JsonBean> {
            return LocalStoragePreferenceDataStore(requireContext().jsonLocalStorage0)
        }
    }

    companion object {
        private const val TAG = "JsonPreferenceFragment"
    }
}
