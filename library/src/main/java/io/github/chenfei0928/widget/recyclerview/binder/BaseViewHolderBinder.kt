package io.github.chenfei0928.widget.recyclerview.binder

import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import com.drakeet.multitype.ItemViewBinder
import io.github.chenfei0928.concurrent.ExecutorUtil
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

    open fun onViewHolderCreated(holder: VH, parent: ViewGroup) {
        // noop
    }

    override fun onBindViewHolder(holder: VH, item: T, payloads: List<Any>) {
        holder.item = item
        super.onBindViewHolder(holder, item, payloads)
    }
}
