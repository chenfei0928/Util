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
    object : ReadWriteProperty<Fragment, T> {
        override fun getValue(thisRef: Fragment, property: KProperty<*>): T =
            this@toFragment.get(thisRef.requireArguments())

        override fun setValue(
            thisRef: Fragment, property: KProperty<*>, value: T
        ) {
            thisRef.applyArgumentBundle {
                this@toFragment.set(this, value)
            }
        }
    }

fun <T> KProperty1<Bundle, T>.toFragment(): ReadOnlyProperty<Fragment, T> =
    object : ReadOnlyProperty<Fragment, T> {
        override fun getValue(thisRef: Fragment, property: KProperty<*>): T =
            this@toFragment.get(thisRef.requireArguments())
    }
