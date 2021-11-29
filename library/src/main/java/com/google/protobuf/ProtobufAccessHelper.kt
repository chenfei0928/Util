package com.google.protobuf

import java.io.InputStream
import java.nio.ByteBuffer

/**
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-11-24 17:01
 */
fun <T : GeneratedMessageLite<T, *>, Source> Class<T>.getParseFrom(): (Source) -> T {
    val defaultInstance: T = GeneratedMessageLite.getDefaultInstance(this)
    return { source ->
        when (source) {
            null -> GeneratedMessageLite.parseFrom(defaultInstance,
                    CodedInputStream.newInstance(Internal.EMPTY_BYTE_ARRAY))
            is ByteBuffer -> GeneratedMessageLite.parseFrom(defaultInstance, source)
            is ByteString -> GeneratedMessageLite.parseFrom(defaultInstance, source)
            is ByteArray -> GeneratedMessageLite.parseFrom(defaultInstance, source)
            is InputStream -> GeneratedMessageLite.parseFrom(defaultInstance, source)
            is CodedInputStream -> GeneratedMessageLite.parseFrom(defaultInstance, source)
            else -> throw IllegalArgumentException("输入数据类型不支持：$source")
        }
    }
}
