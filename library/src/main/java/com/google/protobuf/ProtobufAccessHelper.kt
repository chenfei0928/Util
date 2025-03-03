/**
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-11-24 17:01
 */
package com.google.protobuf

import io.github.chenfei0928.lang.contains
import io.github.chenfei0928.reflect.isSubclassOf
import io.github.chenfei0928.util.DependencyChecker
import io.github.chenfei0928.util.MapCache
import java.lang.reflect.Modifier

//<editor-fold desc="ProtobufLite获取DefaultInstance" defaultstatus="collapsed">
fun <T : GeneratedMessageLite<T, *>> Class<T>.getProtobufLiteDefaultInstance(): T {
    return GeneratedMessageLite.getDefaultInstance(this)
}

fun <T : GeneratedMessageLite<T, *>> Class<T>.getProtobufLiteParserForType(): Parser<T> {
    return getProtobufLiteDefaultInstance().getParserForType()
}
//</editor-fold>

//<editor-fold desc="ProtobufFull获取DefaultInstance" defaultstatus="collapsed">
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
//</editor-fold>

/**
 * Protobuf获取默认实例
 */
@Suppress("UNCHECKED_CAST")
val <T : MessageLite> Class<T>.protobufDefaultInstance: T
    get() {
        require(Modifier.FINAL in modifiers) {
            "Protobuf message must be final."
        }
        return if (DependencyChecker.protobufFull && isSubclassOf(Message::class.java)) {
            (this as Class<out Message>).getProtobufV3DefaultInstance() as T
        } else {
            (this as Class<out GeneratedMessageLite<*, *>>)
                .getProtobufLiteDefaultInstance() as T
        }
    }

/**
 * Protobuf获取解析/反序列化器
 */
@Suppress("UNCHECKED_CAST")
val <T : MessageLite> Class<T>.protobufParserForType: Parser<T>
    get() = protobufDefaultInstance.parserForType as Parser<T>

private val shortDebugStringer by lazy(LazyThreadSafetyMode.NONE) {
    TextFormat.printer().emittingSingleLine(true)
}

/**
 * 对Protobuf结构体进行短 toString，
 * 它不会像标准toString一样每输出一个字段就换行
 */
fun Message.toShortString() = buildString {
    append(this@toShortString.javaClass.simpleName)
    append('@')
    append(Integer.toHexString(hashCode()))
    append("(")
    shortDebugStringer.print(this@toShortString, this)
    append(')')
}

//<editor-fold desc="Protobuf获取枚举未注册值" defaultstatus="collapsed">
const val PROTOBUF_ENUM_UNRECOGNIZED_NUMBER = -1

/**
 * 获取protobuf枚举的默认值实例
 */
fun <E> Class<E>.protobufEnumUnrecognized(): E where E : kotlin.Enum<E>, E : ProtocolMessageEnum =
    enumConstants!!.find { it.number == PROTOBUF_ENUM_UNRECOGNIZED_NUMBER }!!

/**
 * 获取protobuf枚举的默认值实例
 */
inline fun <reified E> protobufEnumUnrecognized(): E where E : kotlin.Enum<E>, E : ProtocolMessageEnum =
    enumValues<E>().find { it.number == PROTOBUF_ENUM_UNRECOGNIZED_NUMBER }!!
//</editor-fold>
