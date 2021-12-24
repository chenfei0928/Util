package io.github.chenfei0928.widget

import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.vlayout.RecyclablePagerAdapter
import io.github.chenfei0928.util.R

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-03-22 17:35
 */
class BinderPagerAdapter(
    val adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
) : RecyclablePagerAdapter<RecyclerView.ViewHolder>(adapter, RecyclerView.RecycledViewPool()) {

    override fun getCount(): Int {
        return adapter.itemCount
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.setTag(R.id.viewTag_dataBinding, holder)
        adapter.onBindViewHolder(holder, position, emptyList())
    }

    override fun getItemViewType(position: Int): Int {
        return adapter.getItemViewType(position)
    }
}
