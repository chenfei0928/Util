package io.github.chenfei0928.widget.recyclerview.binder

import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.drakeet.multitype.ItemViewBinder
import io.github.chenfei0928.concurrent.ExecutorUtil
import io.github.chenfei0928.widget.recyclerview.adapter.ViewBindingHolder
import io.github.chenfei0928.widget.recyclerview.adapter.ViewHolder

/**
 * User: ChenFei(chenfei0928@gmail.com)
 * Date: 2019-03-21
 * Time: 17:05
 */
abstract class BaseViewHolderBinder<T, VH : ViewHolder<T>> : ItemViewBinder<T, VH>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): VH {
        val holder = onCreateViewHolderImpl(inflater, parent)
        // 检查是否是执行在ui线程
        if (Looper.myLooper() == Looper.getMainLooper()) {
            onViewHolderCreated(holder, parent)
        } else {
            ExecutorUtil.postToUiThread {
                onViewHolderCreated(holder, parent)
            }
        }
        return holder
    }

    protected abstract fun onCreateViewHolderImpl(inflater: LayoutInflater, parent: ViewGroup): VH

    open fun onViewHolderCreated(holder: VH, parent: ViewGroup) {}

    override fun onBindViewHolder(holder: VH, item: T, payloads: List<Any>) {
        holder.item = item
        super.onBindViewHolder(holder, item, payloads)
    }
}

abstract class BaseBindingBinder<T, V : ViewBinding>(
    private val viewBindingInflater: (LayoutInflater, ViewGroup, Boolean) -> V
) : BaseViewHolderBinder<T, ViewBindingHolder<T, V>>() {

    final override fun onCreateViewHolderImpl(
        inflater: LayoutInflater, parent: ViewGroup
    ): ViewBindingHolder<T, V> {
        return ViewBindingHolder(viewBindingInflater(inflater, parent, false))
    }
}

abstract class BaseBindingClickBinder<T, V : ViewBinding>(
    viewBindingInflater: (LayoutInflater, ViewGroup, Boolean) -> V
) : BaseBindingBinder<T, V>(viewBindingInflater), ClickableBinder<T> {

    override fun onViewHolderCreated(holder: ViewBindingHolder<T, V>, parent: ViewGroup) {
        super.onViewHolderCreated(holder, parent)
        // 初始化时设置监听器
        holder.itemView.setOnClickListener {
            holder.item?.let {
                onItemClick(it)
            }
        }
    }
}

fun interface ClickableBinder<T> {
    fun onItemClick(item: T)
}
