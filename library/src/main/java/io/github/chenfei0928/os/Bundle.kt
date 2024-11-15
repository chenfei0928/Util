package io.github.chenfei0928.os

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import androidx.core.content.IntentCompat
import androidx.core.os.BundleCompat

/**
 * @author chenf()
 * @date 2024-07-03 11:28
 */
inline fun <reified T> Bundle.getParcelableCompat(key: String): T? {
    return BundleCompat.getParcelable(this, key, T::class.java)
}

inline fun <reified T : Parcelable> Bundle.getParcelableArrayCompat(key: String): Array<T>? {
    return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        getParcelableArray(key, T::class.java)
    } else BundleCompat.getParcelableArray(this, key, T::class.java)?.let { array ->
        if (array.isArrayOf<T>()) {
            array as Array<T>
        } else {
            Array(array.size) { array[it] as T }
        }
    }
}

inline fun <reified T> Bundle.getParcelableArrayListCompat(key: String): ArrayList<T>? {
    return BundleCompat.getParcelableArrayList(this, key, T::class.java)
}

inline fun <reified T> Bundle.getSparseParcelableArrayCompat(key: String): SparseArray<T>? {
    return BundleCompat.getSparseParcelableArray(this, key, T::class.java)
}

inline fun <reified T> Intent.getParcelableExtraCompat(key: String): T? {
    return IntentCompat.getParcelableExtra(this, key, T::class.java)
}

inline fun <reified T : Parcelable> Intent.getParcelableArrayExtraCompat(key: String): Array<T>? {
    return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        getParcelableArrayExtra(key, T::class.java)
    } else IntentCompat.getParcelableArrayExtra(this, key, T::class.java)?.let { array ->
        if (array.isArrayOf<T>()) {
            array as Array<T>
        } else {
            Array(array.size) { array[it] as T }
        }
    }
}

inline fun <reified T> Intent.getParcelableArrayListExtraCompat(key: String): ArrayList<T>? {
    return IntentCompat.getParcelableArrayListExtra(this, key, T::class.java)
}
