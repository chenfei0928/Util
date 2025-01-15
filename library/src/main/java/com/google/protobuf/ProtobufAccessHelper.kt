/**
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-11-24 17:01
 */
package com.google.protobuf

import io.github.chenfei0928.lang.contains
import io.github.chenfei0928.reflect.isSubtypeOf
import io.github.chenfei0928.util.DependencyChecker
import io.github.chenfei0928.util.MapCache
import java.lang.reflect.Modifier

fun <T : GeneratedMessageLite<T, *>> Class<T>.getProtobufLiteDefaultInstance(): T {
    return GeneratedMessageLite.getDefaultInstance(this)
}

fun <T : GeneratedMessageLite<T, *>> Class<T>.getProtobufLiteParserForType(): Parser<T> {
    return getProtobufLiteDefaultInstance().getParserForType()
}

private val protobufDefaultInstanceCache =
    MapCache.Basic<Class<out Message>, Message> {
        @Suppress("kotlin:S6531")
        it.getMethod("getDefaultInstance").invoke(null) as Message
    }

fun <T : Message> Class<T>.getProtobufV3DefaultInstance(): T {
    @Suppress("UNCHECKED_CAST", "kotlin:S6531")
    return protobufDefaultInstanceCache[this] as T
}

fun <T : Message> Class<T>.getProtobufV3ParserForType(): Parser<T> {
    @Suppress("UNCHECKED_CAST")
    return getProtobufV3DefaultInstance().parserForType as Parser<T>
}

private val shortDebugStringer by lazy(LazyThreadSafetyMode.NONE) {
    TextFormat.printer().emittingSingleLine(true)
}

fun Message.toShortString() = buildString {
    append(this@toShortString.javaClass.simpleName)
    append('@')
    append(Integer.toHexString(hashCode()))
    append("(")
    shortDebugStringer.print(this@toShortString, this)
    append(')')
}

@Suppress("UNCHECKED_CAST")
val <T : MessageLite> Class<T>.protobufDefaultInstance: T?
    get() = if (Modifier.FINAL !in modifiers) {
        null
    } else if (DependencyChecker.PROTOBUF() && isSubtypeOf(Message::class.java)) {
        (this as Class<out Message>).getProtobufV3DefaultInstance() as T
    } else {
        (this as Class<out GeneratedMessageLite<*, *>>)
            .getProtobufLiteDefaultInstance() as T
    }

@Suppress("UNCHECKED_CAST")
val <T : MessageLite> Class<T>.protobufParserForType: Parser<T>?
    get() = protobufDefaultInstance?.parserForType as Parser<T>?
