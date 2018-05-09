package com.chenfei.library.view

import android.support.annotation.LayoutRes
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.iterator

/**
 * 为ViewGroup填充子View帮助类
 * Created by MrFeng on 2017/3/1.
 */
class LikeListViewModel {
    /**
     * 基类接口，不对外使用
     * 布局适配载入工具基类接口，提供判断是否为目标View和对View进行数据绑定接口
     *
     * @param VG   目标ViewGroup容器
     * @param Bean 实例类型
     */
    interface BasicAdapter<VG : ViewGroup, Bean> {
        @MainThread
        fun isView(view: View): Boolean

        @MainThread
        fun onBindView(view: View, bean: Bean)
    }

    /**
     * 用于同步的注入的布局适配载入工具，提供在主线程中进行布局的创建
     *
     * @param VG   目标ViewGroup容器
     * @param Bean 实例类型
     */
    interface Adapter<VG : ViewGroup, Bean> : BasicAdapter<VG, Bean> {
        @MainThread
        fun onCreateView(inflater: LayoutInflater, parent: VG): View
    }

    /**
     * 基类接口，不对外使用
     * 用于异步注入的布局适配载入的基类接口，提供在View创建完成后在主线程中进行初始化
     *
     * @param VG   目标ViewGroup容器
     * @param Bean 实例类型
     */
    interface BasicAsyncAdapter<VG : ViewGroup, Bean> : BasicAdapter<VG, Bean> {
        @MainThread
        fun onViewCreated(view: View)
    }

    /**
     * 用于同步的注入的布局适配载入工具，提供在次线程中进行布局的创建（使用布局文件）
     *
     * @param VG   目标ViewGroup容器
     * @param Bean 实例类型
     */
    interface AsyncAdapter<VG : ViewGroup, Bean> : BasicAsyncAdapter<VG, Bean> {
        @get:LayoutRes
        @get:MainThread
        val layoutId: Int
    }

    /**
     * 用于同步的注入的布局适配载入工具，提供在次线程中进行布局的创建（不使用布局文件）
     *
     * @param VG   目标ViewGroup容器
     * @param Bean 实例类型
     */
    interface AsyncAdapter2<VG : ViewGroup, Bean> : BasicAsyncAdapter<VG, Bean> {
        @WorkerThread
        fun onCreateView(inflater: LayoutInflater, parent: VG): View
    }

    companion object {
        /**
         * 同步的加载布局并注入
         *
         * @param viewGroup    注入的目标ViewGroup
         * @param beanIterable 等待注入的实例集合
         * @param adapter      适配器，用于在主线程创建布局和绑定布局
         */
        @JvmStatic
        fun <Bean, VG : ViewGroup> inject(
                viewGroup: VG, beanIterable: Iterable<Bean>?, adapter: Adapter<VG, Bean>) {
            injectImpl(viewGroup, beanIterable, adapter) { beanIterator ->
                // 布局加载器
                val inflater = LayoutInflater.from(viewGroup.context)
                while (beanIterator.hasNext()) {
                    // 在主线程直接加载布局、加入ViewGroup并绑定数据
                    val view = adapter.onCreateView(inflater, viewGroup)
                    viewGroup.addView(view)
                    adapter.onBindView(view, beanIterator.next())
                }
            }
        }

        /**
         * 异步的的加载布局并注入（使用布局ID）
         *
         * @param viewGroup    注入的目标ViewGroup
         * @param beanIterable 等待注入的实例集合
         * @param adapter      适配器，用于在次线程创建视图和在主线程初始化视图、绑定视图
         */
        @JvmStatic
        fun <Bean, VG : ViewGroup> inject(
                viewGroup: VG, beanIterable: Iterable<Bean>?, adapter: AsyncAdapter<VG, Bean>) {
            injectImpl(viewGroup, beanIterable, adapter) { beanIterator ->
                // 异步布局加载器
                val asyncLayoutInflater = AsyncLayoutInflater(viewGroup.context)
                while (beanIterator.hasNext()) {
                    val bean = beanIterator.next()
                    // 通过异步布局加载器加载子布局
                    asyncLayoutInflater.inflate(adapter.layoutId, viewGroup) { view ->
                        // 布局已加载，通知初始化、加入ViewGroup并绑定数据
                        adapter.onViewCreated(view)
                        viewGroup.addView(view)
                        adapter.onBindView(view, bean)
                    }
                }
            }
        }

        /**
         * 异步的加载布局并注入（不使用布局ID）
         *
         * @param viewGroup    注入的目标ViewGroup
         * @param beanIterable 等待注入的实例迭代器
         * @param adapter      适配器，用于在次线程创建视图和在主线程初始化视图、绑定视图
         */
        @JvmStatic
        fun <Bean, VG : ViewGroup> inject(
                viewGroup: VG, beanIterable: Iterable<Bean>?, adapter: AsyncAdapter2<VG, Bean>) {
            injectImpl(viewGroup, beanIterable, adapter) { beanIterator ->
                // 异步布局加载器
                val asyncLayoutInflater = AsyncLayoutInflater(viewGroup.context)
                while (beanIterator.hasNext()) {
                    val bean = beanIterator.next()
                    // 通过异步布局加载器加载子布局
                    asyncLayoutInflater.inflate(adapter::onCreateView, viewGroup, { view ->
                        // 布局已加载，通知初始化、加入ViewGroup并绑定数据
                        adapter.onViewCreated(view)
                        viewGroup.addView(view)
                        adapter.onBindView(view, bean)
                    })
                }
            }
        }

        /**
         * 将bean列表设置给容器ViewGroup里
         *
         * @param viewGroup    注入的目标ViewGroup
         * @param beanIterable 等待注入的实例列表
         * @param adapter      适配器，用于创建布局和绑定布局
         */
        private inline fun <Bean, VG : ViewGroup, Adapter : BasicAdapter<VG, Bean>> injectImpl(
                viewGroup: VG,
                beanIterable: Iterable<Bean>?,
                adapter: Adapter,
                onCreate: (Iterator<Bean>) -> Unit) {
            if (beanIterable == null) {
                viewGroup.visibility = View.GONE
                return
            }
            // 对适配的数据集判空
            val beanIterator = beanIterable.iterator()
            if (!beanIterator.hasNext()) {
                viewGroup.visibility = View.GONE
                return
            }
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
            // 如果还有bean需要显示，实例化View，适配数据，并将Binding缓存
            if (beanIterator.hasNext()) {
                onCreate(beanIterator)
            }
        }
    }
}
