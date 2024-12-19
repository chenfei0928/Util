package io.github.chenfei0928.demo

import android.os.Bundle
import android.util.Log
import androidx.preference.PreferenceFragmentCompat
import io.github.chenfei0928.concurrent.coroutines.coroutineScope
import io.github.chenfei0928.demo.MainActivity.Companion.testDataStore
import io.github.chenfei0928.preference.FieldAccessor
import io.github.chenfei0928.preference.FieldAccessor.Companion.protobufField
import io.github.chenfei0928.preference.FieldAccessor.ProtobufMessageField.Companion.property
import io.github.chenfei0928.preference.bindEnum
import io.github.chenfei0928.preference.datastore.DataStoreDataStore
import io.github.chenfei0928.preference.datastore.DataStorePreferenceGroupBuilder.Companion.buildPreferenceScreen
import kotlinx.coroutines.launch

/**
 * @author chenf()
 * @date 2024-07-29 11:15
 */
class TestPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val testDataStore = requireContext().testDataStore
        coroutineScope.launch {
            testDataStore.data.collect { test ->
                Log.i(TAG, "onCreatePreferences: $test")
            }
        }
        val dataStore: DataStoreDataStore<Test> =
            DataStoreDataStore(coroutineScope, testDataStore)
        preferenceManager.preferenceDataStore = dataStore
        preferenceScreen = buildPreferenceScreen(dataStore) {
            checkBoxPreference(
                dataStore.protobufField("boolean", Test::getBoolean, Test.Builder::setBoolean)
                    .let { dataStore.property(it) }) {
                title = "boolean"
            }
            checkBoxPreference(
                dataStore.property(
                    FieldAccessor.ProtobufMessageField<Test, Test>(Test.TEST_FIELD_NUMBER),
                    FieldAccessor.ProtobufMessageField<Test, Boolean>(Test.BOOLEAN_FIELD_NUMBER),
                )
            ) {
                title = "innerBoolean"
            }
            editTextPreference(dataStore.property(Test.STRING_FIELD_NUMBER)) {
                title = "string"
            }
            seekBarPreference(dataStore.property(Test.INT_FIELD_NUMBER)) {
                title = "int"
            }
            dropDownPreference<TestEnum>(dataStore.property(Test.ENUM_FIELD_NUMBER)) {
                title = "enum"
                bindEnum<TestEnum> { it.name }
            }
            multiSelectListPreference<TestEnum>(dataStore.property(Test.ENUMLIST_FIELD_NUMBER)) {
                title = "enumList"
                bindEnum<TestEnum> { it.name }
            }
        }
    }

    companion object {
        private const val TAG = "TestPreferenceFragment"
    }
}
