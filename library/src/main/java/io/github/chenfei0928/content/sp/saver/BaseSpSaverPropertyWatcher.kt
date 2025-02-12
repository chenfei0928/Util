package io.github.chenfei0928.content.sp.saver

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import io.github.chenfei0928.content.sp.registerOnSharedPreferenceChangeListener
import io.github.chenfei0928.content.sp.toLiveData
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

internal inline fun <SpSaver : BaseSpSaver<SpSaver>> SpSaver.internalRegisterOnSharedPreferenceChangeListener(
    owner: LifecycleOwner, filterKey: String, crossinline listener: () -> Unit,
) {
    listener()
    // 监听sp变化并调用回调
    AbsSpSaver.getSp(this).registerOnSharedPreferenceChangeListener(owner, filterKey) {
        listener()
    }
}

/**
 * 监听sp字段的变化并通知回调进行更新，可以方便的传入字段以保证类型安全和一个监听器只监听一个值的变化
 *
 * @param owner 生命周期宿主（用于同步生命周期变化以取消监听）
 * @param property 要监听的字段（用于错误日志反馈和获取该字段sp存储的key）
 * @param listener 该字段变化的监听器（该sp存储的值发生变化时会触发回调，传入的值根据传入的[property]获取而非直接通过sp获取值）
 */
fun <SpSaver : BaseSpSaver<SpSaver>, V> SpSaver.registerOnSpPropertyChangeListener(
    owner: LifecycleOwner, property: KProperty0<V>, listener: (V) -> Unit,
): Unit = internalRegisterOnSharedPreferenceChangeListener(
    owner, dataStore.getSpKeyByProperty(property)
) { listener(property.get()) }

/**
 * 监听sp字段的变化并通知回调进行更新，可以方便的传入字段以保证类型安全和一个监听器只监听一个值的变化
 *
 * @param owner 生命周期宿主（用于同步生命周期变化以取消监听）
 * @param property 要监听的字段（用于错误日志反馈和获取该字段sp存储的key）
 * @param listener 该字段变化的监听器（该sp存储的值发生变化时会触发回调，传入的值根据传入的[property]获取而非直接通过sp获取值）
 */
fun <SpSaver : BaseSpSaver<SpSaver>, V> SpSaver.registerOnSpPropertyChangeListener(
    owner: LifecycleOwner, property: KProperty1<SpSaver, V>, listener: (V) -> Unit,
): Unit = internalRegisterOnSharedPreferenceChangeListener(
    owner, dataStore.getSpKeyByProperty(property)
) { listener(property.get(this)) }

/**
 * 监听sp属性变化转换，并为liveData使用
 *
 * @param property 需要监听的属性字段
 */
fun <SpSaver : BaseSpSaver<SpSaver>, V> SpSaver.toLiveData(
    property: KProperty0<V>,
): LiveData<V> = AbsSpSaver.getSp(this@toLiveData).toLiveData(
    dataStore.getSpKeyByProperty(property)
) { property.get() }

/**
 * 监听sp属性变化转换，并为liveData使用
 *
 * @param property 需要监听的属性字段
 */
fun <SpSaver : BaseSpSaver<SpSaver>, V> SpSaver.toLiveData(
    property: KProperty1<SpSaver, V>,
): LiveData<V> = AbsSpSaver.getSp(this@toLiveData).toLiveData(
    dataStore.getSpKeyByProperty(property)
) { property.get(this@toLiveData) }
