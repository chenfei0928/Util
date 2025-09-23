package io.github.chenfei0928.app.fragment

import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle

/**
 * @author chenf()
 * @date 2025-05-22 11:32
 */
data class FragmentInfo(
    val fragment: Fragment,
    val container: ViewGroup?,
    @all:IdRes val containerViewId: Int,
    val tag: String? = null
)

fun FragmentTransaction.addAndShow(info: FragmentInfo) {
    if (!info.fragment.isAdded) {
        if (info.container != null) {
            add(info.containerViewId, info.fragment, info.tag)
        } else {
            add(info.container as ViewGroup, info.fragment, info.tag)
        }
    } else if (info.fragment.isHidden) {
        show(info.fragment)
        setMaxLifecycle(info.fragment, Lifecycle.State.RESUMED)
    }
}

fun FragmentTransaction.hideIfAdded(
    f: Fragment, maxState: Lifecycle.State = Lifecycle.State.CREATED
) {
    if (f.isAdded && !f.isHidden) {
        hide(f)
        setMaxLifecycle(f, maxState)
    }
}

fun FragmentTransaction.addAndShow(@IdRes containerViewId: Int, f: Fragment, tag: String) {
    if (!f.isAdded) {
        add(containerViewId, f, tag)
    } else if (f.isHidden) {
        show(f)
        setMaxLifecycle(f, Lifecycle.State.RESUMED)
    }
}

fun FragmentTransaction.addAndShow(container: ViewGroup, f: Fragment, tag: String) {
    if (!f.isAdded) {
        add(container, f, tag)
    } else if (f.isHidden) {
        show(f)
        setMaxLifecycle(f, Lifecycle.State.RESUMED)
    }
}
