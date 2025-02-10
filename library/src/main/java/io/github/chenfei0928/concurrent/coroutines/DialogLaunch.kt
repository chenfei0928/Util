package io.github.chenfei0928.concurrent.coroutines

import android.app.Dialog
import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.github.chenfei0928.lifecycle.EventLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume

/**
 * 对已实现了[LifecycleOwner]的[Dialog]执行，
 * 启动协程块[block]并在其执行完毕后dismiss dialog
 *
 * @param D Dialog类型
 * @param parentLifecycleOwner 可附加的宿主生命周期
 * @param block 要执行的协程代码块
 * @receiver 将会在该dialog上启动
 */
@MainThread
inline fun <D> D.launchWithShow(
    parentLifecycleOwner: LifecycleOwner = EventLifecycleOwner.immortal,
    crossinline block: suspend CoroutineScope.(D) -> Unit,
) where D : Dialog, D : LifecycleOwner {
    val callback = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            cancel()
        }
    }
    parentLifecycleOwner.lifecycle.addObserver(callback)
    // 此处不需要附加context，removeObserver需要在主线程上执行
    val job = coroutineScope.launch {
        try {
            block(this@launchWithShow)
        } finally {
            parentLifecycleOwner.lifecycle.removeObserver(callback)
            dismiss()
        }
    }
    setOnCancelListener {
        if (job.isActive) {
            job.cancel()
        }
    }
    show()
}

/**
 * 在suspend函数中启动dialog并在协程环境中运行[block]代码块，并在执行完毕后dismiss dialog后返回执行结果
 *
 * @param D Dialog类型
 * @param R 返回值类型
 * @param context 执行[block]时要附加的协程上下文
 * @param block 要执行的协程代码块
 * @receiver 将会在该dialog上启动
 * @return
 */
suspend inline fun <D : Dialog, R> D.showWithContext(
    context: CoroutineContext = EmptyCoroutineContext,
    crossinline block: suspend CoroutineScope.(D) -> R,
): R = coroutineScope {
    show()
    val cancelSelfJob = onCancellation {
        if (isShowing) {
            this@showWithContext.cancel()
        }
    }
    setOnCancelListener {
        if (isActive) {
            this.cancel()
        }
    }
    return@coroutineScope try {
        if (context == EmptyCoroutineContext) {
            block(this@showWithContext)
        } else @Suppress("kotlin:S6311") withContext(context) {
            block(this@showWithContext)
        }
    } finally {
        cancelSelfJob.cancel()
        dismiss()
    }
}

/**
 * 显示该dialog并等待，以在其dismiss后返回
 */
suspend fun Dialog.showWithAwaitDismiss() = suspendCancellableCoroutine { continuation ->
    setOnDismissListener {
        continuation.resume(Unit)
    }
    show()
    continuation.invokeOnCancellation {
        cancel()
    }
}
