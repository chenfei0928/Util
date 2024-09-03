package io.github.chenfei0928.widget.recyclerview.adapter

import androidx.viewbinding.ViewBinding

open class ViewBindingHolder<T, V : ViewBinding>(
    val viewBinding: V
) : ViewHolder<T>(viewBinding.root)
