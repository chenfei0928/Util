package io.github.chenfei0928.view.asyncinflater

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerViewHelper
import io.github.chenfei0928.util.R
import io.github.chenfei0928.concurrent.ExecutorUtil

/**
 * 使用adapter方式注入子view
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-07-22 17:01
 */
class LikeListViewRecyclerViewAdapterInjector {
    companion object {

        fun <VG : ViewGroup, VH : RecyclerView.ViewHolder> injectAsync(
            viewGroup: VG, adapter: RecyclerView.Adapter<VH>
        ) {
            val binderClassName = adapter.javaClass.name
            injectImpl(viewGroup, adapter) { indexIterator ->
                indexIterator.forEach { index ->
                    ExecutorUtil.execute({
                        val itemViewType = adapter.getItemViewType(index)
                        // 加载视图
                        val holder = adapter.onCreateViewHolder(viewGroup, itemViewType)
                        holder.itemView.setTag(R.id.viewTag_viewHolder, holder)
                        RecyclerViewHelper.changeViewType(holder, itemViewType)
                        // 记录adapter类名，防止绑定视图错误
                        holder.itemView.setTag(R.id.viewTag_injectorClassName, binderClassName)
                        return@execute holder
                    }, { holder ->
                        // 加入viewGroup并设置数据
                        viewGroup.addView(holder.itemView)
                        adapter.onBindViewHolder(holder, index, emptyList())
                    })
                }
            }
        }

        fun <VG : ViewGroup, VH : RecyclerView.ViewHolder> inject(
            viewGroup: VG, adapter: RecyclerView.Adapter<VH>
        ) {
            val binderClassName = adapter.javaClass.name
            injectImpl(viewGroup, adapter) { indexIterator ->
                indexIterator.forEach { index ->
                    val itemViewType = adapter.getItemViewType(index)
                    // 在主线程直接加载布局、加入ViewGroup并绑定数据
                    val holder = adapter.onCreateViewHolder(viewGroup, itemViewType)
                    holder.itemView.setTag(R.id.viewTag_viewHolder, holder)
                    RecyclerViewHelper.changeViewType(holder, itemViewType)
                    // 记录adapter类名，防止绑定视图错误
                    holder.itemView.setTag(R.id.viewTag_injectorClassName, binderClassName)
                    // 加入viewGroup并设置数据
                    viewGroup.addView(holder.itemView)
                    adapter.onBindViewHolder(holder, index, emptyList())
                }
            }
        }

        private inline fun <VG : ViewGroup, VH : RecyclerView.ViewHolder> injectImpl(
            viewGroup: VG, adapter: RecyclerView.Adapter<VH>, viewCreator: (Iterator<Int>) -> Unit
        ) {
            val binderClassName = adapter.javaClass.name
            val items = 0 until adapter.itemCount
            BaseLikeListViewInjector.injectImpl(
                viewGroup, items, object : BasicAdapter<VG, Int> {
                    override fun isView(view: View): Boolean =
                        view.getTag(R.id.viewTag_injectorClassName) == binderClassName

                    override fun onBindView(view: View, bean: Int) {
                        adapter.onBindViewHolder(
                            view.getTag(R.id.viewTag_viewHolder) as VH, bean, emptyList()
                        )
                    }
                }, viewCreator
            )
        }
    }
}
