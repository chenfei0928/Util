package com.google.protobuf

/**
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-11-24 17:01
 */
fun <T : GeneratedMessageLite<T, *>> Class<T>.getProtobufParserForType(): Parser<T> {
    return getProtobufDefaultInstance().getParserForType()
}

fun <T : GeneratedMessageLite<T, *>> Class<T>.getProtobufDefaultInstance(): T {
    return GeneratedMessageLite.getDefaultInstance(this)
}
