package io.github.chenfei0928.preference

import androidx.datastore.core.DataStore
import io.github.chenfei0928.preference.base.BasePreferenceDataStore
import io.github.chenfei0928.preference.base.DataCopyClassFieldAccessor
import io.github.chenfei0928.preference.base.FieldAccessor
import io.github.chenfei0928.preference.base.MutableFieldAccessor
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
 * 支持使用 [DataStore] 来存储首选项的首选项值访问
 *
 * [DataStore]初次加载数据耗时较久（约300-400ms），建议在提前已经获取过该字段
 *
 * [DataStore.updateData] 的数据刷写方式要求每次返回一个新实例，
 * 不能使用[KMutableProperty1.set]来优化性能，必须使用[DataCopyClassFieldAccessor.copyFunc]写入数据，
 * 即不建议 [fieldAccessor] 使用 [MutableFieldAccessor] 的实例
 *
 * @param blockingWrite true为阻塞方式以 [runBlocking] 写入，false为使用 [launch] 写入，阻塞方式写入时耗时较久
 * @param fieldAccessor 字段访问器，用于存储实例字段存取器
 *
 * @author chenf()
 * @date 2024-08-13 18:18
 */
@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class DataStorePreferenceDataStore<T : Any>(
    private val coroutineScope: CoroutineScope,
    private val dataStore: DataStore<T>,
    private val blockingWrite: Boolean = false,
    private val fieldAccessor: DataCopyClassFieldAccessor<T> = DataCopyClassFieldAccessor.Impl(true),
) : BasePreferenceDataStore<T>(fieldAccessor), DataCopyClassFieldAccessor<T> by fieldAccessor {
    // 缓存dataStore字段最后的值，否则每次 dataStore.data.first() 耗时较久
    private val field: StateFlow<T?> = dataStore.data.stateIn(
        CoroutineScope(coroutineScope.coroutineContext + Dispatchers.IO),
        SharingStarted.Eagerly,
        null
    )

    override fun <V> FieldAccessor.Field<T, V>.set(value: V) {
        if (blockingWrite) {
            runBlocking(coroutineScope.coroutineContext + Dispatchers.IO) {
                suspendSet(value)
            }
        } else {
            coroutineScope.launch(Dispatchers.IO) {
                suspendSet(value)
            }
        }
    }

    private suspend fun <V> FieldAccessor.Field<T, V>.suspendSet(value: V) {
        dataStore.updateData {
            setValue(it, value)
        }
    }

    override fun <V> FieldAccessor.Field<T, V>.get(): V {
        val data = field.value ?: runBlocking(coroutineScope.coroutineContext + Dispatchers.IO) {
            field.filterNotNull().first()
        }
        return getValue(data)
    }
}
