@file:JvmName("DescriptorCf0928Util")

package com.google.protobuf

import androidx.annotation.VisibleForTesting

/**
 * @author chenf()
 * @date 2025-02-28 11:49
 */
val Descriptors.GenericDescriptor.jvmFullyQualifiedName: String
    get() {
        val fileOptions = file.options
        var className = when (this) {
            // 字段、方法定义
            is Descriptors.FieldDescriptor,
            is Descriptors.MethodDescriptor,
            is Descriptors.EnumValueDescriptor,
            is Descriptors.OneofDescriptor,
                -> ".$name"
            // 类、枚举定义
            is Descriptors.EnumDescriptor,
            is Descriptors.Descriptor,
                -> "$$name"
            is Descriptors.ServiceDescriptor,
            is Descriptors.FileDescriptor,
                -> name
            // PackageDescriptor 访问权限是private的，无法引用
            // is Descriptors.PackageDescriptor,
            else -> return if (!this.javaClass.name.endsWith("PackageDescriptor")) {
                throw IllegalArgumentException("Not supported descriptor type: ${this.javaClass.name} $this")
            } else if (fileOptions.hasJavaPackage()) {
                fileOptions.javaPackage
            } else {
                file.`package`
            }
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

fun <E : kotlin.Enum<E>> Descriptors.EnumDescriptor.enumClass(): Class<E> {
    @Suppress("UNCHECKED_CAST")
    return Class.forName(jvmFullyQualifiedName) as Class<E>
}

fun <T : Message> Descriptors.Descriptor.messageClass(): Class<T> {
    @Suppress("UNCHECKED_CAST")
    return Class.forName(jvmFullyQualifiedName) as Class<T>
}

@VisibleForTesting
val Descriptors.GenericDescriptor.parentForTesting: Descriptors.GenericDescriptor?
    get() = parent
