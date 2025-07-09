package io.github.chenfei0928.app.activity

import android.app.Activity
import android.content.Intent
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

/**
 * @author chenf()
 * @date 2025-07-08 18:37
 */
fun <T> KMutableProperty1<Intent, T>.toActivity(): ReadWriteProperty<Activity, T> =
    object : ReadWriteProperty<Activity, T> {
        override fun getValue(thisRef: Activity, property: KProperty<*>): T =
            this@toActivity.get(thisRef.intent)

        override fun setValue(
            thisRef: Activity, property: KProperty<*>, value: T
        ) {
            this@toActivity.set(thisRef.intent, value)
        }
    }

fun <T> KProperty1<Intent, T>.toActivity(): ReadOnlyProperty<Activity, T> =
    object : ReadOnlyProperty<Activity, T> {
        override fun getValue(thisRef: Activity, property: KProperty<*>): T =
            this@toActivity.get(thisRef.intent)
    }
