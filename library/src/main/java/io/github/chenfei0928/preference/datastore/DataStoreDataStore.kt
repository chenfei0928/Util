package io.github.chenfei0928.preference.datastore

import androidx.collection.ArraySet
import androidx.datastore.core.DataStore
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.preference.BasePreferenceDataStore
import io.github.chenfei0928.preference.FieldAccessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KMutableProperty1

/**
 * 支持使用 [DataStore] 来存储首选项
 *
 *  [DataStore.updateData] 的数据刷写方式要求每次返回一个新实例，
 *  不能使用[KMutableProperty1]来优化性能，必须使用[copyFunc]写入数据
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
            runBlocking(coroutineScope.coroutineContext + Dispatchers.IO) {
                dataStore.updateData {
                    val vType = vType
                    if (vType is PreferenceType.EnumNameString<*>) {
                        set(it, vType.forName(value as String) as V)
                    } else if (vType is PreferenceType.EnumNameStringSet<*>) {
                        set(it, vType.forName(value as Collection<String>) as V)
                    } else {
                        set(it, value)
                    }
                }
            }
        } else {
            coroutineScope.launch {
                dataStore.updateData {
                    val vType = vType
                    if (vType is PreferenceType.EnumNameString<*>) {
                        set(it, vType.forName(value as String) as V)
                    } else if (vType is PreferenceType.EnumNameStringSet<*>) {
                        set(it, vType.forName(value as Collection<String>) as V)
                    } else {
                        set(it, value)
                    }
                }
            }
        }
    }

    override fun <V> FieldAccessor.Field<T, V>.get(): V {
        return runBlocking(coroutineScope.coroutineContext + Dispatchers.IO) {
            val data = dataStore.data.first()
            if (vType is PreferenceType.EnumNameString<*>) {
                (get(data) as Enum<*>).name as V
            } else if (vType is PreferenceType.EnumNameStringSet<*>) {
                (get(data) as Collection<Enum<*>>).mapTo(ArraySet()) { it.name } as V
            } else {
                get(data)
            }
        }
    }
}
