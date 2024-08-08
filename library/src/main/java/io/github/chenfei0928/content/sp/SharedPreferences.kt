package io.github.chenfei0928.content.sp

import android.content.SharedPreferences
import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.github.chenfei0928.lifecycle.isAlive

/**
 * 注册一个监听器以监听sp值变更，其可以监听生命周期变化以自动取消
 */
inline fun SharedPreferences.registerOnSharedPreferenceChangeListener(
    owner: LifecycleOwner,
    @MainThread
    crossinline callback: SharedPreferences.(key: String?) -> Unit
) {
    val changeListener = object : LifecycleOwnerOnSharedPreferenceChangeListener(owner, this) {
        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences?, key: String?
        ) {
            callback(this@registerOnSharedPreferenceChangeListener, key)
        }
    }
    owner.lifecycle.addObserver(changeListener)
    registerOnSharedPreferenceChangeListener(changeListener)
}

/**
 * 宿主生命周期联动取消监听的sp变更监听器
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-08-04 16:43
 */
abstract class LifecycleOwnerOnSharedPreferenceChangeListener(
    private val owner: LifecycleOwner,
    private val sharedPreferences: SharedPreferences,
) : SharedPreferences.OnSharedPreferenceChangeListener, LifecycleEventObserver {

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (!owner.lifecycle.isAlive) {
            owner.lifecycle.removeObserver(this)
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
            return
        }
    }
}
