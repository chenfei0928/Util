package io.github.chenfei0928.lang

/**
 * @author chenf()
 * @date 2025-01-14 16:25
 */

@Suppress("UNCHECKED_CAST", "RemoveRedundantQualifierName")
fun <T> Class<T>.javaPrimitiveType(): Class<T> = when (this) {
    Int::class.java -> java.lang.Integer.TYPE
    Float::class.java -> java.lang.Float.TYPE
    Byte::class.java -> java.lang.Byte.TYPE
    Double::class.java -> java.lang.Double.TYPE
    Long::class.java -> java.lang.Long.TYPE
    Char::class.java -> java.lang.Character.TYPE
    Boolean::class.java -> java.lang.Boolean.TYPE
    Short::class.java -> java.lang.Short.TYPE
    Void::class.java -> java.lang.Void.TYPE
    else -> this
} as Class<T>

@Suppress("UNCHECKED_CAST", "RemoveRedundantQualifierName")
fun <T> Class<T>.javaBoxedType(): Class<T> = when (this) {
    java.lang.Integer.TYPE -> Int::class.java
    java.lang.Float.TYPE -> Float::class.java
    java.lang.Byte.TYPE -> Byte::class.java
    java.lang.Double.TYPE -> Double::class.java
    java.lang.Long.TYPE -> Long::class.java
    java.lang.Character.TYPE -> Char::class.java
    java.lang.Boolean.TYPE -> Boolean::class.java
    java.lang.Short.TYPE -> Short::class.java
    java.lang.Void.TYPE -> Void::class.java
    else -> this
} as Class<T>
