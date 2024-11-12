package io.github.chenfei0928.app.fragment

import androidx.fragment.app.Fragment
import io.github.chenfei0928.app.activity.BaseArgumentDelegate
import kotlin.reflect.KProperty

/**
 * @author chenf()
 * @date 2024-11-12 11:04
 */
class FragmentHostActivityArgumentDelegate<T>(
    name: String?, defaultValue: T? = null
) : BaseArgumentDelegate<Fragment, T>(name, defaultValue) {
    override fun getValueImpl(thisRef: Fragment, property: KProperty<*>): T {
        return thisRef.requireActivity().intent.run {
            setExtrasClassLoader(thisRef.requireActivity().classLoader)
            getTExtra(property)
        }
    }
}
