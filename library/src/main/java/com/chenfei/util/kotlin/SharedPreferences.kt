package com.chenfei.util.kotlin

import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.chenfei.util.Log

private const val TAG = "KW_SharedPreferences"

/**
 * 注册一个监听器以监听sp值变更，其可以监听生命周期变化以自动取消
 */
fun SharedPreferences.registerOnSharedPreferenceChangeListener(
    owner: LifecycleOwner, callback: SharedPreferences.(key: String) -> Unit
) {
    if (owner is Fragment) {
        Log.w(
            TAG,
            "registerOnSharedPreferenceChangeListener: owner($owner) is a Fragment, use viewLifecycleOwner as LifecycleOwner in Fragment."
        )
    }
    val changeListener = SharedPreferencesOnSharedPreferenceChangeListener(owner, this, callback)
    owner.lifecycle.addObserver(changeListener)
    registerOnSharedPreferenceChangeListener(changeListener)
}

/**
 * 宿主生命周期联动取消监听的sp变更监听器
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-08-04 16:43
 */
private class SharedPreferencesOnSharedPreferenceChangeListener(
    private val owner: LifecycleOwner,
    private val sharedPreferences: SharedPreferences,
    private val callback: SharedPreferences.(key: String) -> Unit
) : SharedPreferences.OnSharedPreferenceChangeListener, LifecycleEventObserver {

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        callback(this.sharedPreferences, key)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (!owner.lifecycle.isAlive) {
            owner.lifecycle.removeObserver(this)
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
            return
        }
    }
}
