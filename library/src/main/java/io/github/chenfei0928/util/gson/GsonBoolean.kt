package io.github.chenfei0928.util.gson

import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.internal.bind.TypeAdapters
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException

/**
 * [TypeAdapters.BOOLEAN]
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-04-16 15:04
 */
object GsonBoolean : TypeAdapter<Boolean>() {
    val factory: TypeAdapterFactory = TypeAdapters.newFactory(
        Boolean::class.javaPrimitiveType, Boolean::class.java, this
    )

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): Boolean? {
        return when (`in`.peek()) {
            JsonToken.NULL -> {
                `in`.nextNull()
                null
            }
            JsonToken.STRING -> {
                // support strings for compatibility with GSON 1.7
                val string = `in`.nextString()
                "1" == string || string?.toBoolean() == true
            }
            JsonToken.NUMBER -> {
                1 == `in`.nextInt()
            }
            else -> {
                `in`.nextBoolean()
            }
        }
    }

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Boolean?) {
        out.value(if (value == true) 1 else 0)
    }
}
