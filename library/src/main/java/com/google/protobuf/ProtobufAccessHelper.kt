/**
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-11-24 17:01
 */
package com.google.protobuf

import io.github.chenfei0928.util.MapCache

fun <T : GeneratedMessageLite<T, *>> Class<T>.getProtobufLiteDefaultInstance(): T {
    return GeneratedMessageLite.getDefaultInstance(this)
}

fun <T : GeneratedMessageLite<T, *>> Class<T>.getProtobufLiteParserForType(): Parser<T> {
    return getProtobufLiteDefaultInstance().getParserForType()
}

private val protobufDefaultInstanceCache =
    MapCache<Class<out GeneratedMessageV3>, GeneratedMessageV3> {
        @Suppress("kotlin:S6531")
        it.getMethod("getDefaultInstance").invoke(null) as GeneratedMessageV3
    }

fun <T : GeneratedMessageV3> Class<T>.getProtobufV3DefaultInstance(): T {
    @Suppress("UNCHECKED_CAST", "kotlin:S6531")
    return protobufDefaultInstanceCache[this] as T
}

fun <T : GeneratedMessageV3> Class<T>.getProtobufV3ParserForType(): Parser<T> {
    @Suppress("UNCHECKED_CAST")
    return getProtobufV3DefaultInstance().parserForType as Parser<T>
}
