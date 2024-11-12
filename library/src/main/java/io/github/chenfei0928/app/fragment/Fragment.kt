package io.github.chenfei0928.app.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle

fun Fragment.isVisibleForUser(): Boolean {
    return activity?.lifecycle?.currentState == Lifecycle.State.RESUMED
            && isResumed && isVisible
            && parentFragment.let {
        it == null || it.isVisibleForUser()
    }
}

fun Fragment.toVisibleStatusString(): String = buildString {
    var f: Fragment? = this@toVisibleStatusString
    while (f != null) {
        append(f.javaClass.simpleName)
        append(" isAdded:")
        append(f.isAdded)
        append(" isHidden:")
        append(f.isHidden)
        append(" isResumed:")
        append(f.isResumed)
        append(" isVisible:")
        append(f.isVisible)
        append(" view:")
        append(f.view)

        f = f.parentFragment
        if (f != null) {
            append("\nparent: ")
        }
    }
}

inline fun <F : Fragment> F.applyArgumentBundle(
    block: Bundle.() -> Unit
): F = apply {
    arguments = (arguments ?: Bundle()).apply(block)
}

fun Fragment.removeSelf() {
    if (!isAdded) {
        return
    }
    parentFragmentManager.commit(true) {
        remove(this@removeSelf)
    }
}

inline fun <reified T> Fragment.findParentByType(block: T.() -> Unit) {
    findParentByType(T::class.java)?.run(block)
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
