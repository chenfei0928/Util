package io.github.chenfei0928.app.activity

import android.app.Activity
import kotlin.reflect.KProperty

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-23 15:46
 */
class ActivityArgumentDelegate<T>(
    name: String? = null, defaultValue: T? = null,
) : BaseArgumentDelegate<Activity, T>(name, defaultValue) {
    override fun getValueImpl(thisRef: Activity, property: KProperty<*>): T {
        return thisRef.intent.run {
            setExtrasClassLoader(thisRef.classLoader)
            getTExtra(property)
        }
    }
}
