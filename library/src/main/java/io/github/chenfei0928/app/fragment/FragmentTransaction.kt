package io.github.chenfei0928.app.fragment

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle

/**
 * @author chenf()
 * @date 2025-05-22 11:32
 */

fun FragmentTransaction.hideIfAdded(f: Fragment) {
    if (f.isAdded && !f.isHidden) {
        hide(f)
        setMaxLifecycle(f, Lifecycle.State.STARTED)
    }
}

fun FragmentTransaction.addAndShow(@IdRes containerViewId: Int, f: Fragment, tag: String? = null) {
    if (!f.isAdded) {
        add(containerViewId, f, tag)
    } else if (f.isHidden) {
        show(f)
        setMaxLifecycle(f, Lifecycle.State.RESUMED)
    }
}
