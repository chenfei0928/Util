package io.github.chenfei0928.app.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.github.chenfei0928.concurrent.coroutines.showWithAwaitDismiss
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

fun Fragment.isVisibleForUser(): Boolean {
    return activity?.lifecycle?.currentState == Lifecycle.State.RESUMED
            && isResumed && isVisible
            && parentFragment.let {
        it == null || it.isVisibleForUser()
    }
}

fun Fragment.visibleStatus(): String = this::class.java.simpleName +
        " isAdded:$isAdded" +
        " isHidden:$isHidden" +
        " isResumed:$isResumed" +
        " isVisible:$isVisible" +
        " userVisibleHint:$userVisibleHint" +
        " view:$view" +
        (parentFragment?.let {
            "\nparent: " + it.visibleStatus()
        } ?: "")

inline fun <F : Fragment> F.applyArgumentBundle(block: Bundle.() -> Unit): F =
    apply { arguments = (arguments ?: Bundle()).apply(block) }

fun Fragment.removeSelf() {
    if (!isAdded) {
        return
    }
    fragmentManager
        ?.beginTransaction()
        ?.remove(this)
        ?.commitAllowingStateLoss()
}

inline fun <reified T> Fragment.findParentByType(block: T.() -> Unit) {
    findParentByType(T::class.java)
        ?.run(block)
}

inline fun <reified T> Fragment.findParentByType(): T? {
    return findParentByType(T::class.java)
}

fun <T> Fragment.findParentByType(clazz: Class<T>): T? {
    // 检查父Fragment是否能处理该回调
    var parentFragment = parentFragment
    while (parentFragment != null) {
        if (clazz.isInstance(parentFragment)) {
            // 处理回调后，不再向上查找
            return clazz.cast(parentFragment)
        } else {
            parentFragment = parentFragment.parentFragment
        }
    }
    // 检查宿主和Activity是否能处理该回调
    val host = host
    if (clazz.isInstance(host)) {
        return clazz.cast(host)
    }
    val activity = activity
    if (clazz.isInstance(activity)) {
        return clazz.cast(activity)
    }
    return null
}

/**
 * 等待指定fragment销毁，通常可以用于[DialogFragment]的showAndAwait功能
 */
suspend fun LifecycleOwner.awaitDestroy() {
    suspendCancellableCoroutine { continuation ->
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY && continuation.isActive) {
                continuation.resume(Unit)
            }
        })
    }
}
