package io.github.chenfei0928.content.sp

import android.content.SharedPreferences
import androidx.annotation.ReturnThis
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import io.github.chenfei0928.concurrent.ExecutorUtil
import io.github.chenfei0928.concurrent.UiTaskExecutor.Companion.runOnUiThread
import io.github.chenfei0928.content.sp.saver.AbsSpSaver

/**
 * 宿主生命周期联动取消监听的sp变更监听器
 *
 * @param owner 绑定到的生命周期宿主
 * @param filterKey sp变化时的过滤器，如果不为null，则在回调中会验证变化的key是否与传入的一致
 * （sp被清空[SharedPreferences.Editor.clear]时总会回调[onChangedOrClear]）
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-08-04 16:43
 */
interface LifecycleBindOnSharedPreferenceChangeListener
    : SharedPreferences.OnSharedPreferenceChangeListener {
    val sharedPreferences: SharedPreferences
    val filterKey: String?

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // sp被清空时回调中的key为null，在此处需要验证是否是sp被清空的情况，如果是被清空时，总是回调
        if (filterKey != null && key != null && filterKey != key)
            return
        onChangedOrClear(this.sharedPreferences, key)
    }

    fun onChangedOrClear(sharedPreferences: SharedPreferences, key: String?)

    abstract class Base(
        override val sharedPreferences: SharedPreferences,
        override val filterKey: String?,
    ) : LifecycleBindOnSharedPreferenceChangeListener, LifecycleEventObserver {

        final override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_DESTROY) {
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
                return
            }
        }

        @ReturnThis
        fun bind(owner: LifecycleOwner): LifecycleBindOnSharedPreferenceChangeListener {
            owner.lifecycle.addObserver(this)
            sharedPreferences.registerOnSharedPreferenceChangeListener(this)
            return this
        }
    }

    /**
     * @author chenf()
     * @date 2024-12-25 18:23
     */
    abstract class SpValueLiveData<R>(
        override val sharedPreferences: SharedPreferences,
        override val filterKey: String?,
    ) : LiveData<R>(), LifecycleBindOnSharedPreferenceChangeListener {

        final override fun onChangedOrClear(sharedPreferences: SharedPreferences, key: String?) {
            ExecutorUtil.runOnUiThread {
                value = valueGetter()
            }
        }

        protected abstract fun valueGetter(): R

        final override fun onActive() {
            super.onActive()
            value = valueGetter()
            sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        final override fun onInactive() {
            super.onInactive()
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        }

        companion object {
            inline operator fun <SpSaver : AbsSpSaver<SpSaver>, R> invoke(
                spSaver: SpSaver, filterKey: String, crossinline getter: () -> R
            ): LiveData<R> = object : SpValueLiveData<R>(AbsSpSaver.getSp(spSaver), filterKey) {
                override fun valueGetter(): R = getter()
            }
        }
    }
}
