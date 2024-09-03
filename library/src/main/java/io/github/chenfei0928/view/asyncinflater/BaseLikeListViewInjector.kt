package io.github.chenfei0928.view.asyncinflater

import android.view.View
import android.view.ViewGroup
import androidx.core.view.iterator
import androidx.recyclerview.widget.RecyclerView
import io.github.chenfei0928.concurrent.ExecutorAndCallback
import io.github.chenfei0928.util.R
import io.github.chenfei0928.view.ViewTagDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 为ViewGroup填充子View帮助类
 * Created by MrFeng on 2017/3/1.
 */
object BaseLikeListViewInjector {
    inline fun <Bean, R> Iterator<Bean>.forEachInject(
        executorOrScope: Any,
        crossinline command: (Bean) -> R,
        crossinline callback: (R, Bean) -> Unit
    ) {
        if (!hasNext()) {
            return
        } else when (executorOrScope) {
            is CoroutineScope -> {
                executorOrScope.launch {
                    forEach { bean ->
                        val r = withContext(Dispatchers.Default) {
                            command(bean)
                        }
                        callback(r, bean)
                    }
                }
            }
            is ExecutorAndCallback -> {
                forEach { bean ->
                    executorOrScope.execute({
                        command(bean)
                    }) {
                        callback(it, bean)
                    }
                }
            }
            else -> throw IllegalArgumentException(
                "executorOrScope 需要是 CoroutineScope / ExecutorAndCallback 中一个作为执行器: $executorOrScope"
            )
        }
    }

    /**
     * 将bean列表设置给容器ViewGroup里
     *
     * @param viewGroup    注入的目标ViewGroup
     * @param beanIterable 等待注入的实例列表
     * @param adapter      适配器，用于创建布局和绑定布局
     * @return 仍有需要显示的数据实例时，返回未处理完的迭代器。如果数据已处理完毕，返回null
     */
    @Suppress("ReturnCount")
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
                    adapter.onRemoveOrHide(next)
                    next.visibility = View.VISIBLE
                    adapter.onBindView(next, beanIterator.next())
                    adapter.onAddOrShow(next)
                } else {
                    // 如果没有要适配到的bean，隐藏当前行
                    next.visibility = View.GONE
                    adapter.onRemoveOrHide(next)
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

    internal var View.viewHolderTag: RecyclerView.ViewHolder
            by ViewTagDelegate(R.id.viewTag_viewHolder)
    internal var View.injectorClassNameTag: String
            by ViewTagDelegate(R.id.viewTag_injectorClassName)
}
