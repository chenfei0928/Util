package io.github.chenfei0928.json.gson

import android.util.SparseLongArray
import androidx.core.util.forEach
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-06-18 11:18
 */
internal object SparseLongArraySerializer : TypeAdapter<SparseLongArray>() {

    override fun write(out: JsonWriter, value: SparseLongArray?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.beginObject()
            value.forEach { key, value ->
                out.name(key.toString())
                    .value(value)
            }
            out.endObject()
        }
    }

    override fun read(`in`: JsonReader): SparseLongArray? {
        return if (`in`.peek() == JsonToken.NULL) {
            null
        } else {
            SparseLongArray().apply {
                `in`.beginObject()
                while (`in`.hasNext()) {
                    put(
                        `in`.nextName().toInt(),
                        `in`.nextLong()
                    )
                }
                `in`.endObject()
            }
        }
    }
}
