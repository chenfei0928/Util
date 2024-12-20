package io.github.chenfei0928.preference.datastore

import androidx.datastore.core.DataStore
import io.github.chenfei0928.os.Debug
import io.github.chenfei0928.preference.BasePreferenceDataStore
import io.github.chenfei0928.preference.FieldAccessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KMutableProperty1

/**
 * 支持使用 [DataStore] 来存储首选项
 *
 * [DataStore]初次加载数据耗时较久（约300-400ms），建议在提前已经获取过该字段
 *
 *  [DataStore.updateData] 的数据刷写方式要求每次返回一个新实例，
 *  不能使用[KMutableProperty1]来优化性能，必须使用[copyFunc]写入数据
 *
 * @param blockingWrite true为阻塞方式以 [runBlocking] 写入，false为使用 [launch] 写入，阻塞方式写入时耗时较久
 *
 * @author chenf()
 * @date 2024-08-13 18:18
 */
class DataStorePreferenceDataStore<T : Any>(
    private val coroutineScope: CoroutineScope,
    private val dataStore: DataStore<T>,
    private val blockingWrite: Boolean = false,
    private val fieldAccessor: FieldAccessorHelper<T> = FieldAccessorHelper.Impl(true),
) : BasePreferenceDataStore<T>(fieldAccessor), FieldAccessorHelper<T> by fieldAccessor {
    // 缓存dataStore字段最后的值，否则每次 dataStore.data.first() 耗时较久
    private val field: StateFlow<T?> = dataStore.data.stateIn(
        CoroutineScope(coroutineScope.coroutineContext + Dispatchers.IO),
        SharingStarted.Eagerly,
        null
    )

    override fun <V> FieldAccessor.Field<T, V>.set(value: V) {
        if (blockingWrite) {
            Debug.countTime(TAG, "set $name blockingWrite") {
                runBlocking(coroutineScope.coroutineContext + Dispatchers.IO) {
                    suspendSet(value)
                }
            }
        } else {
            coroutineScope.launch(Dispatchers.IO) {
                Debug.countTime(TAG, "set $name async") {
                    suspendSet(value)
                }
            }
        }
    }

    private suspend fun <V> FieldAccessor.Field<T, V>.suspendSet(value: V) {
        dataStore.updateData {
            setValue(it, value)
        }
    }

    override fun <V> FieldAccessor.Field<T, V>.get(): V = Debug.countTime(TAG, "get $name") {
        val data = field.value ?: runBlocking(coroutineScope.coroutineContext + Dispatchers.IO) {
            field.filterNotNull().first()
        }
        getValue(data)
    }

    companion object {
        private const val TAG = "DataStoreDataStore"
    }
}
