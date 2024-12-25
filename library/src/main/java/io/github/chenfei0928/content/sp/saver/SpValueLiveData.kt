package io.github.chenfei0928.content.sp.saver

import android.content.SharedPreferences
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import io.github.chenfei0928.concurrent.ExecutorUtil
import io.github.chenfei0928.concurrent.UiTaskExecutor.Companion.runOnUiThread
import io.github.chenfei0928.lifecycle.isAlive

/**
 * @author chenf()
 * @date 2024-12-25 18:23
 */
abstract class SpValueLiveData<R>(
    private val owner: LifecycleOwner,
    private val sharedPreferences: SharedPreferences,
    private val key: String,
) : LiveData<R>(), SharedPreferences.OnSharedPreferenceChangeListener, LifecycleEventObserver {

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences?, key: String?
    ) {
        if (key != this.key) return
        // liveData只允许主线程更新值，将值的更新发送到主线程执行
        ExecutorUtil.runOnUiThread {
            value = valueGetter()
        }
    }

    protected abstract fun valueGetter(): R

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (!owner.lifecycle.isAlive) {
            owner.lifecycle.removeObserver(this)
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
            return
        }
    }
}
