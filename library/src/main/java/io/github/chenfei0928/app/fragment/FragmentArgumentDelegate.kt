package io.github.chenfei0928.app.fragment

import androidx.fragment.app.Fragment
import io.github.chenfei0928.app.activity.BaseWriteableArgumentDelegate
import kotlin.reflect.KProperty

/**
 * @author chenf()
 * @date 2024-11-12 11:00
 */
class FragmentArgumentDelegate<T>(
    name: String?, value: T? = null
) : BaseWriteableArgumentDelegate<Fragment, T>(name, value) {
    override fun getValueImpl(thisRef: Fragment, property: KProperty<*>): T {
        return thisRef.requireArguments().getT(property)
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        super.setValue(thisRef, property, value)
        thisRef.applyArgumentBundle {
            putT(property, value)
        }
    }
}
