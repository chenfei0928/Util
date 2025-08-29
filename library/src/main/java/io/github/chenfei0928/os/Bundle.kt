package io.github.chenfei0928.os

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import androidx.core.content.IntentCompat
import androidx.core.os.BundleCompat
import io.github.chenfei0928.app.fragment.ArgumentDelegate
import kotlin.reflect.KProperty

/**
 * @author chenf()
 * @date 2024-07-03 11:28
 */

/**
 * 对于不设置 [ArgumentDelegate.name] 、 [BundleDelegate.name] 的委托属性上，
 * 可以直接调用该方法获取其是否已经设置到 [Bundle] 中。
 */
operator fun Bundle.contains(property: KProperty<*>): Boolean =
    containsKey(property.name)

inline fun <reified T> Bundle.getParcelableCompat(key: String): T? {
    classLoader = T::class.java.classLoader
    return BundleCompat.getParcelable(this, key, T::class.java)
}

inline fun <reified T : Parcelable> Bundle.getParcelableArrayCompat(key: String): Array<T>? {
    classLoader = T::class.java.classLoader
    return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        getParcelableArray(key, T::class.java)
    } else BundleCompat.getParcelableArray(this, key, T::class.java)?.let { array ->
        if (array.isArrayOf<T>()) {
            @Suppress("UNCHECKED_CAST")
            array as Array<T>
        } else {
            Array(array.size) { array[it] as T }
        }
    }
}

inline fun <reified T> Bundle.getParcelableArrayListCompat(key: String): ArrayList<T>? {
    classLoader = T::class.java.classLoader
    return BundleCompat.getParcelableArrayList(this, key, T::class.java)
}

inline fun <reified T> Bundle.getSparseParcelableArrayCompat(key: String): SparseArray<T>? {
    classLoader = T::class.java.classLoader
    return BundleCompat.getSparseParcelableArray(this, key, T::class.java)
}

inline fun <reified T> Intent.getParcelableExtraCompat(key: String): T? {
    setExtrasClassLoader(T::class.java.classLoader)
    return IntentCompat.getParcelableExtra(this, key, T::class.java)
}

inline fun <reified T : Parcelable> Intent.getParcelableArrayExtraCompat(key: String): Array<T>? {
    setExtrasClassLoader(T::class.java.classLoader)
    return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        getParcelableArrayExtra(key, T::class.java)
    } else IntentCompat.getParcelableArrayExtra(this, key, T::class.java)?.let { array ->
        if (array.isArrayOf<T>()) {
            @Suppress("UNCHECKED_CAST")
            array as Array<T>
        } else {
            Array(array.size) { array[it] as T }
        }
    }
}

inline fun <reified T> Intent.getParcelableArrayListExtraCompat(key: String): ArrayList<T>? {
    setExtrasClassLoader(T::class.java.classLoader)
    return IntentCompat.getParcelableArrayListExtra(this, key, T::class.java)
}
