/**
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-11-24 17:01
 */
package com.google.protobuf

import androidx.annotation.VisibleForTesting
import io.github.chenfei0928.lang.contains
import io.github.chenfei0928.reflect.isSubclassOf
import io.github.chenfei0928.util.DependencyChecker
import io.github.chenfei0928.util.MapCache
import java.lang.reflect.Modifier
import kotlin.Enum

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
    } else if (DependencyChecker.PROTOBUF() && isSubclassOf(Message::class.java)) {
        (this as Class<out Message>).getProtobufV3DefaultInstance() as T
    } else {
        (this as Class<out GeneratedMessageLite<*, *>>)
            .getProtobufLiteDefaultInstance() as T
    }

@Suppress("UNCHECKED_CAST")
val <T : MessageLite> Class<T>.protobufParserForType: Parser<T>?
    get() = protobufDefaultInstance?.parserForType as Parser<T>?

fun <E : Enum<E>> Descriptors.EnumDescriptor.enumClass(): Class<E> {
    @Suppress("UNCHECKED_CAST")
    return Class.forName(jvmFullyQualifiedName) as Class<E>
}

fun <T : Message> Descriptors.Descriptor.messageClass(): Class<T> {
    @Suppress("UNCHECKED_CAST")
    return Class.forName(jvmFullyQualifiedName) as Class<T>
}

val Descriptors.GenericDescriptor.jvmFullyQualifiedName: String
    get() {
        val fileOptions = file.options
        var className =
            if (this is Descriptors.FieldDescriptor || this is Descriptors.MethodDescriptor) {
                "." + name!!
            } else {
                "$" + name!!
            }
        var parent = parent
        while (parent != null) {
            if (parent !is Descriptors.FileDescriptor) {
                // 类（字段或枚举实例的 parent 只能是message/类）
                className = "$" + parent.name + className
            } else if (!fileOptions.javaMultipleFiles) {
                className = if (fileOptions.hasJavaOuterClassname()) {
                    "$" + fileOptions.javaOuterClassname + className
                } else {
                    "$" + parent.name.let {
                        it.substring(0, it.length - ".proto".length)
                    } + className
                }
            }
            parent = parent.parent
        }
        return if (fileOptions.hasJavaPackage()) {
            fileOptions.javaPackage + "." + className.substring(1)
        } else {
            file.`package` + "." + className.substring(1)
        }
    }

@VisibleForTesting
val Descriptors.GenericDescriptor.parentForTesting: Descriptors.GenericDescriptor?
    get() = parent
