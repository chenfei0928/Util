package io.github.chenfei0928.view.asyncinflater

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isNotEmpty
import androidx.core.view.iterator
import androidx.recyclerview.widget.RecyclerView
import io.github.chenfei0928.concurrent.ExecutorAndCallback
import io.github.chenfei0928.util.R
import io.github.chenfei0928.view.ViewTagDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * 为ViewGroup填充子View帮助类
 * Created by MrFeng on 2017/3/1.
 */
object BaseLikeListViewInjector {
    inline fun <Bean, R> Iterator<Bean>.forEachInject(
        executorOrScope: Any,
        crossinline command: (Bean) -> R,
        crossinline callback: (R, Bean) -> Unit,
        crossinline onDone: () -> Unit,
    ) = if (!hasNext()) {
        Unit
    } else forEachInjectImpl(executorOrScope, object : ForEachInjectImpl<Bean, R> {
        override fun command(bean: Bean): R = command(bean)
        override fun callback(r: R, bean: Bean) = callback(r, bean)
        override fun onDone() = onDone()
    })

    interface ForEachInjectImpl<Bean, R> {
        fun command(bean: Bean): R
        fun callback(r: R, bean: Bean)
        fun onDone()
    }

    fun <Bean, R> Iterator<Bean>.forEachInjectImpl(
        executorOrScope: Any,
        inject: ForEachInjectImpl<Bean, R>,
    ) {
        if (!hasNext()) {
            return
        } else when (executorOrScope) {
            is ExecutorAndCallback.DirectExecutor -> {
                // 直接执行
                forEach { bean ->
                    inject.callback(inject.command(bean), bean)
                }
                inject.onDone()
            }
            is CoroutineScope -> {
                // 协程，多线程并发创建，并回调
                executorOrScope.launch {
                    Iterable { this@forEachInjectImpl }.map {
                        async(Dispatchers.Default) { it to inject.command(it) }
                    }.forEach {
                        val (bean, r) = it.await()
                        inject.callback(r, bean)
                    }
                    inject.onDone()
                }
            }
            is ExecutorAndCallback -> {
                // 普通执行器，在其线程中挨个执行并回调
                forEach { bean ->
                    executorOrScope.execute(
                        commend = { inject.command(bean) },
                        callback = { inject.callback(it, bean) })
                }
                inject.onDone()
            }
            else -> throw IllegalArgumentException(
                "executorOrScope 需要是 CoroutineScope / ExecutorAndCallback 中一个作为执行器: $executorOrScope"
            )
        }
    }

    /**
     * 将bean列表设置给容器ViewGroup中已存在的view，并返回[beanIterable]中未使用的部分，或如果[beanIterable]已经全部已消费时返回null
     *
     * @param viewGroup    注入的目标ViewGroup
     * @param beanIterable 等待注入的实例列表
     * @param adapter      适配器，用于创建布局和绑定布局
     * @return 仍有需要显示的数据实例时，返回未处理完的迭代器。如果数据已处理完毕，返回null
     */
    @Suppress("ReturnCount")
    fun <Bean, VG : ViewGroup, Adapter : BasicAdapter<VG, Bean>> injectContainedViewImpl(
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
        if (viewGroup.isNotEmpty()) {
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
            by ViewTagDelegate(R.id.cf0928util_viewTag_viewHolder)
    internal var View.injectorClassNameTag: String
            by ViewTagDelegate(R.id.cf0928util_viewTag_injectorClassName)
}
