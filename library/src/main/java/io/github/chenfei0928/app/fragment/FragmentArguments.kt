package io.github.chenfei0928.app.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

/**
 * @author chenf()
 * @date 2025-07-08 18:36
 */
fun <T> KMutableProperty1<Bundle, T>.toFragment(): ReadWriteProperty<Fragment, T> =
    BundlePropertyDelegate(otherProperty = this, isReadOnly = false)

fun <T> KProperty1<Bundle, T>.toFragment(): ReadOnlyProperty<Fragment, T> =
    BundlePropertyDelegate(otherProperty = this, isReadOnly = true)

private class BundlePropertyDelegate<T>(
    private val otherProperty: KProperty1<Bundle, T>,
    private val isReadOnly: Boolean,
) : ReadWriteProperty<Fragment, T> {
    override fun getValue(thisRef: Fragment, property: KProperty<*>): T =
        otherProperty.get(thisRef.requireArguments())

    override fun setValue(
        thisRef: Fragment, property: KProperty<*>, value: T
    ) {
        if (isReadOnly) {
            throw UnsupportedOperationException("Cannot set value of read-only property")
        } else thisRef.applyArgumentBundle {
            (otherProperty as KMutableProperty1<Bundle, T>).set(this, value)
        }
    }
}
