package com.chenfei.util

import android.util.SparseArray
import androidx.core.util.forEach
import com.google.gson.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-10-23 17:03
 */
class SparseArrayJsonSerializer : JsonSerializer<SparseArray<*>>, JsonDeserializer<SparseArray<*>> {

    override fun serialize(src: SparseArray<*>?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return if (src == null) {
            JsonNull.INSTANCE
        } else {
            JsonObject().apply {
                src.forEach { key, value ->
                    add(key.toString(), context.serialize(value))
                }
            }
        }
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): SparseArray<*>? {
        return if (json == JsonNull.INSTANCE) {
            null
        } else {
            // 获取范型参数类型
            typeOfT as ParameterizedType
            val type = typeOfT.actualTypeArguments[0]
            // 实例化实例，开始解析
            SparseArray<Any>().apply {
                val jsonObject = json.asJsonObject
                // 对每个key进行遍历
                jsonObject.keySet().forEach { key ->
                    // 反序列化其值，装入实例中
                    put(key.toInt(), context.deserialize(jsonObject[key], type))
                }
            }
        }
    }
}
