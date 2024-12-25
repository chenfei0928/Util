package io.github.chenfei0928.demo

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import io.github.chenfei0928.concurrent.coroutines.coroutineScope
import io.github.chenfei0928.demo.MainActivity.Companion.testDataStore
import io.github.chenfei0928.os.Debug
import io.github.chenfei0928.preference.FieldAccessor
import io.github.chenfei0928.preference.FieldAccessor.Companion.protobufField
import io.github.chenfei0928.preference.FieldAccessor.Companion.protobufProperty
import io.github.chenfei0928.preference.FieldAccessor.ProtobufMessageField.Companion.property
import io.github.chenfei0928.preference.bindEnum
import io.github.chenfei0928.preference.datastore.DataStorePreferenceDataStore
import io.github.chenfei0928.preference.datastore.DataStorePreferenceGroupBuilder.Companion.buildPreferenceScreen

/**
 * @author chenf()
 * @date 2024-07-29 11:15
 */
class TestPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(
        savedInstanceState: Bundle?, rootKey: String?
    ) = Debug.countTime(TAG, "testProtobuf") {
        val testDataStore = requireContext().testDataStore
        val dataStore: DataStorePreferenceDataStore<Test> =
            DataStorePreferenceDataStore(coroutineScope, testDataStore)
        preferenceManager.preferenceDataStore = dataStore
        preferenceScreen = buildPreferenceScreen(dataStore) {
            // protobuf 字段引用方式1
            checkBoxPreference(
                dataStore.protobufProperty("boolean", Test::getBoolean, Test.Builder::setBoolean)
            ) {
                title = "boolean"
            }
            // protobuf 二层字段引用方式1
            seekBarPreference(
                dataStore.property(
                    dataStore.protobufField("test", Test::getTest, Test.Builder::setTest),
                    dataStore.protobufField("int", Test::getInt, Test.Builder::setInt),
                )
            ) {
                title = "innerInt"
            }
            // protobuf 二层字段引用方式2（full专用）
            checkBoxPreference(
                dataStore.property(
                    FieldAccessor.ProtobufMessageField<Test, Test>(Test.TEST_FIELD_NUMBER),
                    FieldAccessor.ProtobufMessageField<Test, Boolean>(Test.BOOLEAN_FIELD_NUMBER),
                )
            ) {
                title = "innerBoolean"
            }
            // protobuf 字段引用方式2（full专用）
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
