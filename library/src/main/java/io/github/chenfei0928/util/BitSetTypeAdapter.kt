package io.github.chenfei0928.util

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.util.*

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-06-16 16:29
 */
object BitSetTypeAdapter : TypeAdapter<BitSet>() {

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

    override fun read(`in`: JsonReader): BitSet {
        if (`in`.peek() == JsonToken.NULL) {
            `in`.nextNull()
            return BitSet()
        }
        val bitSet = BitSet()
        `in`.beginArray()
        while (`in`.peek() != JsonToken.END_ARRAY) {
            bitSet.set(`in`.nextInt())
        }
        `in`.endArray()
        return bitSet
    }
}
