package com.chenfei.util.kotlin

import android.animation.Animator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

fun Animator.bindToLifecycle(owner: LifecycleOwner): Animator {
    bindUntilEvent(owner, Lifecycle.Event.ON_DESTROY)
    return this
}

private fun Animator.bindUntilEvent(owner: LifecycleOwner, event: Lifecycle.Event) {
    owner.lifecycle.onEvent { e ->
        if (e == event) {
            this.cancel()
        }
    }
}

/**
 * [android.animation.LayoutTransition.DEFAULT_DURATION]
 */
const val ANIMATE_LAYOUT_CHANGES_DEFAULT_DURATION: Long = 300
