package io.github.chenfei0928.widget.recyclerview.adapter.zhy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.RecyclerView
import io.github.chenfei0928.collection.contains
import io.github.chenfei0928.widget.recyclerview.adapter.AdapterDelegate
import io.github.chenfei0928.widget.recyclerview.isEmpty

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-10-22 14:39
 */
abstract class AbsWrapperAdapter(
    innerAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
) : AdapterDelegate<RecyclerView.ViewHolder, RecyclerView.Adapter<RecyclerView.ViewHolder>>(
    innerAdapter
) {
    protected val disabled = ViewCreator(0, null)
    internal val headerViews = SparseArrayCompat<ViewCreator>()
    internal val footerViews = SparseArrayCompat<ViewCreator>()
    internal var empty: ViewCreator = disabled
    var showHeaderAndFooterWithNoEmptyHint = false
    internal var loadMore: ViewCreator = disabled

    override fun getItemCount(): Int {
        return if (!isRealEmpty() || (showHeaderAndFooterWithNoEmptyHint && !empty.enable)) {
            // 有内容或无空内容提示时显示header、footer情况下，显示内容、header、footer和加载更多
            innerAdapter.itemCount + headerSize + footerSize + if (loadMore.enable) 1 else 0
        } else if (empty.enable) {
            // 无内容，显示空内容提示
            1
        } else {
            0
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (!isRealEmpty() || (showHeaderAndFooterWithNoEmptyHint && !empty.enable)) {
            // 有内容或无空内容提示时显示header、footer情况下，显示内容、header、footer和加载更多
            when {
                isHeaderViewPos(position) ->
                    // header
                    headerViews.keyAt(position)
                isFooterViewPos(position) ->
                    // footer
                    footerViews.keyAt(position - headerSize - innerAdapter.itemCount)
                isShowLoadMore(position) ->
                    // 加载更多提示
                    ITEM_TYPE_LOAD_MORE
                else ->
                    // 真实内容
                    innerAdapter.getItemViewType(position - headerSize)
            }
        } else {
            // 空内容
            ITEM_TYPE_EMPTY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val headerCreator = headerViews[viewType]
        if (headerCreator != null) {
            return headerCreator.createViewHolder(parent)
        }
        val footerCreator = footerViews[viewType]
        if (footerCreator != null) {
            return footerCreator.createViewHolder(parent)
        }
        return when (viewType) {
            ITEM_TYPE_EMPTY -> empty.createViewHolder(parent)
            ITEM_TYPE_LOAD_MORE -> loadMore.createViewHolder(parent)
            else -> innerAdapter.onCreateViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>
    ) {
        if (holder is WrapperViewHolder) {
            return
        }
        innerAdapter.onBindViewHolder(holder, position - headerSize, payloads)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        WrapperUtils.onAttachedToRecyclerView(
            innerAdapter, recyclerView
        ) callback@{ layoutManager, oldLookup, position ->
            return@callback if (isThisAdapterItem(position)) {
                layoutManager.spanCount
            } else {
                oldLookup.getSpanSize(position - headerSize)
            }
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        if (holder is WrapperViewHolder) {
            WrapperUtils.setFullSpan(holder)
        } else {
            innerAdapter.onViewAttachedToWindow(holder)
        }
    }

    override fun findRelativeAdapterPositionIn(
        adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>,
        viewHolder: RecyclerView.ViewHolder,
        localPosition: Int
    ): Int {
        return if (viewHolder is WrapperViewHolder) {
            RecyclerView.NO_POSITION
        } else {
            super.findRelativeAdapterPositionIn(adapter, viewHolder, localPosition - headerSize)
        }
    }

    override fun getItemId(position: Int): Long {
        return if (isThisAdapterItem(position)) {
            RecyclerView.NO_ID
        } else {
            super.getItemId(position)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder !is WrapperViewHolder) {
            super.onViewRecycled(holder)
        }
    }

    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        return if (holder is WrapperViewHolder) {
            false
        } else {
            super.onFailedToRecycleView(holder)
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        if (holder !is WrapperViewHolder) {
            super.onViewDetachedFromWindow(holder)
        }
    }

    fun notifyViewCreatorEnableChanged(creator: ViewCreator) {
        val index = when {
            creator in headerViews -> {
                headerViews.indexOfValue(creator)
            }
            creator in footerViews -> {
                innerAdapter.itemCount + headerSize + footerViews.indexOfValue(creator)
            }
            creator === loadMore -> {
                headerSize + innerAdapter.itemCount + footerSize
            }
            else -> {
                return
            }
        }
        if (creator.enable) {
            notifyItemInserted(index)
        } else {
            notifyItemRemoved(index)
        }
    }

    internal val headerSize: Int
        get() = headerViews.size()

    internal val footerSize: Int
        get() = footerViews.size()

    protected open fun isRealEmpty() = innerAdapter.isEmpty()

    private fun isThisAdapterItem(position: Int): Boolean =
        isHeaderViewPos(position) || isFooterViewPos(position) || isShowLoadMore(position) || isRealEmpty()

    private fun isHeaderViewPos(position: Int): Boolean {
        if (headerViews.isEmpty) {
            return false
        }
        return position < headerSize
    }

    private fun isFooterViewPos(position: Int): Boolean {
        if (footerViews.isEmpty) {
            return false
        }
        val footerFirstPosition = headerSize + innerAdapter.itemCount
        return position >= footerFirstPosition && position < footerFirstPosition + footerSize
    }

    private fun isShowLoadMore(position: Int): Boolean {
        if (!loadMore.enable) {
            return false
        }
        val loadMorePosition = headerSize + innerAdapter.itemCount + footerSize
        return position >= loadMorePosition
    }

    private fun ViewCreator.createViewHolder(parent: ViewGroup): WrapperViewHolder {
        val view = if (layoutId != 0) {
            LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        } else {
            view
        } ?: throw IllegalArgumentException("无效的ViewCreator: $this")
        return WrapperViewHolder(view)
    }

    companion object {
        const val ITEM_TYPE_EMPTY = Integer.MAX_VALUE - 1
        const val ITEM_TYPE_LOAD_MORE = Integer.MAX_VALUE - 2
        const val BASE_ITEM_TYPE_HEADER = 100000
        const val BASE_ITEM_TYPE_FOOTER = 200000
    }
}

private class WrapperViewHolder(
    itemView: View
) : RecyclerView.ViewHolder(itemView)

data class ViewCreator(
    @LayoutRes val layoutId: Int, val view: View?
) {
    var enable: Boolean = true
        get() = (layoutId != 0 || view != null) && field
}
