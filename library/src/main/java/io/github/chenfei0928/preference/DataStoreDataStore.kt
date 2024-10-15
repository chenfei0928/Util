package io.github.chenfei0928.preference

import androidx.datastore.core.DataStore
import androidx.preference.PreferenceDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * 支持使用 [DataStore] 来存储首选项
 *
 * @param blockingWrite true为阻塞方式以 [runBlocking] 写入，false为使用 [launch] 写入
 *
 * @author chenf()
 * @date 2024-08-13 18:18
 */
@Suppress("TooManyFunctions")
class DataStoreDataStore<T : Any>(
    private val coroutineScope: CoroutineScope,
    private val dataStore: DataStore<T>,
    private val blockingWrite: Boolean = false,
    private val fieldAccessor: FieldAccessorHelper<T> = FieldAccessorHelper.Impl(),
) : PreferenceDataStore(), FieldAccessorHelper<T> by fieldAccessor {

    //<editor-fold desc="快速访问字段扩展" defaultstatus="collapsed">
    private fun <V> FieldAccessor.Field<T, V>.set(value: V) {
        if (blockingWrite) {
            runBlocking(coroutineScope.coroutineContext) {
                dataStore.updateData {
                    set(it, value)
                }
            }
        } else {
            coroutineScope.launch {
                dataStore.updateData {
                    set(it, value)
                }
            }
        }
    }

    private fun <V> FieldAccessor.Field<T, V>.get(): V {
        return runBlocking(coroutineScope.coroutineContext) {
            val data = dataStore.data.first()
            get(data)
        }
    }
    //</editor-fold>

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
