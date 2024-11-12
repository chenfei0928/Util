package io.github.chenfei0928.app.fragment

import android.os.Parcelable
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import io.github.chenfei0928.app.activity.BaseWriteableArgumentDelegate
import io.github.chenfei0928.collection.asArrayList
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaType

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-23 15:46
 */
class FragmentParcelableDelegate<T : Parcelable>(
    name: String? = null
) : BaseWriteableArgumentDelegate<Fragment, T>(name) {
    override fun getValueImpl(thisRef: Fragment, property: KProperty<*>): T {
        return BundleCompat.getParcelable(
            thisRef.requireArguments(),
            name ?: property.name,
            property.returnType.javaType as Class<T>
        ) ?: throw IllegalArgumentException("缺少应有的字段: ${name ?: property.name}")
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        super.setValue(thisRef, property, value)
        thisRef.applyArgumentBundle {
            putParcelable(name ?: property.name, value)
        }
    }
}

class FragmentParcelableNullableDelegate<T : Parcelable>(
    name: String? = null
) : BaseWriteableArgumentDelegate<Fragment, T?>(name) {
    override fun getValueImpl(thisRef: Fragment, property: KProperty<*>): T? {
        return BundleCompat.getParcelable(
            thisRef.requireArguments(),
            name ?: property.name,
            property.returnType.javaType as Class<T>
        )
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T?) {
        super.setValue(thisRef, property, value)
        thisRef.applyArgumentBundle {
            putParcelable(name ?: property.name, value)
        }
    }
}

class FragmentParcelableListDelegate<T : Parcelable>(
    name: String? = null
) : BaseWriteableArgumentDelegate<Fragment, List<T>>(name) {
    override fun getValueImpl(thisRef: Fragment, property: KProperty<*>): List<T> {
        return BundleCompat.getParcelableArrayList(
            thisRef.requireArguments(),
            name ?: property.name,
            property.returnType.arguments[0].type?.javaType as Class<T>
        ) ?: throw IllegalArgumentException("缺少应有的字段: ${name ?: property.name}")
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: List<T>) {
        super.setValue(thisRef, property, value)
        thisRef.applyArgumentBundle {
            putParcelableArrayList(name ?: property.name, value.asArrayList())
        }
    }
}

class FragmentStringDelegate(
    name: String? = null
) : BaseWriteableArgumentDelegate<Fragment, String>(name) {
    override fun getValueImpl(thisRef: Fragment, property: KProperty<*>): String {
        return thisRef
            .requireArguments()
            .getString(name ?: property.name, null)
            ?: ""
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: String) {
        super.setValue(thisRef, property, value)
        thisRef.applyArgumentBundle {
            putString(name ?: property.name, value)
        }
    }
}

class FragmentIntDelegate(
    name: String? = null
) : BaseWriteableArgumentDelegate<Fragment, Int>(name) {
    override fun getValueImpl(thisRef: Fragment, property: KProperty<*>): Int {
        return thisRef
            .requireArguments()
            .getInt(name ?: property.name)
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: Int) {
        super.setValue(thisRef, property, value)
        thisRef.applyArgumentBundle {
            putInt(name ?: property.name, value)
        }
    }
}

class FragmentBooleanDelegate(
    name: String? = null
) : BaseWriteableArgumentDelegate<Fragment, Boolean>(name) {
    override fun getValueImpl(thisRef: Fragment, property: KProperty<*>): Boolean {
        return thisRef
            .requireArguments()
            .getBoolean(name ?: property.name)
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: Boolean) {
        super.setValue(thisRef, property, value)
        thisRef.applyArgumentBundle {
            putBoolean(name ?: property.name, value)
        }
    }
}
