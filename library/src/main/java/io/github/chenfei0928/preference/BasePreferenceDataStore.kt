package io.github.chenfei0928.preference

import androidx.collection.ArraySet
import androidx.preference.PreferenceDataStore
import io.github.chenfei0928.content.sp.saver.PreferenceType
import kotlin.collections.toMutableSet

/**
 * @author chenf()
 * @date 2024-12-17 15:41
 */
@Suppress("TooManyFunctions")
abstract class BasePreferenceDataStore<T : Any>(
    private val fieldAccessor: FieldAccessor<T> = FieldAccessor.Impl(false),
) : PreferenceDataStore(), FieldAccessor<T> by fieldAccessor {

    protected abstract fun <V> FieldAccessor.Field<T, V>.set(value: V)
    protected abstract fun <V> FieldAccessor.Field<T, V>.get(): V

    protected fun <V> FieldAccessor.Field<T, V>.setValue(it: T, value: V): T {
        val vType = vType
        return if (vType is PreferenceType.EnumNameString<*>) {
            set(it, vType.forName(value as String) as V)
        } else if (vType is PreferenceType.EnumNameStringSet<*>) {
            set(it, vType.forName(value as Collection<String>) as V)
        } else {
            set(it, value)
        }
    }

    protected fun <V> FieldAccessor.Field<T, V>.getValue(data: T): V {
        return if (vType is PreferenceType.EnumNameString<*>) {
            (get(data) as Enum<*>).name as V
        } else if (vType is PreferenceType.EnumNameStringSet<*>) {
            (get(data) as Collection<Enum<*>>).mapTo(ArraySet()) { it.name } as V
        } else {
            get(data)
        }
    }

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
