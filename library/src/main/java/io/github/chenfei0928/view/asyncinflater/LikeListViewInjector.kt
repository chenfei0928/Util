package io.github.chenfei0928.view.asyncinflater

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AnyThread
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import io.github.chenfei0928.view.asyncinflater.BaseLikeListViewInjector.forEachInject
import io.github.chenfei0928.view.asyncinflater.BaseLikeListViewInjector.injectorClassNameTag

/**
 * User: ChenFei(chenfei0928@gmail.com)
 * Date: 2019-04-13
 * Time: 16:40
 */
object LikeListViewInjector {
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

        override fun isView(view: View): Boolean {
            return view.injectorClassNameTag == this.javaClass.name
        }
    }

    /**
     * 异步的的加载布局并注入（使用布局ID）
     * 通过次线程进行布局加载后在主线程进行回调
     * 唯一次线程，且加载通过信息队列方式处理每一条，故不会出现次线程加载后顺序错乱
     *
     * @param asyncLayoutInflater 异步布局加载器
     * @param viewGroup    注入的目标ViewGroup
     * @param beanIterable 等待注入的实例集合
     * @param layoutId     使用的布局文件的id
     * @param adapter      适配器，用于在次线程创建视图和在主线程初始化视图、绑定视图
     */
    @JvmStatic
    fun <Bean, VG : ViewGroup> injectWithId(
        asyncLayoutInflater: IAsyncLayoutInflater,
        viewGroup: VG,
        beanIterable: Iterable<Bean>?,
        @LayoutRes layoutId: Int,
        adapter: AsyncAdapter<VG, Bean>
    ) {
        BaseLikeListViewInjector.injectImpl(
            viewGroup, beanIterable, adapter
        )?.forEachInject(asyncLayoutInflater.executorOrScope, {
            val view = asyncLayoutInflater.inflater.inflate(layoutId, viewGroup, false)
            // 记录binder类名，防止绑定视图错误
            view.injectorClassNameTag = adapter.javaClass.name
            view
        }) { view, bean ->
            // 布局已加载，通知初始化、加入ViewGroup并绑定数据
            adapter.onViewCreated(view)
            viewGroup.addView(view)
            adapter.onBindView(view, bean)
        }
    }

    /**
     * 异步的加载布局并注入（不使用布局ID）
     * 通过次线程进行布局加载后在主线程进行回调
     * 唯一次线程，且加载通过信息队列方式处理每一条，故不会出现次线程加载后顺序错乱
     *
     * @param asyncLayoutInflater 异步布局加载器
     * @param viewGroup    注入的目标ViewGroup
     * @param beanIterable 等待注入的实例迭代器
     * @param adapter      适配器，用于在次线程创建视图和在主线程初始化视图、绑定视图
     */
    @JvmStatic
    fun <Bean, VG : ViewGroup> inject(
        asyncLayoutInflater: IAsyncLayoutInflater,
        viewGroup: VG, beanIterable: Iterable<Bean>?, adapter: AsyncAdapter<VG, Bean>
    ) {
        BaseLikeListViewInjector.injectImpl(
            viewGroup, beanIterable, adapter
        )?.forEachInject(asyncLayoutInflater.executorOrScope, {
            val view = adapter.onCreateView(asyncLayoutInflater.inflater, viewGroup)
            // 记录binder类名，防止绑定视图错误
            view.injectorClassNameTag = adapter.javaClass.name
            view
        }) { view, bean ->
            // 布局已加载，通知初始化、加入ViewGroup并绑定数据
            adapter.onViewCreated(view)
            viewGroup.addView(view)
            adapter.onBindView(view, bean)
        }
    }
}
