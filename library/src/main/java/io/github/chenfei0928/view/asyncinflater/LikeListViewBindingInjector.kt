package io.github.chenfei0928.view.asyncinflater

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.viewbinding.ViewBinding
import io.github.chenfei0928.util.R
import io.github.chenfei0928.view.ViewTagDelegate
import io.github.chenfei0928.view.asyncinflater.BaseLikeListViewInjector.forEachInject
import io.github.chenfei0928.view.asyncinflater.BaseLikeListViewInjector.injectorClassNameTag

/**
 * User: ChenFei(chenfei0928@gmail.com)
 * Date: 2019-04-13
 * Time: 16:33
 */
object LikeListViewBindingInjector {
    /**
     * 用于同步的注入的布局适配载入工具，提供在主线程中进行布局的创建
     *
     * @param Binding 子itemBinding
     * @param Bean    实例类型
     */
    interface DataBindingAdapter<Binding : ViewBinding, Bean> : BasicAdapter<ViewGroup, Bean> {
        fun onCreateView(inflater: LayoutInflater, viewGroup: ViewGroup): Binding

        @MainThread
        fun onViewCreated(itemBinding: Binding)

        override fun isView(view: View): Boolean {
            return view.injectorClassNameTag == this.javaClass.name
        }

        override fun onBindView(view: View, bean: Bean) {
            @Suppress("UNCHECKED_CAST")
            onBindView(view.viewHolderTag as Binding, bean)
        }

        fun onBindView(itemBinding: Binding, bean: Bean)
    }

    /**
     * 同步的加载布局并注入
     *
     * @param asyncLayoutInflater 异步布局加载器
     * @param viewGroup    注入的目标ViewGroup
     * @param beanIterable 等待注入的实例集合
     * @param adapter      适配器，用于在主线程创建布局和绑定布局
     */
    @JvmStatic
    fun <Bean, Binding : ViewBinding> inject(
        asyncLayoutInflater: IAsyncLayoutInflater,
        viewGroup: ViewGroup,
        beanIterable: Iterable<Bean>?,
        adapter: DataBindingAdapter<Binding, Bean>,
        onDone: () -> Unit = {},
    ) {
        val binderClassName = adapter.javaClass.name
        BaseLikeListViewInjector.injectImpl(
            viewGroup, beanIterable, adapter
        )?.forEachInject(asyncLayoutInflater.executorOrScope, {
            // 在主线程直接加载布局、加入ViewGroup并绑定数据
            val binding = adapter.onCreateView(asyncLayoutInflater.inflater, viewGroup)
            binding.root.viewHolderTag = binding
            // 记录binder类名，防止绑定视图错误
            binding.root.injectorClassNameTag = binderClassName
            binding
        }, { binding, bean ->
            // 布局已加载，通知初始化、加入ViewGroup并绑定数据
            adapter.onViewCreated(binding)
            viewGroup.addView(binding.root)
            adapter.onBindView(binding, bean)
        }, onDone)
    }

    private var View.viewHolderTag: ViewBinding
            by ViewTagDelegate(R.id.cf0928util_viewTag_viewHolder)
}
