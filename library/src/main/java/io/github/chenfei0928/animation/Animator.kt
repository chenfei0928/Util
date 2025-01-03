package io.github.chenfei0928.animation

import android.animation.Animator
import androidx.annotation.ReturnThis
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import io.github.chenfei0928.lifecycle.bindUntilFirstEvent

@ReturnThis
fun Animator.bindToLifecycle(owner: LifecycleOwner): Animator {
    bindUntilEvent(owner, Lifecycle.Event.ON_DESTROY)
    return this
}

private fun Animator.bindUntilEvent(owner: LifecycleOwner, event: Lifecycle.Event) {
    owner.bindUntilFirstEvent(event) {
        this.cancel()
    }
}

/**
 * [android.animation.LayoutTransition.DEFAULT_DURATION]
 */
const val ANIMATE_LAYOUT_CHANGES_DEFAULT_DURATION: Long = 300
