package com.chenfei.view.asyncinflater

import android.view.View
import android.view.ViewGroup
import androidx.core.view.iterator

/**
 * 为ViewGroup填充子View帮助类
 * Created by MrFeng on 2017/3/1.
 */
class BaseLikeListViewInjector {

    companion object {
        /**
         * 将bean列表设置给容器ViewGroup里
         *
         * @param viewGroup    注入的目标ViewGroup
         * @param beanIterable 等待注入的实例列表
         * @param adapter      适配器，用于创建布局和绑定布局
         * @param viewCreator  仍有需要显示的视图时，用来创建新的子视图
         */
        inline fun <Bean, VG : ViewGroup, Adapter : BasicAdapter<VG, Bean>> injectImpl(
            viewGroup: VG,
            beanIterable: Iterable<Bean>?,
            adapter: Adapter,
            viewCreator: (Iterator<Bean>) -> Unit
        ) {
            // 预处理数据和view，并获取未迭代完毕的数据实例迭代器
            val beanIterator = injectImpl(viewGroup, beanIterable, adapter) ?: return
            // 如果还有bean需要显示，交由调用处去创建视图
            viewCreator(beanIterator)
        }

        /**
         * 将bean列表设置给容器ViewGroup里
         *
         * @param viewGroup    注入的目标ViewGroup
         * @param beanIterable 等待注入的实例列表
         * @param adapter      适配器，用于创建布局和绑定布局
         * @return 仍有需要显示的数据实例时，返回未处理完的迭代器。如果数据已处理完毕，返回null
         */
        fun <Bean, VG : ViewGroup, Adapter : BasicAdapter<VG, Bean>> injectImpl(
            viewGroup: VG, beanIterable: Iterable<Bean>?, adapter: Adapter
        ): Iterator<Bean>? {
            // 对适配的数据集判空
            if (beanIterable == null) {
                viewGroup.visibility = View.GONE
                return null
            }
            if (beanIterable is Collection && beanIterable.isEmpty()) {
                viewGroup.visibility = View.GONE
                return null
            }
            // 对适配的数据集判空
            val beanIterator = beanIterable.iterator()
            if (!beanIterator.hasNext()) {
                viewGroup.visibility = View.GONE
                return null
            }
            viewGroup.visibility = View.VISIBLE
            if (viewGroup.childCount > 0) {
                // 对view和bean列表进行迭代
                val viewIterator = viewGroup.iterator()
                while (viewIterator.hasNext()) {
                    val next = viewIterator.next()
                    // 判断当前迭代到的view
                    if (!adapter.isView(next)) {
                        // 如果不是目标itemView
                        viewIterator.remove()
                    } else if (beanIterator.hasNext()) {
                        // 如果是目标itemView，并且有一个要适配到的bean
                        next.visibility = View.VISIBLE
                        adapter.onBindView(next, beanIterator.next())
                    } else {
                        // 如果没有要适配到的bean，隐藏当前行
                        next.visibility = View.GONE
                    }
                }
            }
            // 返回未迭代完毕数据的迭代器
            return if (beanIterator.hasNext()) {
                beanIterator
            } else {
                null
            }
        }
    }
}
