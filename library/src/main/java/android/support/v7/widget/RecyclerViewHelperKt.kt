package android.support.v7.widget

/**
 * 仅用于在[com.chenfei.library.adapter.MultiAdapterWrapper]中用于修改viewType并交由子Adapter调用使用
 */
internal class RecyclerViewHelperKt {
    companion object {
        @JvmStatic
        internal fun changeViewType(viewHolder: RecyclerView.ViewHolder, viewType: Int) {
            viewHolder.mItemViewType = viewType
        }
    }
}
