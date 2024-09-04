package io.github.chenfei0928.view.asyncinflater

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.ItemViewBinder
import com.drakeet.multitype.ItemViewDelegate
import io.github.chenfei0928.view.asyncinflater.BaseLikeListViewInjector.forEachInject
import io.github.chenfei0928.view.asyncinflater.BaseLikeListViewInjector.injectorClassNameTag
import io.github.chenfei0928.view.asyncinflater.BaseLikeListViewInjector.viewHolderTag

/**
 * 使用binder方式注入子view
 *
 * [ItemViewDelegate.onViewDetachedFromWindow]、[ItemViewDelegate.onViewRecycled]
 * 回调时并不意味着其被从父viewGroup中移除，只是它将被隐藏或将要被更新数据。
 *
 * User: ChenFei(chenfei0928@gmail.com)
 * Date: 2019-04-13
 * Time: 16:39
 */
@Suppress("UNCHECKED_CAST")
object LikeListViewBinderInjector {

    /**
     * 同步的加载布局并注入
     *
     * @param asyncLayoutInflater 异步布局加载器
     * @param viewGroup    注入的目标ViewGroup
     * @param beanIterable 等待注入的实例集合
     * @param binder       绑定器，用于在主线程创建布局和绑定布局
     */
    @JvmStatic
    fun <Bean, VG : ViewGroup, ViewHolder : RecyclerView.ViewHolder> inject(
        asyncLayoutInflater: IAsyncLayoutInflater,
        viewGroup: VG,
        beanIterable: Iterable<Bean>?,
        binder: ItemViewDelegate<Bean, ViewHolder>,
    ) {
        val binderClassName = binder.javaClass.name
        injectImpl(
            viewGroup, beanIterable, binder
        )?.forEachInject(asyncLayoutInflater.executorOrScope, {
            // 加载视图
            val holder = if (binder is ItemViewBinder) {
                binder.onCreateViewHolder(asyncLayoutInflater.inflater, viewGroup)
            } else {
                binder.onCreateViewHolder(viewGroup.context, viewGroup)
            }
            holder.itemView.viewHolderTag = holder
            // 记录binder类名，防止绑定视图错误
            holder.itemView.injectorClassNameTag = binderClassName
            holder
        }, { holder, bean ->
            // 加入viewGroup并设置数据
            viewGroup.addView(holder.itemView)
            binder.onBindViewHolder(holder, bean, emptyList())
            binder.onViewAttachedToWindow(holder)
        })
    }

    /**
     * 将bean列表设置给容器ViewGroup里
     *
     * @param viewGroup    注入的目标ViewGroup
     * @param beanIterable 等待注入的实例列表
     * @param binder       Binder绑定器
     */
    private fun <Bean, VG : ViewGroup, ViewHolder : RecyclerView.ViewHolder> injectImpl(
        viewGroup: VG,
        beanIterable: Iterable<Bean>?,
        binder: ItemViewDelegate<Bean, ViewHolder>,
    ): Iterator<Bean>? = BaseLikeListViewInjector.injectImpl(
        viewGroup, beanIterable, object : BasicAdapter<VG, Bean> {
            private val binderClassName = binder.javaClass.name

            override fun isView(view: View): Boolean =
                view.injectorClassNameTag == binderClassName

            override fun onBindView(view: View, bean: Bean) {
                val holder = view.viewHolderTag as ViewHolder
                binder.onViewDetachedFromWindow(holder)
                binder.onViewRecycled(holder)
                binder.onBindViewHolder(holder, bean, emptyList())
                binder.onViewAttachedToWindow(holder)
            }

            override fun onAddOrShow(view: View) {
                val holder = view.viewHolderTag as ViewHolder
                binder.onViewAttachedToWindow(holder)
            }

            override fun onRemoveOrHide(view: View) {
                val holder = view.viewHolderTag as ViewHolder
                binder.onViewDetachedFromWindow(holder)
                binder.onViewRecycled(holder)
            }
        })
}
