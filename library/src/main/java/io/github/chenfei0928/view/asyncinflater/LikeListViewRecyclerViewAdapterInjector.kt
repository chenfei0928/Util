package io.github.chenfei0928.view.asyncinflater

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerViewHelper
import io.github.chenfei0928.view.asyncinflater.BaseLikeListViewInjector.forEachInject
import io.github.chenfei0928.view.asyncinflater.BaseLikeListViewInjector.injectorClassNameTag
import io.github.chenfei0928.view.asyncinflater.BaseLikeListViewInjector.viewHolderTag

/**
 * 使用adapter方式注入子view
 *
 * 注意：由于无法监听父 [ViewGroup] 的 [View.onDetachedFromWindow] 事件，
 * 所以同时不会回调 adapter 的 [RecyclerView.Adapter.onViewAttachedToWindow]、
 * [RecyclerView.Adapter.onViewDetachedFromWindow]。
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-07-22 17:01
 */
object LikeListViewRecyclerViewAdapterInjector {

    fun <VG : ViewGroup, VH : RecyclerView.ViewHolder> inject(
        asyncLayoutInflater: IAsyncLayoutInflater,
        viewGroup: VG,
        adapter: RecyclerView.Adapter<VH>,
        onDone: () -> Unit = {},
    ) {
        val binderClassName = adapter.javaClass.name
        injectImpl(viewGroup, adapter)?.forEachInject(
            executorOrScope = asyncLayoutInflater.executorOrScope,
            command = { index ->
                val itemViewType = adapter.getItemViewType(index)
                // 加载视图
                val holder = adapter.onCreateViewHolder(viewGroup, itemViewType)
                holder.itemView.viewHolderTag = holder
                RecyclerViewHelper.changeViewType(holder, itemViewType)
                // 记录adapter类名，防止绑定视图错误
                holder.itemView.injectorClassNameTag = binderClassName
                holder
            },
            callback = { holder, index ->
                // 加入viewGroup并设置数据
                viewGroup.addView(holder.itemView)
                adapter.onBindViewHolder(holder, index, emptyList())
            },
            onDone = onDone
        )
    }

    private fun <VG : ViewGroup, VH : RecyclerView.ViewHolder> injectImpl(
        viewGroup: VG, adapter: RecyclerView.Adapter<VH>
    ): Iterator<Int>? = BaseLikeListViewInjector.injectContainedViewImpl(
        viewGroup = viewGroup,
        beanIterable = 0 until adapter.itemCount,
        adapter = object : BasicAdapter<VG, Int> {
            private val binderClassName = adapter.javaClass.name

            override fun isView(view: View): Boolean =
                view.injectorClassNameTag == binderClassName

            override fun onBindView(view: View, bean: Int) {
                @Suppress("UNCHECKED_CAST")
                adapter.onBindViewHolder(view.viewHolderTag as VH, bean, emptyList())
            }
        })
}
