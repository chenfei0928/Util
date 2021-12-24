package io.github.chenfei0928.adapter

import androidx.databinding.ViewDataBinding

@Deprecated(message = "Do not use DataBinding, use MultiType-Binder.")
open class DataBindingViewHolder<Binding : ViewDataBinding>(
        @JvmField
        val binding: Binding
) : ViewHolder<Any>(binding.root) {
    @Deprecated("use dataBinding", level = DeprecationLevel.HIDDEN)
    override var item: Any? = null
}
