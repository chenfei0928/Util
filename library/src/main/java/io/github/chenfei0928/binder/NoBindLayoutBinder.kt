package io.github.chenfei0928.binder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import io.github.chenfei0928.widget.recyclerview.adapter.ViewHolder

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-01-18 17:16
 */
abstract class BaseNoBindLayoutBinder<T> : BaseViewHolderLayoutBinder<T, ViewHolder<T>>() {

    final override fun onBindViewHolder(holder: ViewHolder<T>, item: T, payloads: List<Any>) {
        super.onBindViewHolder(holder, item, payloads)
    }

    final override fun onBindViewHolder(holder: ViewHolder<T>, item: T) {}

    final override fun getItemId(item: T): Long {
        return super.getItemId(item)
    }

    final override fun onFailedToRecycleView(holder: ViewHolder<T>): Boolean {
        return super.onFailedToRecycleView(holder)
    }

    final override fun onViewAttachedToWindow(holder: ViewHolder<T>) {
        super.onViewAttachedToWindow(holder)
    }

    final override fun onViewDetachedFromWindow(holder: ViewHolder<T>) {
        super.onViewDetachedFromWindow(holder)
    }

    final override fun onViewRecycled(holder: ViewHolder<T>) {
        super.onViewRecycled(holder)
    }
}

/**
 * 仅用于无内容绑定实现且无点击事件的binder
 */
open class NoBindLayoutBinder<T>(
    @LayoutRes private val layout: Int
) : BaseNoBindLayoutBinder<T>() {

    override fun onCreateViewHolderImpl(
        inflater: LayoutInflater, parent: ViewGroup
    ): ViewHolder<T> {
        return ViewHolder(inflater.inflate(layout, parent, false))
    }

    final override fun onViewHolderCreated(holder: ViewHolder<T>, parent: ViewGroup) {
        super.onViewHolderCreated(holder, parent)
    }
}

/**
 * 仅用于无内容绑定实现，但有点击事件的binder
 */
abstract class NoBindLayoutClickListenerBinder<T>(
    @LayoutRes private val layout: Int
) : BaseNoBindLayoutBinder<T>(), ClickableBinder<T> {

    final override fun onCreateViewHolderImpl(
        inflater: LayoutInflater, parent: ViewGroup
    ): ViewHolder<T> {
        return ViewHolder(inflater.inflate(layout, parent, false))
    }

    final override fun onViewHolderCreated(holder: ViewHolder<T>, parent: ViewGroup) {
        super.onViewHolderCreated(holder, parent)
        // 初始化时设置监听器
        holder.itemView.setOnClickListener {
            holder.item?.let {
                onItemClick(it)
            }
        }
    }
}
