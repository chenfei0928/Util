package io.github.chenfei0928.util.kotlin

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.databinding.OnRebindCallback
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

class DataBindingExt {
    companion object {
        @JvmStatic
        fun <T : ViewDataBinding> setContentView(activity: Activity, @LayoutRes layoutId: Int): T {
            val t: T = DataBindingUtil.setContentView(activity, layoutId)
            if (activity is LifecycleOwner) {
                t.lifecycleOwner = activity
            }
            return t
        }

        @JvmStatic
        fun <T : ViewDataBinding> bind(view: View, lifecycleOwner: LifecycleOwner?): T {
            val bind: T = DataBindingUtil.bind(view)!!
            bind.lifecycleOwner = lifecycleOwner
            return bind
        }

        /**
         * 用于加载ViewDataBinding而不添加到ViewGroup时使用，常见于Adapter
         */
        @JvmStatic
        @JvmOverloads
        fun <Binding : ViewDataBinding> inflate(parent: ViewGroup,
                                                @LayoutRes layoutId: Int,
                                                attachToParent: Boolean = false,
                                                inflater: LayoutInflater = LayoutInflater.from(parent.context)
        ): Binding {
            return DataBindingUtil.inflate(inflater, layoutId, parent, attachToParent)
        }
    }
}

inline fun <T : ViewDataBinding> T.doOnBound(crossinline block: (T) -> Unit) {
    addOnRebindCallback(object : OnRebindCallback<T>() {
        override fun onBound(binding: T) {
            super.onBound(binding)
            block(binding)
        }
    })
}

inline fun <T : Observable> T.doOnPropertyChanged(crossinline block: T.(Int) -> Unit) {
    addOnPropertyChangedCallback(createOnPropertyChanged(block))
}

inline fun <T : Observable> T.createOnPropertyChanged(crossinline block: T.(Int) -> Unit) =
        object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                block(this@createOnPropertyChanged, propertyId)
            }
        }

inline fun <T : Observable> T.observe(owner: LifecycleOwner, crossinline block: T.(Int) -> Unit) {
    if (owner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
        return
    }
    val callback = object : LifecycleOnPropertyChangedCallback(this) {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            block(this@observe, propertyId)
        }
    }
    owner.lifecycle.addObserver(callback)
    this.addOnPropertyChangedCallback(callback)
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
