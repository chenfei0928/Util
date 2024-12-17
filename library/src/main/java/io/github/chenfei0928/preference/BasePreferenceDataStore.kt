package io.github.chenfei0928.preference

import androidx.preference.PreferenceDataStore
import kotlin.collections.toMutableSet

/**
 * @author chenf()
 * @date 2024-12-17 15:41
 */
@Suppress("TooManyFunctions")
abstract class BasePreferenceDataStore<T : Any>(
    private val fieldAccessor: FieldAccessor<T> = FieldAccessor.Impl(),
) : PreferenceDataStore(), FieldAccessor<T> by fieldAccessor {

    protected abstract fun <V> FieldAccessor.Field<T, V>.set(value: V)
    protected abstract fun <V> FieldAccessor.Field<T, V>.get(): V

    //<editor-fold desc="put\get" defaultstatus="collapsed">
    override fun putString(key: String, value: String?) {
        findByName<String?>(key).set(value)
    }

    override fun putStringSet(key: String, values: MutableSet<String>?) {
        findByName<Set<String>?>(key).set(values)
    }

    override fun putInt(key: String, value: Int) {
        findByName<Int>(key).set(value)
    }

    override fun putLong(key: String, value: Long) {
        findByName<Long>(key).set(value)
    }

    override fun putFloat(key: String, value: Float) {
        findByName<Float>(key).set(value)
    }

    override fun putBoolean(key: String, value: Boolean) {
        findByName<Boolean>(key).set(value)
    }

    override fun getString(key: String, defValue: String?): String? {
        return findByName<String?>(key).get() ?: defValue
    }

    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? {
        return findByName<Set<String>?>(key).get()?.toMutableSet() ?: defValues
    }

    override fun getInt(key: String, defValue: Int): Int {
        return findByName<Int?>(key).get() ?: defValue
    }

    override fun getLong(key: String, defValue: Long): Long {
        return findByName<Long?>(key).get() ?: defValue
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return findByName<Float?>(key).get() ?: defValue
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return findByName<Boolean?>(key).get() ?: defValue
    }
    //</editor-fold>
}
