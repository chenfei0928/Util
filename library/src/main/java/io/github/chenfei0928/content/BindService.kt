package io.github.chenfei0928.content

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * @author chenf()
 * @date 2025-02-08 15:36
 */

suspend inline fun <T> Context.bindService(
    intent: Intent,
    flag: Int,
    crossinline onServiceConnected: ContinuationOnServiceConnected<T>
): T = object : SuspendCancellableCoroutineBindService<T>(this, intent, flag) {
    override fun invoke(
        name: ComponentName, service: IBinder, continuation: CancellableContinuation<T>
    ) = onServiceConnected(name, service, continuation)
}.use {
    suspendCancellableCoroutine(it)
}

inline fun <T> Context.bindServiceLiveData(
    intent: Intent,
    flag: Int,
    crossinline onServiceConnectedTransform: OnServiceConnected<T>
): LiveData<T> = object : BaseServiceConnection.LiveData<T>(this, intent, flag, true) {
    override fun onServiceConnectedTransform(
        name: ComponentName, service: IBinder
    ): T = onServiceConnectedTransform(name, service)
}

inline fun <T> Context.bindServiceLiveData(
    lifecycleOwner: LifecycleOwner,
    intent: Intent,
    flag: Int,
    crossinline onServiceConnectedTransform: OnServiceConnected<T>
): LiveData<T> = object : BaseServiceConnection.LiveData<T>(this, intent, flag, false) {
    override fun onServiceConnectedTransform(
        name: ComponentName, service: IBinder
    ): T = onServiceConnectedTransform(name, service)
}.apply {
    lifecycleOwner.lifecycle.addObserver(this)
}

inline fun <T : Any> Context.bindServiceFlow(
    intent: Intent,
    flag: Int,
    crossinline onServiceConnected: OnServiceConnected<T>,
): Flow<T?> = callbackFlow(object : CallbackFlowBindService<T?>(this, intent, flag) {
    override fun onServiceConnectedTransform(
        name: ComponentName, service: IBinder
    ): T = onServiceConnected(name, service)
})

typealias ContinuationOnServiceConnected<T> = (
    name: ComponentName,
    service: IBinder,
    continuation: CancellableContinuation<T>,
) -> Unit

typealias OnServiceConnected<T> = (
    name: ComponentName,
    service: IBinder,
) -> T
