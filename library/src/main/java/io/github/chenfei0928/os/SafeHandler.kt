package io.github.chenfei0928.os

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.github.chenfei0928.lifecycle.LifecycleCacheDelegate
import io.github.chenfei0928.lifecycle.isAlive

/**
 * 与宿主生命周期绑定的Handler，会在处理消息前，判断当前生命周期是否可用。
 *
 * 对应[Fragment]的[Fragment.onCreate]（[Fragment.performCreate]，在[Fragment.onAttach]中
 * 发送的任务不一定会被处理（Fragment在[Fragment.performAttach]中不会触发生命周期变更，
 * 需要到[Fragment.performCreate]之后））,
 * [Fragment.getViewLifecycleOwner]的[Fragment.onViewStateRestored]（[Fragment.restoreViewState]）。
 *
 * 会在宿主生命周期[Lifecycle.Event.ON_DESTROY]时移除所有message。
 *
 * 对应[Fragment]的[Fragment.onDestroy]（[Fragment.performDestroy]）,
 * 和[Activity]的[Activity.onDestroy]（[FragmentActivity.onDestroy]），
 * [Fragment.getViewLifecycleOwner]的[Fragment.onViewStateRestored]（[Fragment.restoreViewState]）。
 *
 * 即在[Activity.onDestroy]的 `super.onDestroy()` 之后、[Fragment.onDestroy]、[Fragment.onDetach]中发送的任务将不会被处理。
 * 在[Fragment.onAttach]中发送的任务也不一定会被处理（Fragment在[Fragment.performAttach]中不会触发生命周期变更，需要到[Fragment.performCreate]之后）。
 *
 * 即允许在以下场景之外，才可以正常工作。
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
 *     - [Fragment.onDestroy]
 *     - [Fragment.onDetach]
 *
 * 不要在该类中重写[Handler.sendMessageAtTime]并判断生命周期忽略任务，这样会导致[Fragment.onAttach]和[Fragment.performCreate]中发送的事件不会被处理。
 *
 * todo 尝试使用[FragmentManager.FragmentLifecycleCallbacks]来实现对Fragment/FragmentView生命周期的感知或许会更灵活？
 *
 * Created by MrFeng on 2017/6/28.
 */
private class SafeHandler(
    owner: LifecycleOwner,
    private val closeCallback: () -> Unit
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
            closeCallback()
            mIsAlive = false
        } else if (event == Lifecycle.Event.ON_CREATE) {
            mIsAlive = true
        }
    }
}

val LifecycleOwner.safeHandler: Handler by LifecycleCacheDelegate<LifecycleOwner, SafeHandler> { owner, closeCallback ->
    SafeHandler(owner, closeCallback)
}
