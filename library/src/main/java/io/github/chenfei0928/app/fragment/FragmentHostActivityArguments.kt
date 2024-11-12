package io.github.chenfei0928.app.fragment

import android.os.Parcelable
import androidx.core.content.IntentCompat
import androidx.fragment.app.Fragment
import io.github.chenfei0928.app.activity.BaseArgumentDelegate
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaType

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-23 15:46
 */
class FragmentHostActivityParcelableDelegate<T : Parcelable>(
    name: String? = null
) : BaseArgumentDelegate<Fragment, T>(name) {
    override fun getValueImpl(thisRef: Fragment, property: KProperty<*>): T {
        return IntentCompat.getParcelableExtra(
            thisRef.requireActivity().intent,
            name ?: property.name,
            property.returnType.javaType as Class<T>
        ) ?: throw IllegalArgumentException("缺少应有的字段: ${name ?: property.name}")
    }
}

class FragmentHostActivityParcelableNullableDelegate<T : Parcelable>(
    name: String? = null
) : BaseArgumentDelegate<Fragment, T?>(name) {
    override fun getValueImpl(thisRef: Fragment, property: KProperty<*>): T? {
        return IntentCompat.getParcelableExtra(
            thisRef.requireActivity().intent,
            name ?: property.name,
            property.returnType.javaType as Class<T>
        )
    }
}

class FragmentHostActivityParcelableListDelegate<T : Parcelable>(
    name: String? = null
) : BaseArgumentDelegate<Fragment, List<T>>(name) {
    override fun getValueImpl(thisRef: Fragment, property: KProperty<*>): List<T> {
        return IntentCompat.getParcelableArrayListExtra(
            thisRef.requireActivity().intent,
            name ?: property.name,
            property.returnType.arguments[0].type?.javaType as Class<T>
        ) ?: throw IllegalArgumentException("缺少应有的字段: ${name ?: property.name}")
    }
}

class FragmentHostActivityStringDelegate(
    name: String? = null
) : BaseArgumentDelegate<Fragment, String>(name) {
    override fun getValueImpl(thisRef: Fragment, property: KProperty<*>): String {
        return thisRef.requireActivity().intent
            .getStringExtra(name ?: property.name)
            ?: ""
    }
}

class FragmentHostActivityStringNullableDelegate(
    name: String? = null
) : BaseArgumentDelegate<Fragment, String?>(name) {
    override fun getValueImpl(thisRef: Fragment, property: KProperty<*>): String? {
        return thisRef.requireActivity().intent
            .getStringExtra(name ?: property.name)
    }
}

class FragmentHostActivityIntDelegate(
    name: String? = null
) : BaseArgumentDelegate<Fragment, Int>(name) {
    override fun getValueImpl(thisRef: Fragment, property: KProperty<*>): Int {
        return thisRef.requireActivity().intent
            .getIntExtra(name ?: property.name, 0)
    }
}

class FragmentHostActivityFloatDelegate(
    name: String? = null
) : BaseArgumentDelegate<Fragment, Float>(name) {
    override fun getValueImpl(thisRef: Fragment, property: KProperty<*>): Float {
        return thisRef.requireActivity().intent
            .getFloatExtra(name ?: property.name, 0f)
    }
}

class FragmentHostActivityBooleanDelegate(
    name: String? = null
) : BaseArgumentDelegate<Fragment, Boolean>(name) {
    override fun getValueImpl(thisRef: Fragment, property: KProperty<*>): Boolean {
        return thisRef.requireActivity().intent
            .getBooleanExtra(name ?: property.name, false)
    }
}
