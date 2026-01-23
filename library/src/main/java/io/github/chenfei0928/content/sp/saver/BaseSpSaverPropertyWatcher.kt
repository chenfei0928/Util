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
 * 监听sp属性变化转换，并为liveData使用
 *
 * @param property 需要监听的属性字段
 */
fun <SpSaver : BaseSpSaver<SpSaver>, V> SpSaver.toLiveData(
    property: KProperty0<V>,
): LiveData<V> = AbsSpSaver.getSp(this@toLiveData).toLiveData(
    fieldAccessorCache.getSpKeyByProperty(property)
) { property.get() }

/**
 * 监听sp属性变化转换，并为liveData使用
 *
 * @param property 需要监听的属性字段
 */
fun <SpSaver : BaseSpSaver<SpSaver>, V> SpSaver.toLiveData(
    property: KProperty1<SpSaver, V>,
): LiveData<V> = AbsSpSaver.getSp(this@toLiveData).toLiveData(
    fieldAccessorCache.getSpKeyByProperty(property)
) { property.get(this@toLiveData) }
