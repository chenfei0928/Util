package io.github.chenfei0928.content

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.RemoteException
import io.github.chenfei0928.concurrent.coroutines.CoroutineAndroidContext
import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resumeWithException

/**
 * @author chenf()
 * @date 2025-02-08 15:07
 */
abstract class SuspendCancellableCoroutineBindService<T>(
    context: Context,
    intent: Intent,
    flag: Int,
) : BaseServiceConnection.Base(context, intent, flag), (CancellableContinuation<T>) -> Unit,
    ContinuationOnServiceConnected<T> {
    private lateinit var continuation: CancellableContinuation<T>
    override val tag: String
        get() = continuation.context[CoroutineAndroidContext]?.tag ?: super.tag

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        try {
            invoke(name, service, continuation)
        } catch (e: RemoteException) {
            // ipc执行失败，返回异常
            if (!continuation.isActive) {
                return
            }
            continuation.resumeWithException(e)
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        // service链接断开，返回异常
        if (!continuation.isActive) {
            return
        }
        continuation.resumeWithException(RemoteException("onServiceDisconnected"))
    }

    override fun invoke(continuation: CancellableContinuation<T>) {
        this.continuation = continuation
        isBound = true
        continuation.invokeOnCancellation {
            // 当请求被取消，直接断开服务
            isBound = false
        }
    }
}
