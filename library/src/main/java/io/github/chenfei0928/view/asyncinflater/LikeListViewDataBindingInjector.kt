package io.github.chenfei0928.view.asyncinflater

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import io.github.chenfei0928.util.R

/**
 * User: ChenFei(chenfei0928@gmail.com)
 * Date: 2019-04-13
 * Time: 16:33
 */
class LikeListViewDataBindingInjector {
    /**
     * 用于同步的注入的布局适配载入工具，提供在主线程中进行布局的创建
     *
     * @param Binding 子itemBinding
     * @param Bean    实例类型
     */
    interface DataBindingAdapter<Binding : ViewDataBinding, Bean> : BasicAdapter<ViewGroup, Bean> {
        @get:LayoutRes
        @get:MainThread
        val layoutId: Int

        @MainThread
        fun onViewCreated(itemBinding: Binding)

        override fun isView(view: View): Boolean {
            return view.getTag(R.id.viewTag_dataBinding) is ViewDataBinding
        }

        override fun onBindView(view: View, bean: Bean) {
            @Suppress("UNCHECKED_CAST")
            onBindView(view.getTag(R.id.viewTag_dataBinding) as Binding, bean)
        }

        fun onBindView(itemBinding: Binding, bean: Bean)
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
        fun <Bean, Binding : ViewDataBinding> inject(
            viewGroup: ViewGroup,
            beanIterable: Iterable<Bean>?,
            adapter: DataBindingAdapter<Binding, Bean>
        ) {
            BaseLikeListViewInjector.injectImpl(viewGroup, beanIterable, adapter) { beanIterator ->
                // 布局加载器
                val inflater = LayoutInflater.from(viewGroup.context)
                beanIterator.forEach { bean ->
                    // 在主线程直接加载布局、加入ViewGroup并绑定数据
                    val binding = DataBindingUtil.inflate<Binding>(
                        inflater, adapter.layoutId, viewGroup, false
                    )
                    binding.root.setTag(R.id.viewTag_dataBinding, binding)
                    adapter.onViewCreated(binding)
                    viewGroup.addView(binding.root)
                    adapter.onBindView(binding, bean)
                }
            }
        }

        /**
         * 同步的加载布局并注入
         *
         * @param viewGroup    注入的目标ViewGroup
         * @param beanIterable 等待注入的实例集合
         * @param adapter      适配器，用于在主线程创建布局和绑定布局
         */
        @JvmStatic
        fun <Bean, Binding : ViewDataBinding> injectAsync(
            viewGroup: ViewGroup,
            beanIterable: Iterable<Bean>?,
            adapter: DataBindingAdapter<Binding, Bean>
        ) {
            BaseLikeListViewInjector.injectImpl(viewGroup, beanIterable, adapter) { beanIterator ->
                // 异步布局加载器
                val asyncLayoutInflater = AsyncLayoutInflater(viewGroup.context)
                beanIterator.forEach { bean ->
                    // 通过异步布局加载器加载子布局
                    asyncLayoutInflater.inflate(adapter.layoutId, viewGroup) { view ->
                        val binding: Binding = DataBindingUtil.bind(view)!!
                        // 布局已加载，通知初始化、加入ViewGroup并绑定数据
                        binding.root.setTag(R.id.viewTag_dataBinding, binding)
                        adapter.onViewCreated(binding)
                        viewGroup.addView(binding.root)
                        adapter.onBindView(binding, bean)
                    }
                }
            }
        }
    }
}
