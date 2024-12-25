package io.github.chenfei0928.content.sp.saver

import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import io.github.chenfei0928.content.sp.registerOnSharedPreferenceChangeListener
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

@Suppress("UNCHECKED_CAST")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
private fun <SpSaver : AbsSpSaver<SpSaver>, V> KProperty<V>.get(owner: SpSaver): V {
    return when (this) {
        is KProperty0<V> -> get()
        is KProperty1<*, V> -> (this as KProperty1<SpSaver, V>).get(owner)
        else -> throw IllegalArgumentException("not support KProperty2 or other Property: $this")
    }
}

/**
 * 监听sp字段的变化并通知回调进行更新，可以方便的传入字段以保证类型安全和一个监听器只监听一个值的变化
 *
 * @param owner 生命周期宿主（用于同步生命周期变化以取消监听）
 * @param property 要监听的字段（用于错误日志反馈和获取该字段sp存储的key）
 * @param listener 该字段变化的监听器（该sp存储的值发生变化时会触发回调，传入的值根据传入的[property]获取而非直接通过sp获取值）
 */
fun <SpSaver : AbsSpSaver<SpSaver>, V> SpSaver.registerOnSharedPreferenceChangeListener(
    owner: LifecycleOwner, property: KProperty<V>, listener: (V) -> Unit,
) {
    val spSaver = this
    val key = dataStore.getSpKeyByProperty(property)
    // 如果该字段是sp委托，获取其sp存储的key
    listener(property.get(this))
    // 监听sp变化并调用回调
    AbsSpSaver.getSp(this).registerOnSharedPreferenceChangeListener(owner) {
        if (it == key) listener(property.get(spSaver))
    }
}

/**
 * 监听sp属性变化转换，并为liveData使用
 *
 * @param owner 生命周期宿主
 * @param property 需要监听的属性字段
 */
fun <SpSaver : AbsSpSaver<SpSaver>, V> SpSaver.toLiveData(
    owner: LifecycleOwner, property: KProperty<V>,
): LiveData<V> = object : SpValueLiveData<V>(
    owner, AbsSpSaver.getSp(this), dataStore.getSpKeyByProperty(property)
) {
    override fun valueGetter(): V = property.get(this@toLiveData)
}.also {
    owner.lifecycle.addObserver(it)
    AbsSpSaver.getSp(this).registerOnSharedPreferenceChangeListener(it)
}
