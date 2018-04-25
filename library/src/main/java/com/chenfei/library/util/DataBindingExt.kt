package com.chenfei.library.util

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.ViewGroup

class DataBindingExt {
    companion object {
        /**
         * 用于加载ViewDataBinding而不添加到ViewGroup时使用，常见于Adapter
         */
        @JvmStatic
        fun <Binding : ViewDataBinding> inflate(parent: ViewGroup, @LayoutRes layoutId: Int): Binding {
            return DataBindingUtil.inflate(LayoutInflater.from(parent.context), layoutId, parent, false)
        }
    }
}
