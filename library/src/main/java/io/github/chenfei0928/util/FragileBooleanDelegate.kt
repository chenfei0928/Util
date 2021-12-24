package io.github.chenfei0928.util

import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * 易碎贴boolean，只可访问一次，后续访问将均返回false
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-11-26 16:43
 */
class FragileBooleanDelegate<T> : ReadOnlyProperty<T, Boolean> {
    private var field = AtomicBoolean(true)

    override fun getValue(thisRef: T, property: KProperty<*>): Boolean {
        return field.getAndSet(false)
    }
}
