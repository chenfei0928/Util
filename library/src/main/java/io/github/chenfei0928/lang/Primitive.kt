package io.github.chenfei0928.lang

/**
 * @author chenf()
 * @date 2025-01-14 16:25
 */

fun <T> Class<T>.javaPrimitiveType(): Class<T> = when (this) {
    java.lang.Integer::class.java -> java.lang.Integer.TYPE
    java.lang.Float::class.java -> java.lang.Float.TYPE
    java.lang.Byte::class.java -> java.lang.Byte.TYPE
    java.lang.Double::class.java -> java.lang.Double.TYPE
    java.lang.Long::class.java -> java.lang.Long.TYPE
    java.lang.Character::class.java -> java.lang.Character.TYPE
    java.lang.Boolean::class.java -> java.lang.Boolean.TYPE
    java.lang.Short::class.java -> java.lang.Short.TYPE
    java.lang.Void::class.java -> java.lang.Void.TYPE
    else -> this
} as Class<T>

fun <T> Class<T>.javaBoxedType(): Class<T> = when (this) {
    java.lang.Integer.TYPE -> java.lang.Integer::class.java
    java.lang.Float.TYPE -> java.lang.Float::class.java
    java.lang.Byte.TYPE -> java.lang.Byte::class.java
    java.lang.Double.TYPE -> java.lang.Double::class.java
    java.lang.Long.TYPE -> java.lang.Long::class.java
    java.lang.Character.TYPE -> java.lang.Character::class.java
    java.lang.Boolean.TYPE -> java.lang.Boolean::class.java
    java.lang.Short.TYPE -> java.lang.Short::class.java
    java.lang.Void.TYPE -> java.lang.Void::class.java
    else -> this
} as Class<T>
