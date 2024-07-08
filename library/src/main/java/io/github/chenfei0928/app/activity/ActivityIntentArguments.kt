package io.github.chenfei0928.app.activity

import android.app.Activity
import android.os.Parcelable
import androidx.core.content.IntentCompat
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaType

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-23 15:46
 */
class ActivityNullableParcelableDelegate<T : Parcelable>(
    private val name: String? = null
) : ReadOnlyProperty<Activity, T?> {
    private var value: T? = null

    override fun getValue(thisRef: Activity, property: KProperty<*>): T? {
        return value ?: IntentCompat.getParcelableExtra(
            thisRef.intent,
            name ?: property.name,
            property.returnType.javaType as Class<T>
        )?.also { value = it }
    }
}

class ActivityParcelableListDelegate<T : Parcelable>(
    private val name: String? = null
) : ReadOnlyProperty<Activity, List<T>> {
    private var value: List<T>? = null

    override fun getValue(thisRef: Activity, property: KProperty<*>): List<T> {
        return value ?: IntentCompat.getParcelableArrayListExtra(
            thisRef.intent,
            name ?: property.name,
            property.returnType.arguments[0].type?.javaType as Class<T>
        )?.also { value = it } ?: emptyList()
    }
}

class ActivityStringDelegate(
    private val name: String? = null
) : ReadOnlyProperty<Activity, String> {
    private var value: String? = null

    override fun getValue(thisRef: Activity, property: KProperty<*>): String {
        return value ?: thisRef.intent.getStringExtra(
            name ?: property.name
        )?.also { value = it } ?: ""
    }
}

class ActivityIntDelegate(
    private val name: String? = null, private val defaultValue: Int = 0
) : ReadOnlyProperty<Activity, Int> {
    private var value: Int? = null

    override fun getValue(thisRef: Activity, property: KProperty<*>): Int {
        return value ?: thisRef.intent.getIntExtra(
            name ?: property.name, defaultValue
        ).also { value = it }
    }
}

class ActivityFloatDelegate(
    private val name: String? = null, private val defaultValue: Float = 0f
) : ReadOnlyProperty<Activity, Float> {
    private var value: Float? = null

    override fun getValue(thisRef: Activity, property: KProperty<*>): Float {
        return value ?: thisRef.intent.getFloatExtra(
            name ?: property.name, defaultValue
        ).also { value = it }
    }
}

class ActivityBooleanDelegate(
    private val name: String? = null
) : ReadOnlyProperty<Activity, Boolean> {
    private var value: Boolean? = null

    override fun getValue(thisRef: Activity, property: KProperty<*>): Boolean {
        return value ?: thisRef.intent.getBooleanExtra(
            name ?: property.name, false
        ).also { value = it }
    }
}
