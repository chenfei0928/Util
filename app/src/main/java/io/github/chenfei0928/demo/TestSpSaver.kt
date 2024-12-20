package io.github.chenfei0928.demo

import android.content.Context
import io.github.chenfei0928.content.sp.saver.BaseSpSaver
import io.github.chenfei0928.content.sp.saver.convert.DefaultValueSpDelete.Companion.defaultValue
import io.github.chenfei0928.content.sp.saver.convert.EnumNameSpConvertSaver
import io.github.chenfei0928.content.sp.saver.convert.EnumSetNameSpConvertSaver
import io.github.chenfei0928.content.sp.saver.delegate.BooleanDelegate
import io.github.chenfei0928.content.sp.saver.delegate.FloatDelegate
import io.github.chenfei0928.content.sp.saver.delegate.IntDelegate
import io.github.chenfei0928.content.sp.saver.delegate.LongDelegate
import io.github.chenfei0928.content.sp.saver.delegate.StringDelegate
import io.github.chenfei0928.os.Debug
import io.github.chenfei0928.preference.sp.SpSaverFieldAccessor.Companion.property
import io.github.chenfei0928.preference.sp.SpSaverPreferenceDataStore

/**
 * @author chenf()
 * @date 2024-12-20 11:35
 */
class TestSpSaver(context: Context) : BaseSpSaver(context, "test") {
    var int: Int by IntDelegate()
    var long: Long by LongDelegate()
    var float: Float by FloatDelegate()
    var boolean: Boolean by BooleanDelegate()
    var string: String by StringDelegate().defaultValue("")
    var enum: JsonEnum by EnumNameSpConvertSaver<JsonEnum>().defaultValue(JsonEnum.DEFAULT)
    var enums: Set<JsonEnum> by EnumSetNameSpConvertSaver<JsonEnum>().defaultValue(emptySet())

    val dataStore: SpSaverPreferenceDataStore<TestSpSaver> by lazy {
        setEnableAutoApply(true)
        Debug.traceAndTime(TAG, "spSaver_dataStore_${System.currentTimeMillis()}") {
            SpSaverPreferenceDataStore<TestSpSaver>(this, false).apply {
                property(::int)
                property(::long)
                property(::float)
                property(::boolean)
                property(::string)
                property(::enum)
                property(::enums)
            }
        }
    }

    companion object {
        private const val TAG = "TestSpSaver"
    }
}
