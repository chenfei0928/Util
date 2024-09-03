package io.github.chenfei0928.widget.recyclerview.binder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import io.github.chenfei0928.widget.recyclerview.adapter.ViewBindingHolder

abstract class BaseBindingBinder<T, V : ViewBinding>(
    private val viewBindingInflater: (LayoutInflater, ViewGroup, Boolean) -> V
) : BaseViewHolderBinder<T, ViewBindingHolder<T, V>>() {

    final override fun onCreateViewHolderImpl(
        inflater: LayoutInflater, parent: ViewGroup
    ): ViewBindingHolder<T, V> {
        return ViewBindingHolder(viewBindingInflater(inflater, parent, false))
    }
}
