package io.github.chenfei0928.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

/**
 * @author chenf()
 * @date 2025-02-12 14:51
 */
interface ILiveListener<Observer> {

    fun observe(
        owner: LifecycleOwner,
        state: Lifecycle.State = Lifecycle.State.INITIALIZED,
        observer: Observer
    )

    fun observeForever(element: Observer)

    fun removeObserver(element: Observer)

    fun removeObservers(owner: LifecycleOwner)
}
