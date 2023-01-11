package io.github.chenfei0928.os

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.github.chenfei0928.lifecycle.LifecycleCacheDelegate
import io.github.chenfei0928.lifecycle.isAlive
import java.io.Closeable

/**
 * 与宿主生命周期绑定的Handler，会在处理消息前，判断当前生命周期是否可用。
 * 会在宿主生命周期[Lifecycle.Event.ON_DESTROY]时移除所有message。
 *
 * - [Activity]
 *     - [Activity.onCreate] 时，即可用
 *     - [Activity.onDestroy] 及其之后不可用
 * - [Fragment]
 *     - [Fragment.performCreate] 时，即可用（在[Fragment.performAttach]中不会触发生命周期变更）
 *     - [Fragment.performDestroy] 及其之后不可用
 * - [Fragment.getViewLifecycleOwner]
 *     - [Fragment.restoreViewState] 时，即可用（在[Fragment.performCreateView]、[Fragment.performViewCreated]中不会触发生命周期变更）
 *     - [Fragment.performDestroyView] 及其之后不可用
 *
 * 即允许在以下场景之外，消息才会被执行处理。
 * - [Activity]
 *     - [Activity.onDestroy]
 * - [Fragment]
 *     - [Fragment.onAttach]
 *     - [Fragment.onDestroy]
 *     - [Fragment.onDetach]
 * - [Fragment.getViewLifecycleOwner]
 *     - [Fragment.onAttach]
 *     - [Fragment.onCreateView]
 *     - [Fragment.onViewCreated]
 *     - [Fragment.onViewDestroy]
 *     - [Fragment.onDestroy]
 *     - [Fragment.onDetach]
 *
 * 不要在该类中重写[Handler.sendMessageAtTime]并判断生命周期忽略任务，这样会导致[Fragment.onAttach]和[Fragment.performCreate]中发送的事件不会被处理。
 *
 * Created by MrFeng on 2017/6/28.
 */
private class SafeHandler(
    owner: LifecycleOwner,
    private val closeCallback: Closeable
) : Handler(Looper.getMainLooper()), LifecycleEventObserver {
    private var mIsAlive = !owner.lifecycle.isAlive

    override fun dispatchMessage(msg: Message) {
        if (!mIsAlive) {
            return
        }
        super.dispatchMessage(msg)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            removeCallbacksAndMessages(null)
            closeCallback.close()
            mIsAlive = false
        } else if (event == Lifecycle.Event.ON_CREATE) {
            mIsAlive = true
        }
    }
}

/**
 * 与宿主生命周期绑定的Handler，会在处理消息前，判断当前生命周期是否可用，
 * 会在宿主生命周期[Lifecycle.Event.ON_DESTROY]时移除所有message。
 * 即允许在以下场景之外，消息才会被执行处理。
 * - [Activity]
 *     - [Activity.onDestroy]
 * - [Fragment]
 *     - [Fragment.onAttach]
 *     - [Fragment.onDestroy]
 *     - [Fragment.onDetach]
 * - [Fragment.getViewLifecycleOwner]
 *     - [Fragment.onAttach]
 *     - [Fragment.onCreateView]
 *     - [Fragment.onViewCreated]
 *     - [Fragment.onViewDestroy]
 *     - [Fragment.onDestroy]
 *     - [Fragment.onDetach]
 */
val LifecycleOwner.safeHandler: Handler by LifecycleCacheDelegate<LifecycleOwner, SafeHandler> { owner, closeCallback ->
    SafeHandler(owner, closeCallback)
}
