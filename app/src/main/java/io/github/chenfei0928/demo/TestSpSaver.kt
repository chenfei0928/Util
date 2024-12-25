package io.github.chenfei0928.demo

import android.content.Context
import io.github.chenfei0928.content.sp.saver.BaseSpSaver
import io.github.chenfei0928.content.sp.saver.DataStoreDelegateStoreProvider.Companion.dataStore
import io.github.chenfei0928.content.sp.saver.convert.DefaultValueSpDelete.Companion.defaultValue
import io.github.chenfei0928.content.sp.saver.convert.EnumNameSpConvertSaver
import io.github.chenfei0928.content.sp.saver.convert.EnumSetNameSpConvertSaver
import io.github.chenfei0928.content.sp.saver.delegate.BooleanDelegate
import io.github.chenfei0928.content.sp.saver.delegate.FloatDelegate
import io.github.chenfei0928.content.sp.saver.delegate.IntDelegate
import io.github.chenfei0928.content.sp.saver.delegate.LongDelegate
import io.github.chenfei0928.content.sp.saver.delegate.StringDelegate

/**
 * @author chenf()
 * @date 2024-12-20 11:35
 */
class TestSpSaver(context: Context) : BaseSpSaver<TestSpSaver>(context, "test") {
    var int: Int by IntDelegate().dataStore()
    var long: Long by LongDelegate().dataStore()
    var float: Float by FloatDelegate().dataStore()
    var boolean: Boolean by BooleanDelegate().dataStore()
    var string: String by StringDelegate().defaultValue("").dataStore()
    var enum: JsonBean.JsonEnum by EnumNameSpConvertSaver<JsonBean.JsonEnum>()
        .defaultValue(JsonBean.JsonEnum.DEFAULT)
        .dataStore()
    var enums: Set<JsonBean.JsonEnum> by EnumSetNameSpConvertSaver<JsonBean.JsonEnum>()
        .defaultValue(emptySet())
        .dataStore()
}
