package io.github.chenfei0928.content.sp.saver

import android.os.Looper
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.github.chenfei0928.concurrent.ExecutorUtil
import io.github.chenfei0928.content.sp.registerOnSharedPreferenceChangeListener
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.isAccessible

/**
 * 监听sp字段的变化并通知回调进行更新，可以方便的传入字段以保证类型安全和一个监听器只监听一个值的变化
 *
 * @param owner 生命周期宿主（用于同步生命周期变化以取消监听）
 * @param key 要监听的字段（用于错误日志反馈和获取该字段sp存储的key）
 * @param valueGetter 该字段值的获取方法（通过字段反射获取或其它）
 * @param listener 该字段变化的监听器（该sp存储的值发生变化时会触发回调，传入的值根据传入的[valueGetter]获取而非直接通过sp获取值）
 */
inline fun <SpSaver : AbsSpSaver, V> SpSaver.internalInlineRegisterOnSharedPreferenceChangeListener(
    owner: LifecycleOwner,
    key: String,
    crossinline valueGetter: () -> V,
    crossinline listener: (V) -> Unit,
) {
    // 如果该字段是sp委托，获取其sp存储的key
    listener(valueGetter())
    // 监听sp变化并调用回调
    AbsSpSaver.getSp(this)
        .registerOnSharedPreferenceChangeListener(owner) {
            if (it == key) listener(valueGetter())
        }
}

/**
 * 通过类的属性引用获取该sp字段的key
 */
@Suppress("UNCHECKED_CAST")
fun <SpSaver : AbsSpSaver> SpSaver.getPropertySpKeyName(
    property: KProperty<*>,
    accessDelegateName: Boolean,
): String = if (!accessDelegateName) {
    property.name
} else {
    // 获取该字段的委托
    property.isAccessible = true
    val delegate = when (property) {
        is KProperty0<*> -> property.getDelegate()
        is KProperty1<*, *> -> (property as KProperty1<SpSaver, *>).getDelegate(this)
        else -> throw IllegalArgumentException("not support KProperty2 or other Property: $property")
    }
    // 判断该字段的委托
    require(delegate is AbsSpSaver.AbsSpDelegate<*>) {
        "Property($property) must is delegate subclass as AbsSpSaver.AbsSpDelegate0: $delegate"
    }
    // 如果该字段是sp委托，获取其sp存储的key
    delegate.obtainDefaultKey(property)
}

@Suppress("UNCHECKED_CAST")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <SpSaver : AbsSpSaver, V> KProperty<V>.get(owner: SpSaver): V {
    return when (this) {
        is KProperty0<V> -> get()
        is KProperty1<*, V> -> (this as KProperty1<SpSaver, V>).get(owner)
        else -> throw IllegalArgumentException("not support KProperty2 or other Property: $this")
    }
}

//<editor-fold desc="SpSaver属性监听器/LiveData，KProperty的在实例上属性引用" defaultstate="collapsed">
/**
 * 监听sp字段的变化并通知回调进行更新，可以方便的传入字段以保证类型安全和一个监听器只监听一个值的变化
 */
inline fun <SpSaver : AbsSpSaver, V> SpSaver.internalInlineRegisterOnSharedPreferenceChangeListener(
    owner: LifecycleOwner,
    property: KProperty<V>,
    accessDelegateName: Boolean = false,
    crossinline listener: (V) -> Unit,
) = internalInlineRegisterOnSharedPreferenceChangeListener(
    owner, getPropertySpKeyName(property, accessDelegateName), { property.get(this) }, listener
)

fun <SpSaver : AbsSpSaver, V> SpSaver.registerOnSharedPreferenceChangeListener(
    owner: LifecycleOwner,
    property: KProperty<V>,
    accessDelegateName: Boolean = false,
    listener: (V) -> Unit,
) = internalInlineRegisterOnSharedPreferenceChangeListener(
    owner, getPropertySpKeyName(property, accessDelegateName), { property.get(this) }, listener
)

/**
 * 监听sp属性变化转换，并为liveData使用
 *
 * @param owner 生命周期宿主
 * @param property 需要监听的属性字段
 */
inline fun <SpSaver : AbsSpSaver, V, R> SpSaver.internalInlineLiveDataPropertyChange(
    owner: LifecycleOwner,
    property: KProperty<V>,
    accessDelegateName: Boolean = false,
    crossinline mapCast: (V) -> R,
): LiveData<R> = MutableLiveData<R>().apply {
    // 监听sp属性变化
    internalInlineRegisterOnSharedPreferenceChangeListener(owner, property, accessDelegateName) {
        // liveData只允许主线程更新值，将值的更新发送到主线程执行
        if (Looper.myLooper() == Looper.getMainLooper()) {
            value = mapCast(it)
        } else {
            ExecutorUtil.postToUiThread {
                value = mapCast(it)
            }
        }
    }
}

fun <SpSaver : AbsSpSaver, V> SpSaver.toLiveData(
    owner: LifecycleOwner, property: KProperty<V>, accessDelegateName: Boolean = false,
): LiveData<V> = internalInlineLiveDataPropertyChange(owner, property, accessDelegateName) { it }

fun <SpSaver : AbsSpSaver, V, R> SpSaver.toLiveData(
    owner: LifecycleOwner,
    property: KProperty<V>,
    accessDelegateName: Boolean = false,
    mapCast: (V) -> R,
): LiveData<R> = internalInlineLiveDataPropertyChange(owner, property, accessDelegateName, mapCast)
//</editor-fold>
