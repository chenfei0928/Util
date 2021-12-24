package io.github.chenfei0928.adapter.zhy

import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import io.github.chenfei0928.util.Log

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-10-22 16:31
 */
class WrapperAdapter(
    innerAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
) : AbsWrapperAdapter(innerAdapter), ListUpdateCallback {
    private val TAG = "KW_WrapperAdapter"
    private var recyclerView: RecyclerView? = null
    internal var lastIsEmpty: Boolean = false
    private var onLoadMoreListener: (WrapperAdapter) -> Unit = {}

    override fun createObserver(): RecyclerView.AdapterDataObserver {
        return WrapperAdapterDataObserver(this)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
        if (holder.itemViewType == ITEM_TYPE_LOAD_MORE) {
            onLoadMoreListener(this)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun isRealEmpty(): Boolean {
        return super
            .isRealEmpty()
            .also {
                if (lastIsEmpty != it && recyclerView?.isComputingLayout != true) {
                    recyclerView?.post {
                        try {
                            notifyDataSetChanged()
                        } catch (e: IllegalStateException) {
                            Log.w(TAG, "isRealEmpty: ", e)
                        }
                    }
                }
                lastIsEmpty = it
            }
    }

    fun setLoadMoreEnable(enable: Boolean) {
        if (loadMore == disabled || loadMore.enable == enable) {
            return
        }
        loadMore.enable = enable
        if (loadMore.enable == enable) {
            notifyViewCreatorEnableChanged(loadMore)
        }
    }

    fun setEmptyView(@LayoutRes layoutId: Int = 0, view: View? = null) {
        empty = if (layoutId == 0 && view == null) {
            disabled
        } else {
            ViewCreator(layoutId, view)
        }
    }

    fun setLoadMoreView(@LayoutRes layoutId: Int = 0, view: View? = null) {
        loadMore = if (layoutId == 0 && view == null) {
            disabled
        } else {
            ViewCreator(layoutId, view)
        }
    }

    fun setOnLoadMoreListener(listener: (WrapperAdapter) -> Unit) {
        onLoadMoreListener = listener
    }

    fun addHeaderView(@LayoutRes layoutId: Int = 0, view: View? = null) {
        if (layoutId == 0 && view == null) {
            return
        }
        headerViews.put(headerSize + BASE_ITEM_TYPE_HEADER, ViewCreator(layoutId, view))
    }

    fun addFooterView(@LayoutRes layoutId: Int = 0, view: View? = null) {
        if (layoutId == 0 && view == null) {
            return
        }
        footerViews.put(footerViews.size() + BASE_ITEM_TYPE_FOOTER, ViewCreator(layoutId, view))
    }

    override fun onInserted(position: Int, count: Int) {
        notifyItemRangeInserted(position + headerSize, count)
    }

    override fun onRemoved(position: Int, count: Int) {
        notifyItemRangeRemoved(position + headerSize, count)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        notifyItemMoved(fromPosition + headerSize, toPosition + headerSize)
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        notifyItemRangeChanged(position + headerSize, count, payload)
    }
}
