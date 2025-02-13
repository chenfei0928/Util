package io.github.chenfei0928.demo.storage

import io.github.chenfei0928.content.sp.saver.BaseMmkvSaver
import io.github.chenfei0928.content.sp.saver.convert.EnumNameSpConvert
import io.github.chenfei0928.content.sp.saver.convert.EnumSetNameSpConvert
import io.github.chenfei0928.content.sp.saver.convert.IntArraySpConvert
import io.github.chenfei0928.content.sp.saver.convert.KtxsJsonSpConvert
import io.github.chenfei0928.content.sp.saver.convert.LocalSerializerSpConvert
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
object TestMmkvSaver : BaseMmkvSaver<TestMmkvSaver>("test", enableFieldObservable = true) {
    var int: Int by dataStore { IntDelegate() }
    var long: Long by dataStore { LongDelegate() }
    var float: Float by dataStore { FloatDelegate() }
    var boolean: Boolean by dataStore { BooleanDelegate() }
    var string: String by dataStore { StringDelegate.nonNull() }
    var stringSet: Set<String> by dataStore { StringSetDelegate.nonNull() }
    var enum: JsonBean.JsonEnum by dataStore {
        EnumNameSpConvert.nonnull(JsonBean.JsonEnum.DEFAULT)
    }
    var enums: Set<JsonBean.JsonEnum> by dataStore {
        EnumSetNameSpConvert.nonnull(JsonBean.JsonEnum.DEFAULT)
    }

    // 下面三个是结构体类型，不支持作为 preferenceDataStore
    var json: JsonBean.InnerJsonBean? by dataStore {
        KtxsJsonSpConvert()
    }
    var intArray: IntArray by dataStore {
        IntArraySpConvert.nonnull()
    }
    var test: Test by dataStore {
        LocalSerializerSpConvert.nonnullForMmkv(ProtobufSerializer())
    }
}
