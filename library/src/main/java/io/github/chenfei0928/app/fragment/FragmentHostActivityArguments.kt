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
class FragmentHostActivityParcelableDelegate<T : Parcelable>(
    private val name: String? = null
) : ReadWriteProperty<Fragment, T> {
    private var value: T? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        return value ?: thisRef.requireActivity().intent
            .getParcelableExtra<T>(name ?: property.name)
            ?.also {
                value = it
            } ?: throw IllegalArgumentException("缺少应有的字段: ${name ?: property.name}")
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        thisRef.applyArgumentBundle {
            putParcelable(name ?: property.name, value)
        }
    }
}

class FragmentHostActivityParcelableNullableDelegate<T : Parcelable>(
    private val name: String? = null
) : ReadWriteProperty<Fragment, T?> {
    private var value: Any? = Unit

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T? {
        return if (value is Unit) {
            thisRef.requireActivity().intent
                .getParcelableExtra<T>(name ?: property.name)
                ?.also {
                    value = it
                }
        } else {
            value as T?
        }
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T?) {
        thisRef.applyArgumentBundle {
            if (value == null) {
                remove(name ?: property.name)
            } else {
                putParcelable(name ?: property.name, value)
            }
        }
    }
}

class FragmentHostActivityParcelableListDelegate<T : Parcelable>(
    private val name: String? = null
) : ReadWriteProperty<Fragment, List<T>> {
    private var value: List<T>? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): List<T> {
        return value ?: thisRef.requireActivity().intent
            .getParcelableArrayListExtra<T>(name ?: property.name)
            ?.also {
                value = it
            } ?: throw IllegalArgumentException("缺少应有的字段: ${name ?: property.name}")
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: List<T>) {
        thisRef.applyArgumentBundle {
            putParcelableArrayList(name ?: property.name, value.asArrayList())
        }
    }
}

class FragmentHostActivityStringDelegate(
    private val name: String? = null
) : ReadWriteProperty<Fragment, String> {
    private var value: String? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): String {
        return value ?: thisRef.requireActivity().intent
            .getStringExtra(name ?: property.name)
            ?.also {
                value = it
            } ?: ""
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: String) {
        thisRef.applyArgumentBundle {
            putString(name ?: property.name, value)
        }
    }
}

class FragmentHostActivityStringNullableDelegate(
    private val name: String? = null
) : ReadWriteProperty<Fragment, String?> {
    private var value: Any? = Unit

    override fun getValue(thisRef: Fragment, property: KProperty<*>): String? {
        if (value is Unit) {
            value = thisRef.requireActivity().intent.getStringExtra(name ?: property.name)
        }
        return value as? String
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: String?) {
        thisRef.applyArgumentBundle {
            putString(name ?: property.name, value)
        }
    }
}

class FragmentHostActivityIntDelegate(
    private val name: String? = null
) : ReadWriteProperty<Fragment, Int> {
    private var value: Int? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): Int {
        return value ?: thisRef.requireActivity().intent
            .getIntExtra(name ?: property.name, 0)
            .also {
                value = it
            }
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: Int) {
        thisRef.applyArgumentBundle {
            putInt(name ?: property.name, value)
        }
    }
}

class FragmentHostActivityFloatDelegate(
    private val name: String? = null
) : ReadWriteProperty<Fragment, Float> {
    private var value: Float? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): Float {
        return value ?: thisRef.requireActivity().intent
            .getFloatExtra(name ?: property.name, 0f)
            .also {
                value = it
            }
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: Float) {
        thisRef.applyArgumentBundle {
            putFloat(name ?: property.name, value)
        }
    }
}

class FragmentHostActivityBooleanDelegate(
    private val name: String? = null
) : ReadWriteProperty<Fragment, Boolean> {
    private var value: Boolean? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): Boolean {
        return value ?: thisRef.requireActivity().intent
            .getBooleanExtra(name ?: property.name, false)
            .also {
                value = it
            }
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: Boolean) {
        thisRef.applyArgumentBundle {
            putBoolean(name ?: property.name, value)
        }
    }
}
