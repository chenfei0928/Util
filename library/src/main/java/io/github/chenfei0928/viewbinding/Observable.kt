/**
 * @author chenf()
 * @date 2024-08-01 14:33
 */
package io.github.chenfei0928.viewbinding

import androidx.databinding.Observable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

inline fun <T : Observable> T.observeForever(
    crossinline block: Observable.OnPropertyChangedCallback.(propertyId: Int) -> Unit
): Observable.OnPropertyChangedCallback = object : Observable.OnPropertyChangedCallback() {
    override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
        block(propertyId)
    }
}.apply { addOnPropertyChangedCallback(this) }

inline fun <T : Observable> T.observe(
    owner: LifecycleOwner,
    crossinline block: Observable.OnPropertyChangedCallback.(propertyId: Int) -> Unit
): Observable.OnPropertyChangedCallback {
    val callback = object : LifecycleOnPropertyChangedCallback(this) {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            block(propertyId)
        }
    }
    if (owner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
        return callback
    }
    owner.lifecycle.addObserver(callback)
    this.addOnPropertyChangedCallback(callback)
    return callback
}

abstract class LifecycleOnPropertyChangedCallback(
    private val observable: Observable
) : Observable.OnPropertyChangedCallback(), LifecycleEventObserver {

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            observable.removeOnPropertyChangedCallback(this)
        }
    }
}
