/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-23 15:46
 */
package io.github.chenfei0928.app.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import io.github.chenfei0928.os.AbsBundleProperty
import io.github.chenfei0928.os.BaseWriteableBundleDelegate
import io.github.chenfei0928.reflect.ReadCacheDelegate
import io.github.chenfei0928.reflect.ReadCacheDelegate.Companion.getCacheOrValue
import io.github.chenfei0928.reflect.ReadCacheDelegate.Companion.setValueAndCache
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class ArgumentDelegate<T>(
    name: String? = null, defaultValue: T? = null
) : BaseWriteableBundleDelegate<Fragment, T>(name, defaultValue) {
    final override fun getValueImpl(thisRef: Fragment, property: KProperty<*>): T {
        return thisRef.requireArguments().run {
            classLoader = thisRef.javaClass.classLoader
            getValueImpl(property)
        }
    }

    protected open fun Bundle.getValueImpl(property: KProperty<*>): T {
        return getT(property)
    }

    final override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        super.setValue(thisRef, property, value)
        thisRef.applyArgumentBundle {
            setValueImpl(property, value)
        }
    }

    protected open fun Bundle.setValueImpl(property: KProperty<*>, value: T) {
        putT(property, value)
    }

    companion object {
        // no cache
        private val accessors = object : AbsBundleProperty<Fragment, Any>(),
            ReadWriteProperty<Fragment, Any> {
            override fun getValue(
                thisRef: Fragment, property: KProperty<*>
            ): Any = thisRef.requireArguments().run {
                classLoader = thisRef.javaClass.classLoader
                getT(property)
            }

            override fun setValue(
                thisRef: Fragment, property: KProperty<*>, value: Any
            ) {
                thisRef.applyArgumentBundle {
                    putT(property, value)
                }
            }
        }

        operator fun <T> Fragment.getValue(thisRef: Fragment, property: KProperty<*>): T =
            (accessors as ReadWriteProperty<Fragment, T>).getCacheOrValue(thisRef, property)

        operator fun <T> Fragment.setValue(
            thisRef: Fragment, property: KProperty<*>, value: T
        ) = (accessors as ReadWriteProperty<Fragment, T>).setValueAndCache(thisRef, property, value)

        fun <T> Fragment.argumentArg(): ReadWriteProperty<Fragment, T> =
            ReadCacheDelegate.Writable(accessors as ReadWriteProperty<Fragment, T>)
    }
}
