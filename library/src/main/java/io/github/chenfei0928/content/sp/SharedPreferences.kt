package io.github.chenfei0928.content.sp

import android.content.SharedPreferences
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

/**
 * 注册一个监听器以监听sp值变更，其可以监听生命周期变化以自动取消
 *
 * @param owner 绑定到的生命周期宿主
 * @param filterKey sp变化时的过滤器，如果不为null，则在回调中会验证变化的key是否与传入的一致
 * （sp被清空[SharedPreferences.Editor.clear]时总会回调[callback]）
 */
inline fun SharedPreferences.registerOnSharedPreferenceChangeListener(
    owner: LifecycleOwner,
    filterKey: String? = null,
    @MainThread
    crossinline callback: SharedPreferences.(key: String?) -> Unit
): SharedPreferences.OnSharedPreferenceChangeListener =
    object : LifecycleBindOnSharedPreferenceChangeListener.Base(this, filterKey) {
        override fun onChangedOrClear(sharedPreferences: SharedPreferences, key: String?) =
            callback(sharedPreferences, key)
    }.bind(owner)

/**
 * 监听sp属性变化转换，并为liveData使用
 */
inline fun <T> SharedPreferences.toLiveData(
    filterKey: String? = null, crossinline valueGetter: (SharedPreferences) -> T
): LiveData<T> = object : LifecycleBindOnSharedPreferenceChangeListener.SpValueLiveData<T>(
    this
) {
    override val filterKey: String? = filterKey
    override fun valueGetter(): T = valueGetter(this@toLiveData)
}
