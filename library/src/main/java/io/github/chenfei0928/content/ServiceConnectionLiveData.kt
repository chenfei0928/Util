package io.github.chenfei0928.content

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import io.github.chenfei0928.concurrent.coroutines.CoroutineAndroidContextImpl.Companion.getStaticTag

/**
 * @author chenf()
 * @date 2025-02-08 14:13
 */
internal abstract class ServiceConnectionLiveData<T>(
    private val context: Context,
    private val intent: Intent,
    private val flag: Int,
    private val lazyBind: Boolean,
) : LiveData<T>(), ServiceConnection, LifecycleEventObserver {
    override fun onActive() {
        super.onActive()
        if (lazyBind) {
            isBound = true
        }
    }

    override fun onInactive() {
        super.onInactive()
        if (lazyBind) {
            isBound = false
        }
    }

    private var isBound: Boolean = false
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (value) {
                context.bindService(intent, this, flag)
            } else try {
                context.unbindService(this)
            } catch (e: IllegalArgumentException) {
                val tag = context.getStaticTag()
                Log.e(tag, "bindService: ", e)
            }
        }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            isBound = false
        }
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        value = onServiceConnectedTransform(name, service)
    }

    override fun onServiceDisconnected(name: ComponentName) {
        value = null
    }

    override fun onBindingDied(name: ComponentName) {
        super.onBindingDied(name)
        isBound = false
        isBound = true
    }

    override fun onNullBinding(name: ComponentName) {
        super.onNullBinding(name)
        isBound = false
    }

    protected abstract fun onServiceConnectedTransform(name: ComponentName, service: IBinder): T
}
