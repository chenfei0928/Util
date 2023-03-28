package com.google.protobuf

/**
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-11-24 17:01
 */
fun <T : GeneratedMessageLite<T, *>> Class<T>.getProtobufLiteParserForType(): Parser<T> {
    return getProtobufLiteDefaultInstance().getParserForType()
}

fun <T : GeneratedMessageLite<T, *>> Class<T>.getProtobufLiteDefaultInstance(): T {
    return GeneratedMessageLite.getDefaultInstance(this)
}

fun <T : GeneratedMessageV3> Class<T>.getProtobufParserForType(): Parser<T> {
    return getProtobufDefaultInstance().parserForType as Parser<T>
}

fun <T : GeneratedMessageV3> Class<T>.getProtobufDefaultInstance(): T {
    return getMethod("getDefaultInstance").invoke(null) as T
}
