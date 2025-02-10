package io.github.chenfei0928.content

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.github.chenfei0928.concurrent.coroutines.CoroutineAndroidContextImpl.Companion.getStaticTag
import io.github.chenfei0928.util.Log
import java.lang.AutoCloseable

/**
 * 提供在目标死亡时自动重启目标、在目标拒绝服务时自动解除的支持
 *
 * @author chenf()
 * @date 2025-02-08 16:36
 */
interface BaseServiceConnection : ServiceConnection, AutoCloseable {
    val tag: String
    var isBound: Boolean

    override fun onBindingDied(name: ComponentName) {
        isBound = false
        isBound = true
    }

    override fun onNullBinding(name: ComponentName) {
        isBound = false
    }

    override fun close() {
        isBound = false
    }

    abstract class Base(
        private val context: Context,
        private val intent: Intent,
        private val flag: Int,
    ) : BaseServiceConnection {
        override val tag: String
            get() = context.javaClass.simpleName
        override var isBound: Boolean = false
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
                    Log.e(tag, "bindService: ", e)
                }
            }
    }

    //<editor-fold desc="LiveData 的服务链接订阅" defaultstatus="collapsed">
    /**
     * LiveData 的服务链接订阅
     *
     * 对于反注册，提供三种反注册方式
     * - [androidx.lifecycle.LiveData] 的 [LiveData.onActive]、[LiveData.onInactive] 来自动根据下游是否有监听者来注册和反注册
     * - [LifecycleEventObserver] 通过向 [Lifecycle.addObserver] 注册自身，来在其 destroy 时自动反注册
     * - 外部销毁时手动调用 [isBound] 设置为 false 来反注册
     *
     * @param lazyBind true则为绑定到 LiveData 自动注册与反注册，如果传入 false 则需要手动注册到[Lifecycle]或手动反注册
     *
     * @author chenf()
     * @date 2025-02-08 14:13
     */
    abstract class LiveData<T>(
        private val context: Context,
        private val intent: Intent,
        private val flag: Int,
        private val lazyBind: Boolean,
    ) : androidx.lifecycle.LiveData<T>(), BaseServiceConnection, LifecycleEventObserver {
        override val tag: String
            get() = context.getStaticTag() ?: context.javaClass.simpleName

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

        override var isBound: Boolean = false
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

        protected abstract fun onServiceConnectedTransform(name: ComponentName, service: IBinder): T
    }
    //</editor-fold>
}
