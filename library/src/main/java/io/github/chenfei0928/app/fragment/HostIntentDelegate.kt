package io.github.chenfei0928.app.fragment

import android.content.Intent
import androidx.fragment.app.Fragment
import io.github.chenfei0928.os.BaseBundleDelegate
import io.github.chenfei0928.reflect.ReadCacheDelegate
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * @author chenf()
 * @date 2024-11-12 11:04
 */
open class HostIntentDelegate<T>(
    name: String? = null, defaultValue: T? = null
) : BaseBundleDelegate<Fragment, T>(name, defaultValue) {
    final override fun getValueImpl(thisRef: Fragment, property: KProperty<*>): T {
        return thisRef.requireActivity().intent.run {
            setExtrasClassLoader(thisRef.requireActivity().classLoader)
            getValueImpl(property)
        }
    }

    protected open fun Intent.getValueImpl(property: KProperty<*>): T {
        return getTExtra(property)
    }

    companion object {
        val accessors = HostIntentDelegate<Any>()

        fun <T> Fragment.hostIntentArg(): ReadOnlyProperty<Fragment, T> =
            ReadCacheDelegate(accessors as ReadOnlyProperty<Fragment, T>)
    }
}
