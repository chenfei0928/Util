package io.github.chenfei0928.app.activity

import android.app.Activity
import android.os.Parcelable
import androidx.core.content.IntentCompat
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaType

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-23 15:46
 */
class ActivityNullableParcelableDelegate<T : Parcelable>(
    name: String? = null
) : BaseArgumentDelegate<Activity, T?>(name, null) {
    override fun getValueImpl(thisRef: Activity, property: KProperty<*>): T? {
        return IntentCompat.getParcelableExtra(
            thisRef.intent,
            name ?: property.name,
            property.returnType.javaType as Class<T>
        )
    }
}

class ActivityParcelableListDelegate<T : Parcelable>(
    name: String? = null
) : BaseArgumentDelegate<Activity, List<T>>(name) {
    override fun getValueImpl(thisRef: Activity, property: KProperty<*>): List<T> {
        return IntentCompat.getParcelableArrayListExtra(
            thisRef.intent,
            name ?: property.name,
            property.returnType.arguments[0].type?.javaType as Class<T>
        ) ?: emptyList()
    }
}

class ActivityStringDelegate(
    name: String? = null
) : BaseArgumentDelegate<Activity, String>(name) {
    override fun getValueImpl(thisRef: Activity, property: KProperty<*>): String {
        return thisRef.intent.getStringExtra(
            name ?: property.name
        ) ?: ""
    }
}

class ActivityIntDelegate(
    name: String? = null, private val defaultValue: Int = 0
) : BaseArgumentDelegate<Activity, Int>(name) {
    override fun getValueImpl(thisRef: Activity, property: KProperty<*>): Int {
        return thisRef.intent.getIntExtra(
            name ?: property.name, defaultValue
        )
    }
}

class ActivityFloatDelegate(
    name: String? = null, private val defaultValue: Float = 0f
) : BaseArgumentDelegate<Activity, Float>(name) {
    override fun getValueImpl(thisRef: Activity, property: KProperty<*>): Float {
        return thisRef.intent.getFloatExtra(
            name ?: property.name, defaultValue
        )
    }
}

class ActivityBooleanDelegate(
    name: String? = null
) : BaseArgumentDelegate<Activity, Boolean>(name) {
    override fun getValueImpl(thisRef: Activity, property: KProperty<*>): Boolean {
        return thisRef.intent.getBooleanExtra(
            name ?: property.name, false
        )
    }
}
