package io.github.chenfei0928.preference.datastore

import androidx.datastore.core.DataStore
import io.github.chenfei0928.preference.BasePreferenceDataStore
import io.github.chenfei0928.preference.FieldAccessor
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
class DataStoreDataStore<T : Any>(
    private val coroutineScope: CoroutineScope,
    private val dataStore: DataStore<T>,
    private val blockingWrite: Boolean = false,
    private val fieldAccessor: FieldAccessorHelper<T> = FieldAccessorHelper.Impl(),
) : BasePreferenceDataStore<T>(fieldAccessor), FieldAccessorHelper<T> by fieldAccessor {

    override fun <V> FieldAccessor.Field<T, V>.set(value: V) {
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

    override fun <V> FieldAccessor.Field<T, V>.get(): V {
        return runBlocking(coroutineScope.coroutineContext) {
            val data = dataStore.data.first()
            get(data)
        }
    }
}
