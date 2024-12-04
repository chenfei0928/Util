package io.github.chenfei0928.app.activity

import android.app.Activity
import android.content.Intent
import io.github.chenfei0928.os.AbsBundleProperty
import io.github.chenfei0928.os.BaseBundleDelegate
import io.github.chenfei0928.reflect.ReadCacheDelegate
import io.github.chenfei0928.reflect.ReadCacheDelegate.Companion.getCacheOrValue
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-23 15:46
 */
open class IntentDelegate<T>(
    name: String? = null, defaultValue: T? = null,
) : BaseBundleDelegate<Activity, T>(name, defaultValue) {
    final override fun getValueImpl(thisRef: Activity, property: KProperty<*>): T {
        return thisRef.intent.run {
            setExtrasClassLoader(thisRef.classLoader)
            getValueImpl(property)
        }
    }

    protected open fun Intent.getValueImpl(property: KProperty<*>): T {
        return getTExtra(property)
    }

    companion object {
        // no cache
        private val accessors = object : AbsBundleProperty<Activity, Any>() {
            override fun getValue(
                thisRef: Activity, property: KProperty<*>
            ): Any = thisRef.intent.run {
                setExtrasClassLoader(thisRef.classLoader)
                getTExtra(property)
            }
        }

        operator fun <T> Activity.getValue(
            thisRef: Activity, property: KProperty<*>
        ): T = (accessors as ReadOnlyProperty<Activity, T>).getCacheOrValue(thisRef, property)

        fun <T> Activity.intentArg(): ReadOnlyProperty<Activity, T> =
            ReadCacheDelegate(accessors as ReadOnlyProperty<Activity, T>)
    }
}
