package com.chenfei.library.adapter

import android.databinding.ViewDataBinding
import android.support.annotation.IdRes
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.View

open class ViewHolder<Binding : ViewDataBinding> : RecyclerView.ViewHolder {
    private val cachedViews: SparseArray<View> by lazy { SparseArray<View>() }
    @JvmField
    val binding: Binding?

    constructor(itemView: View) : super(itemView) {
        this.binding = null
    }

    constructor(dataBinding: Binding) : super(dataBinding.root) {
        this.binding = dataBinding
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : View> findViewById(@IdRes id: Int): T? {
        val cachedView = cachedViews[id]
        return if (cachedView != null) {
            cachedView as T
        } else {
            val v = itemView.findViewById<View>(id)
            if (v != null) {
                cachedViews.put(id, v)
            }
            v as T
        }
    }
}
