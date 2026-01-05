package io.github.chenfei0928.content

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import io.github.chenfei0928.concurrent.coroutines.CoroutineAndroidContext
import io.github.chenfei0928.util.Log
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking

/**
 * @author chenf()
 * @date 2025-02-08 15:16
 */
abstract class CallbackFlowBindService<T>(
    context: Context,
    intent: Intent,
    flag: Int,
) : BaseServiceConnection.Base(context, intent, flag), suspend (ProducerScope<T?>) -> Unit {
    private lateinit var producerScope: ProducerScope<T?>
    override val tag: String
        get() = producerScope.coroutineContext[CoroutineAndroidContext]?.tag ?: super.tag

    override suspend fun invoke(producerScope: ProducerScope<T?>) {
        this.producerScope = producerScope
        isBound = true
        producerScope.invokeOnClose {
            isBound = false
        }
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        producerScope.trySendBlocking(onServiceConnectedTransform(name, service)).onFailure {
            Log.w(tag, "onServiceConnected: ", it)
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        // service链接断开，传递null
        producerScope.trySendBlocking(null).onFailure {
            Log.w(tag, "onServiceDisconnected: ", it)
        }
    }

    protected abstract fun onServiceConnectedTransform(
        name: ComponentName, service: IBinder
    ): T & Any
}
