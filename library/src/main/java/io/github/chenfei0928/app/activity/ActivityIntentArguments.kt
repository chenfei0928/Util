package io.github.chenfei0928.app.activity

import android.app.Activity
import android.os.Parcelable
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-23 15:46
 */
class ActivityNullableParcelableDelegate<T : Parcelable>(
    private val name: String
) : ReadOnlyProperty<Activity, T?> {
    private var value: T? = null

    override fun getValue(thisRef: Activity, property: KProperty<*>): T? {
        return value ?: thisRef.intent
            .getParcelableExtra<T>(name)
            ?.also {
                value = it
            }
    }
}

class ActivityParcelableListDelegate<T : Parcelable>(
    private val name: String
) : ReadOnlyProperty<Activity, List<T>> {
    private var value: List<T>? = null

    override fun getValue(thisRef: Activity, property: KProperty<*>): List<T> {
        return value ?: thisRef.intent
            .getParcelableArrayListExtra<T>(name)
            ?.also {
                value = it
            } ?: emptyList()
    }
}

class ActivityStringDelegate(
    private val name: String
) : ReadOnlyProperty<Activity, String> {
    private var value: String? = null

    override fun getValue(thisRef: Activity, property: KProperty<*>): String {
        return value ?: thisRef.intent
            .getStringExtra(name)
            ?.also {
                value = it
            } ?: ""
    }
}

class ActivityIntDelegate(
    private val name: String, private val defaultValue: Int = 0
) : ReadOnlyProperty<Activity, Int> {
    private var value: Int? = null

    override fun getValue(thisRef: Activity, property: KProperty<*>): Int {
        return value ?: thisRef.intent
            .getIntExtra(name, defaultValue)
            .also {
                value = it
            }
    }
}

class ActivityFloatDelegate(
    private val name: String, private val defaultValue: Float = 0f
) : ReadOnlyProperty<Activity, Float> {
    private var value: Float? = null

    override fun getValue(thisRef: Activity, property: KProperty<*>): Float {
        return value ?: thisRef.intent
            .getFloatExtra(name, defaultValue)
            .also {
                value = it
            }
    }
}

class ActivityBooleanDelegate(
    private val name: String
) : ReadOnlyProperty<Activity, Boolean> {
    private var value: Boolean? = null

    override fun getValue(thisRef: Activity, property: KProperty<*>): Boolean {
        return value ?: thisRef.intent
            .getBooleanExtra(name, false)
            .also {
                value = it
            }
    }
}
