package io.github.chenfei0928.demo.storage

import android.content.Context
import io.github.chenfei0928.content.sp.saver.BaseSpSaver
import io.github.chenfei0928.content.sp.saver.DataStoreDelegateStoreProvider.Companion.dataStore
import io.github.chenfei0928.content.sp.saver.convert.DefaultValueSpDelete.Companion.defaultValue
import io.github.chenfei0928.content.sp.saver.convert.EnumNameSpConvertSaver
import io.github.chenfei0928.content.sp.saver.convert.EnumNameSpConvertSaver.Companion.invoke
import io.github.chenfei0928.content.sp.saver.convert.EnumSetNameSpConvertSaver
import io.github.chenfei0928.content.sp.saver.convert.EnumSetNameSpConvertSaver.Companion.invoke
import io.github.chenfei0928.content.sp.saver.convert.IntArraySpConvertSaver
import io.github.chenfei0928.content.sp.saver.convert.KtxsJsonSpConvertSaver
import io.github.chenfei0928.content.sp.saver.convert.LocalSerializerSpConvertSaver
import io.github.chenfei0928.content.sp.saver.delegate.BooleanDelegate
import io.github.chenfei0928.content.sp.saver.delegate.FloatDelegate
import io.github.chenfei0928.content.sp.saver.delegate.IntDelegate
import io.github.chenfei0928.content.sp.saver.delegate.LongDelegate
import io.github.chenfei0928.content.sp.saver.delegate.StringDelegate
import io.github.chenfei0928.content.sp.saver.delegate.StringSetDelegate
import io.github.chenfei0928.demo.bean.JsonBean
import io.github.chenfei0928.demo.bean.Test
import io.github.chenfei0928.repository.local.serializer.ProtobufSerializer

/**
 * @author chenf()
 * @date 2024-12-20 11:35
 */
class TestSpSaver(context: Context) : BaseSpSaver<TestSpSaver>(context, "test") {
    var int: Int by IntDelegate().dataStore()
    var long: Long by LongDelegate().dataStore()
    var float: Float by FloatDelegate().dataStore()
    var boolean: Boolean by BooleanDelegate().dataStore()
    var string: String by StringDelegate()
        .defaultValue("")
        .dataStore()
    var stringSet: Set<String> by StringSetDelegate()
        .defaultValue(emptySet())
        .dataStore()
    var enum: JsonBean.JsonEnum by EnumNameSpConvertSaver<JsonBean.JsonEnum>()
        .defaultValue(JsonBean.JsonEnum.DEFAULT)
        .dataStore()
    var enums: Set<JsonBean.JsonEnum> by EnumSetNameSpConvertSaver<JsonBean.JsonEnum>()
        .defaultValue(emptySet())
        .dataStore()

    // 下面三个是结构体类型，不支持作为 preferenceDataStore
    var json: JsonBean.InnerJsonBean? by KtxsJsonSpConvertSaver<JsonBean.InnerJsonBean>()
        .dataStore()
    var intArray: IntArray by IntArraySpConvertSaver()
        .defaultValue(intArrayOf())
        .dataStore()
    var test: Test by LocalSerializerSpConvertSaver<Test>(ProtobufSerializer())
        .defaultValue(Test.getDefaultInstance())
        .dataStore()
}
