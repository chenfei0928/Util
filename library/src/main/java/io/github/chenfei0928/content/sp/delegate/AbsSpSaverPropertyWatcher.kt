package io.github.chenfei0928.content.sp.delegate

import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.github.chenfei0928.content.sp.registerOnSharedPreferenceChangeListener
import io.github.chenfei0928.util.ExecutorUtil
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.isAccessible

/**
 * 监听sp字段的变化并通知回调进行更新，可以方便的传入字段以保证类型安全和一个监听器只监听一个值的变化
 *
 * @param owner 生命周期宿主（用于同步生命周期变化以取消监听）
 * @param property 要监听的字段（用于错误日志反馈和获取该字段sp存储的key）
 * @param delegate 该字段的委托实现（用于获取该字段的sp存储key）
 * @param valueGetter 该字段值的获取方法（通过字段反射获取或其它）
 * @param listener 该字段变化的监听器（该sp存储的值发生变化时会触发回调，传入的值根据传入的[valueGetter]获取而非直接通过sp获取值）
 */
private inline fun <SpSaver : AbsSpSaver, V> SpSaver.watchSharedPreferenceDelegatePropertyChange(
    owner: LifecycleOwner,
    property: KProperty<*>,
    delegate: Any?,
    crossinline valueGetter: () -> V,
    noinline listener: (V) -> Unit
) {
    // 判断该字段的委托
    if (delegate !is AbsSpSaver.AbsSpDelegate<*>) {
        throw IllegalArgumentException("Property($property) must is delegate subclass as AbsSpSaver.AbsSpDelegate: $delegate")
    } else {
        // 如果该字段是sp委托，获取其sp存储的key
        val key = delegate.obtainDefaultKey(property)
        listener(valueGetter())
        // 监听sp变化并调用回调
        AbsSpSaver.getSp(this)
            .registerOnSharedPreferenceChangeListener(owner) {
                if (it == key) {
                    listener(valueGetter())
                }
            }
    }
}

//<editor-fold desc="KProperty0的委托监听，其使用实例的反射属性引用" defaultstate="collapsed">
/**
 * 监听sp字段的变化并通知回调进行更新，可以方便的传入字段以保证类型安全和一个监听器只监听一个值的变化
 */
fun <SpSaver : AbsSpSaver, V> SpSaver.watchSharedPreferenceDelegatePropertyChange(
    owner: LifecycleOwner, property: KProperty0<V>, listener: (V) -> Unit
) {
    // 获取该字段的委托
    property.isAccessible = true
    val delegate = property.getDelegate()
    watchSharedPreferenceDelegatePropertyChange(
        owner, property, delegate, { property.get() }, listener
    )
}

/**
 * 监听sp属性变化转换，并为liveData使用
 *
 * @param owner 生命周期宿主
 * @param property 需要监听的属性字段
 */
fun <SpSaver : AbsSpSaver, V, R> SpSaver.liveDataPropertyChange(
    owner: LifecycleOwner, property: KProperty0<V>, mapCast: (V) -> R
): LiveData<R> = MutableLiveData<R>().apply {
    // 监听sp属性变化
    watchSharedPreferenceDelegatePropertyChange(owner, property) {
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

fun <SpSaver : AbsSpSaver, V> SpSaver.liveDataPropertyChange(
    owner: LifecycleOwner, property: KProperty0<V>
): LiveData<V> = liveDataPropertyChange(owner, property) { it }
//</editor-fold>

//<editor-fold desc="KProperty1的委托监听，其使用类的反射属性引用" defaultstate="collapsed">
/**
 * 监听sp字段的变化并通知回调进行更新，可以方便的传入字段以保证类型安全和一个监听器只监听一个值的变化
 */
fun <SpSaver : AbsSpSaver, V> SpSaver.watchSharedPreferenceDelegatePropertyChange(
    owner: LifecycleOwner, property: KProperty1<SpSaver, V>, listener: (V) -> Unit
) {
    // 获取该字段的委托
    property.isAccessible = true
    val delegate = property.getDelegate(this)
    watchSharedPreferenceDelegatePropertyChange(
        owner, property, delegate, { property.get(this) }, listener
    )
}

/**
 * 监听sp属性变化转换，并为liveData使用
 *
 * @param owner 生命周期宿主
 * @param property 需要监听的属性字段
 */
fun <SpSaver : AbsSpSaver, V, R> SpSaver.liveDataPropertyChange(
    owner: LifecycleOwner, property: KProperty1<SpSaver, V>, mapCast: (V) -> R
): LiveData<R> = MutableLiveData<R>().apply {
    // 监听sp属性变化
    watchSharedPreferenceDelegatePropertyChange(owner, property) {
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

fun <SpSaver : AbsSpSaver, V> SpSaver.liveDataPropertyChange(
    owner: LifecycleOwner, property: KProperty1<SpSaver, V>
): LiveData<V> = liveDataPropertyChange(owner, property) { it }
//</editor-fold>
