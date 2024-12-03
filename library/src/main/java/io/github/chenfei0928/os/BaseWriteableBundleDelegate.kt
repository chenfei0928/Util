package io.github.chenfei0928.os

import androidx.annotation.CallSuper
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @author chenf()
 * @date 2024-11-12 10:26
 */
abstract class BaseWriteableBundleDelegate<Host : Any, T>(
    name: String?, defaultValue: T? = null
) : BaseBundleDelegate<Host, T>(name, defaultValue), ReadWriteProperty<Host, T> {
    @CallSuper
    override fun setValue(thisRef: Host, property: KProperty<*>, value: T) {
        this.value = value
    }
}
