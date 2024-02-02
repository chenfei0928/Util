package io.github.chenfei0928.util.gson

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.util.BitSet

/**
 * 可以使用BitSet来实现intIdsArray，并实现快速的查找其是否在其中的操作。
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-06-16 16:29
 */
object BitSetTypeAdapter : TypeAdapter<BitSet?>() {

    override fun write(out: JsonWriter, value: BitSet?) {
        if (value == null) {
            out.nullValue()
            return
        }
        out.beginArray()
        var index = value.nextSetBit(0)
        while (index >= 0) {
            out.value(index)
            index = value.nextSetBit(index)
        }
        out.endArray()
    }

    override fun read(`in`: JsonReader): BitSet? {
        if (`in`.peek() == JsonToken.NULL) {
            `in`.nextNull()
            return null
        }
        val bitSet = BitSet()
        `in`.beginArray()
        while (`in`.hasNext()) {
            bitSet.set(`in`.nextInt())
        }
        `in`.endArray()
        return bitSet
    }
}
