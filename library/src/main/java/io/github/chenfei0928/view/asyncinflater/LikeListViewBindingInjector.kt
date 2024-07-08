package io.github.chenfei0928.view.asyncinflater

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.viewbinding.ViewBinding
import io.github.chenfei0928.util.R

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
            return view.getTag(R.id.viewTag_viewHolder) is ViewBinding
                    && view.getTag(R.id.viewTag_injectorClassName) == this.javaClass.name
        }

        override fun onBindView(view: View, bean: Bean) {
            @Suppress("UNCHECKED_CAST")
            onBindView(view.getTag(R.id.viewTag_viewHolder) as Binding, bean)
        }

        fun onBindView(itemBinding: Binding, bean: Bean)
    }

    /**
     * 同步的加载布局并注入
     *
     * @param viewGroup    注入的目标ViewGroup
     * @param beanIterable 等待注入的实例集合
     * @param adapter      适配器，用于在主线程创建布局和绑定布局
     */
    @JvmStatic
    fun <Bean, Binding : ViewBinding> inject(
        viewGroup: ViewGroup,
        beanIterable: Iterable<Bean>?,
        adapter: DataBindingAdapter<Binding, Bean>
    ) {
        BaseLikeListViewInjector.injectImpl(viewGroup, beanIterable, adapter) { beanIterator ->
            // 布局加载器
            val inflater = LayoutInflater.from(viewGroup.context)
            beanIterator.forEach { bean ->
                // 在主线程直接加载布局、加入ViewGroup并绑定数据
                val binding = adapter.onCreateView(inflater, viewGroup)
                binding.root.setTag(R.id.viewTag_viewHolder, binding)
                // 记录binder类名，防止绑定视图错误
                binding.root.setTag(R.id.viewTag_injectorClassName, adapter.javaClass.name)
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
    fun <Bean, Binding : ViewBinding> injectAsyncViewBinding(
        viewGroup: ViewGroup,
        beanIterable: Iterable<Bean>?,
        adapter: DataBindingAdapter<Binding, Bean>
    ) {
        BaseLikeListViewInjector.injectImpl(viewGroup, beanIterable, adapter) { beanIterator ->
            // 异步布局加载器
            val asyncLayoutInflater = AsyncLayoutInflater(viewGroup.context)
            beanIterator.forEach { bean ->
                // 通过异步布局加载器加载子布局
                asyncLayoutInflater.inflate<ViewGroup>({ inflater: LayoutInflater, vg: ViewGroup ->
                    // 在主线程直接加载布局、加入ViewGroup并绑定数据
                    val binding = adapter.onCreateView(inflater, vg)
                    binding.root.setTag(R.id.viewTag_viewHolder, binding)
                    binding.root
                }, viewGroup) { view ->
                    // 布局已加载，通知初始化、加入ViewGroup并绑定数据
                    val binding: Binding = view.getTag(R.id.viewTag_viewHolder) as Binding
                    // 记录binder类名，防止绑定视图错误
                    binding.root.setTag(R.id.viewTag_injectorClassName, adapter.javaClass.name)
                    adapter.onViewCreated(binding)
                    viewGroup.addView(binding.root)
                    adapter.onBindView(binding, bean)
                }
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
    fun <Bean, Binding : ViewDataBinding> injectAsyncDataBinding(
        viewGroup: ViewGroup,
        beanIterable: Iterable<Bean>?,
        adapter: DataBindingAdapter<Binding, Bean>
    ) {
        BaseLikeListViewInjector.injectImpl(viewGroup, beanIterable, adapter) { beanIterator ->
            // 异步布局加载器
            val asyncLayoutInflater = AsyncLayoutInflater(viewGroup.context)
            beanIterator.forEach { bean ->
                // 通过异步布局加载器加载子布局
                asyncLayoutInflater.inflate<ViewGroup>({ inflater: LayoutInflater, vg: ViewGroup ->
                    // 在主线程直接加载布局、加入ViewGroup并绑定数据
                    adapter.onCreateView(inflater, vg).root
                }, viewGroup) { view ->
                    val binding: Binding = DataBindingUtil.bind(view)!!
                    // 布局已加载，通知初始化、加入ViewGroup并绑定数据
                    binding.root.setTag(R.id.viewTag_viewHolder, binding)
                    // 记录binder类名，防止绑定视图错误
                    binding.root.setTag(R.id.viewTag_injectorClassName, adapter.javaClass.name)
                    adapter.onViewCreated(binding)
                    viewGroup.addView(binding.root)
                    adapter.onBindView(binding, bean)
                }
            }
        }
    }
}
