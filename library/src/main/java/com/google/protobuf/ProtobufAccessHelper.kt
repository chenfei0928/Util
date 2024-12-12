/**
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-11-24 17:01
 */
package com.google.protobuf

import io.github.chenfei0928.lang.contains
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

fun GeneratedMessageV3.toShortString() = buildString {
    append(this@toShortString.javaClass.simpleName)
    append('@')
    append(Integer.toHexString(hashCode()))
    append("(")
    append(TextFormat.shortDebugString(this@toShortString))
    append(')')
}

@Suppress("kotlin:S6531", "kotlin:S6530", "UNCHECKED_CAST")
fun <T : MessageLite> findProtobufParser(clazz: Class<T>): Parser<T>? {
    return if (Modifier.FINAL !in clazz.modifiers) {
        null
    } else if (DependencyChecker.PROTOBUF() && clazz.isAssignableFrom(GeneratedMessageV3::class.java)) {
        (clazz as Class<out GeneratedMessageV3>)
            .getProtobufV3ParserForType() as Parser<T>
    } else {
        (clazz as Class<out GeneratedMessageLite<*, *>>)
            .getProtobufLiteParserForType() as Parser<T>
    }
}
