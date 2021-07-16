package com.chenfei.util.kotlin

import android.os.Parcelable
import androidx.fragment.app.Fragment
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-23 15:46
 */
class FragmentParcelableDelegate<T : Parcelable>(
    private val name: String
) : ReadWriteProperty<Fragment, T> {
    private var value: T? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        return value ?: thisRef
            .requireArguments()
            .getParcelable<T>(name)
            ?.also {
                value = it
            } ?: throw IllegalArgumentException("缺少应有的字段: $name")
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        thisRef.applyArgumentBundle {
            putParcelable(name, value)
        }
    }
}

class FragmentParcelableNullableDelegate<T : Parcelable>(
    private val name: String
) : ReadWriteProperty<Fragment, T?> {
    private var value: Any? = Unit

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T? {
        if (value is Unit) {
            value = thisRef
                .requireArguments()
                .getParcelable<T>(name)
        }
        return value as? T
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T?) {
        thisRef.applyArgumentBundle {
            putParcelable(name, value)
        }
    }
}

class FragmentParcelableListDelegate<T : Parcelable>(
    private val name: String
) : ReadWriteProperty<Fragment, List<T>> {
    private var value: List<T>? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): List<T> {
        return value ?: thisRef
            .requireArguments()
            .getParcelableArrayList<T>(name)
            ?.also {
                value = it
            } ?: throw IllegalArgumentException("缺少应有的字段: $name")
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: List<T>) {
        thisRef.applyArgumentBundle {
            putParcelableList(name, value)
        }
    }
}

class FragmentStringDelegate(
    private val name: String
) : ReadWriteProperty<Fragment, String> {
    private var value: String? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): String {
        return value ?: thisRef
            .requireArguments()
            .getString(name, null)
            ?.also {
                value = it
            } ?: ""
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: String) {
        thisRef.applyArgumentBundle {
            putString(name, value)
        }
    }
}

class FragmentIntDelegate(
    private val name: String
) : ReadWriteProperty<Fragment, Int> {
    private var value: Int? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): Int {
        return value ?: thisRef
            .requireArguments()
            .getInt(name)
            .also {
                value = it
            }
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: Int) {
        thisRef.applyArgumentBundle {
            putInt(name, value)
        }
    }
}

class FragmentBooleanDelegate(
    private val name: String
) : ReadWriteProperty<Fragment, Boolean> {
    private var value: Boolean? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): Boolean {
        return value ?: thisRef
            .requireArguments()
            .getBoolean(name)
            .also {
                value = it
            }
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: Boolean) {
        thisRef.applyArgumentBundle {
            putBoolean(name, value)
        }
    }
}
