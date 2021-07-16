package com.chenfei.view.asyncinflater

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AnyThread
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread

/**
 * User: ChenFei(chenfei0928@gmail.com)
 * Date: 2019-04-13
 * Time: 16:40
 */
class LikeListViewInjector {
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
     * 用于同步的注入的布局适配载入工具，提供在次线程中进行布局的创建（不使用布局文件）
     *
     * @param VG   目标ViewGroup容器
     * @param Bean 实例类型
     */
    interface AsyncAdapter<VG : ViewGroup, Bean> : BasicAdapter<VG, Bean> {
        @AnyThread
        fun onCreateView(inflater: LayoutInflater, parent: VG): View

        @MainThread
        fun onViewCreated(view: View)
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
            viewGroup: VG, beanIterable: Iterable<Bean>?, adapter: Adapter<VG, Bean>
        ) {
            BaseLikeListViewInjector.injectImpl(viewGroup, beanIterable, adapter) { beanIterator ->
                // 布局加载器
                val inflater = LayoutInflater.from(viewGroup.context)
                beanIterator.forEach { bean ->
                    // 在主线程直接加载布局、加入ViewGroup并绑定数据
                    val view = adapter.onCreateView(inflater, viewGroup)
                    viewGroup.addView(view)
                    adapter.onBindView(view, bean)
                }
            }
        }

        /**
         * 异步的的加载布局并注入（使用布局ID）
         * 通过次线程进行布局加载后在主线程进行回调
         * 唯一次线程，且加载通过信息队列方式处理每一条，故不会出现次线程加载后顺序错乱
         *
         * @param viewGroup    注入的目标ViewGroup
         * @param beanIterable 等待注入的实例集合
         * @param layoutId     使用的布局文件的id
         * @param adapter      适配器，用于在次线程创建视图和在主线程初始化视图、绑定视图
         */
        @JvmStatic
        fun <Bean, VG : ViewGroup> injectAsyncWithId(
            viewGroup: VG,
            beanIterable: Iterable<Bean>?,
            @LayoutRes layoutId: Int,
            adapter: AsyncAdapter<VG, Bean>
        ) {
            BaseLikeListViewInjector.injectImpl(viewGroup, beanIterable, adapter) { beanIterator ->
                // 异步布局加载器
                val asyncLayoutInflater = AsyncLayoutInflater(viewGroup.context)
                beanIterator.forEach { bean ->
                    // 通过异步布局加载器加载子布局
                    asyncLayoutInflater.inflate(layoutId, viewGroup) { view ->
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
         * 通过次线程进行布局加载后在主线程进行回调
         * 唯一次线程，且加载通过信息队列方式处理每一条，故不会出现次线程加载后顺序错乱
         *
         * @param viewGroup    注入的目标ViewGroup
         * @param beanIterable 等待注入的实例迭代器
         * @param adapter      适配器，用于在次线程创建视图和在主线程初始化视图、绑定视图
         */
        @JvmStatic
        fun <Bean, VG : ViewGroup> injectAsync(
            viewGroup: VG, beanIterable: Iterable<Bean>?, adapter: AsyncAdapter<VG, Bean>
        ) {
            BaseLikeListViewInjector.injectImpl(viewGroup, beanIterable, adapter) { beanIterator ->
                // 异步布局加载器
                val asyncLayoutInflater = AsyncLayoutInflater(viewGroup.context)
                beanIterator.forEach { bean ->
                    // 通过异步布局加载器加载子布局
                    asyncLayoutInflater.inflate(adapter::onCreateView, viewGroup) { view ->
                        // 布局已加载，通知初始化、加入ViewGroup并绑定数据
                        adapter.onViewCreated(view)
                        viewGroup.addView(view)
                        adapter.onBindView(view, bean)
                    }
                }
            }
        }
    }
}
