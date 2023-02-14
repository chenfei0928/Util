package io.github.chenfei0928.app.fragment

import android.os.Parcelable
import androidx.fragment.app.Fragment
import io.github.chenfei0928.collection.asArrayList
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-23 15:46
 */
class FragmentParcelableDelegate<T : Parcelable>(
    private val name: String? = null
) : ReadWriteProperty<Fragment, T> {
    private var value: T? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        return value ?: thisRef
            .requireArguments()
            .getParcelable<T>(name ?: property.name)
            ?.also {
                value = it
            } ?: throw IllegalArgumentException("缺少应有的字段: ${name ?: property.name}")
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        this.value = value
        thisRef.applyArgumentBundle {
            putParcelable(name ?: property.name, value)
        }
    }
}

class FragmentParcelableNullableDelegate<T : Parcelable>(
    private val name: String? = null
) : ReadWriteProperty<Fragment, T?> {
    private var value: Any? = Unit

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T? {
        if (value is Unit) {
            value = thisRef
                .requireArguments()
                .getParcelable<T>(name ?: property.name)
        }
        return value as? T
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T?) {
        this.value = value
        thisRef.applyArgumentBundle {
            putParcelable(name ?: property.name, value)
        }
    }
}

class FragmentParcelableListDelegate<T : Parcelable>(
    private val name: String? = null
) : ReadWriteProperty<Fragment, List<T>> {
    private var value: List<T>? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): List<T> {
        return value ?: thisRef
            .requireArguments()
            .getParcelableArrayList<T>(name ?: property.name)
            ?.also {
                value = it
            } ?: throw IllegalArgumentException("缺少应有的字段: ${name ?: property.name}")
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: List<T>) {
        this.value = value
        thisRef.applyArgumentBundle {
            putParcelableArrayList(name ?: property.name, value.asArrayList())
        }
    }
}

class FragmentStringDelegate(
    private val name: String? = null
) : ReadWriteProperty<Fragment, String> {
    private var value: String? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): String {
        return value ?: thisRef
            .requireArguments()
            .getString(name ?: property.name, null)
            ?.also {
                value = it
            } ?: ""
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: String) {
        this.value = value
        thisRef.applyArgumentBundle {
            putString(name ?: property.name, value)
        }
    }
}

class FragmentIntDelegate(
    private val name: String? = null
) : ReadWriteProperty<Fragment, Int> {
    private var value: Int? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): Int {
        return value ?: thisRef
            .requireArguments()
            .getInt(name ?: property.name)
            .also {
                value = it
            }
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: Int) {
        this.value = value
        thisRef.applyArgumentBundle {
            putInt(name ?: property.name, value)
        }
    }
}

class FragmentBooleanDelegate(
    private val name: String? = null
) : ReadWriteProperty<Fragment, Boolean> {
    private var value: Boolean? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): Boolean {
        return value ?: thisRef
            .requireArguments()
            .getBoolean(name ?: property.name)
            .also {
                value = it
            }
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: Boolean) {
        this.value = value
        thisRef.applyArgumentBundle {
            putBoolean(name ?: property.name, value)
        }
    }
}
