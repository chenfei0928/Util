package io.github.chenfei0928.app.activity

import android.app.Activity
import android.content.Intent
import io.github.chenfei0928.os.BaseBundleDelegate
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
        val accessors = IntentDelegate<Any>()

        inline operator fun <reified T> Activity.getValue(
            thisRef: Activity, property: KProperty<*>
        ): T = intentArg<T>().getValue(thisRef, property)

        inline fun <reified T> Activity.intentArg(): ReadOnlyProperty<Activity, T> =
            accessors as ReadOnlyProperty<Activity, T>
    }
}
