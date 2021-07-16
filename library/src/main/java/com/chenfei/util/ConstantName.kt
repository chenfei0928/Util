package com.chenfei.util

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.staticProperties
import kotlin.reflect.jvm.kotlinProperty

/**
 * 用Kotlin反射实现按值取名
 *
 * [原博客](https://droidyue.com/blog/2020/05/31/using-kotlin-reflection-to-get-constant-variable-name-by-value/)
 * [原仓库](https://github.com/androidyue/KotlinReflectionSample)
 */
fun <T> KClass<*>.findConstantNameByValue(value: T): String? {
    return if (this.isKotlinObject()) {
        getConstantNameByValueForObject(this, value)
    } else {
        getConstantNameByValueFromNormalClass(this, value)
    }
}

fun <T> getConstantNameByValueFromNormalClass(kClass: KClass<*>, value: T): String? {
    value ?: return null
    return kClass.staticProperties
        .filter {
            it.isFinal
        }
        .firstOrNull {
            it.getter.call() == value
        }?.name
}

fun <T> getConstantNameByValueForObject(kClass: KClass<*>, value: T): String? {
    value ?: return null
    return kClass.memberProperties
        .filter {
            it.isFinal
        }
        .firstOrNull {
            it.getter.call() == value
        }?.name
}

fun <T> Class<*>.getConstantNameByValues(value: T): String? {
    value ?: return null
    return declaredFields
        .mapNotNull {
            it.kotlinProperty
        }
        .filter {
            it.isFinal
        }
        .firstOrNull {
            it.getter.call() == value
        }?.name
}

fun KClass<*>.isKotlinObject(): Boolean {
    return this.objectInstance != null
}
