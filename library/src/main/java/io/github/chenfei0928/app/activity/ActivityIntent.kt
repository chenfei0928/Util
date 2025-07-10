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
    IntentPropertyDelegate(this, isReadOnly = false)

fun <T> KProperty1<Intent, T>.toActivity(): ReadOnlyProperty<Activity, T> =
    IntentPropertyDelegate(this, isReadOnly = true)

private class IntentPropertyDelegate<T>(
    private val otherProperty: KProperty1<Intent, T>,
    private val isReadOnly: Boolean,
) : ReadWriteProperty<Activity, T> {
    override fun getValue(thisRef: Activity, property: KProperty<*>): T =
        otherProperty.get(thisRef.intent)

    override fun setValue(
        thisRef: Activity, property: KProperty<*>, value: T
    ) {
        if (isReadOnly) {
            throw UnsupportedOperationException("Cannot set value of read-only property")
        } else {
            (otherProperty as KMutableProperty1<Intent, T>).set(thisRef.intent, value)
        }
    }
}
