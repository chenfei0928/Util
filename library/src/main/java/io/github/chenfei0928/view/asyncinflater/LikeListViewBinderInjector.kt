package io.github.chenfei0928.view.asyncinflater

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.ItemViewBinder
import com.drakeet.multitype.ItemViewDelegate
import io.github.chenfei0928.util.R
import io.github.chenfei0928.util.ExecutorUtil
import io.github.chenfei0928.util.kotlin.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 使用binder方式注入子view
 *
 * User: ChenFei(chenfei0928@gmail.com)
 * Date: 2019-04-13
 * Time: 16:39
 */
class LikeListViewBinderInjector {
    companion object {

        /**
         * 同步的加载布局并注入
         *
         * @param viewGroup    注入的目标ViewGroup
         * @param beanIterable 等待注入的实例集合
         * @param binder       绑定器，用于在主线程创建布局和绑定布局
         */
        @JvmStatic
        fun <Bean, VG : ViewGroup, ViewHolder : RecyclerView.ViewHolder> inject(
            viewGroup: VG, beanIterable: Iterable<Bean>?, binder: ItemViewBinder<Bean, ViewHolder>
        ) {
            val binderClassName = binder.javaClass.name
            injectImpl(viewGroup, beanIterable, binder) { beanIterator ->
                // 布局加载器
                val inflater = LayoutInflater.from(viewGroup.context)
                beanIterator.forEach { bean ->
                    // 在主线程直接加载布局、加入ViewGroup并绑定数据
                    val holder = binder.onCreateViewHolder(inflater, viewGroup)
                    holder.itemView.setTag(R.id.viewTag_viewHolder, holder)
                    // 记录binder类名，防止绑定视图错误
                    holder.itemView.setTag(R.id.viewTag_injectorClassName, binderClassName)
                    // 加入viewGroup并设置数据
                    viewGroup.addView(holder.itemView)
                    binder.onBindViewHolder(holder, bean, emptyList())
                }
            }
        }

        /**
         * 同步的加载布局并注入
         *
         * @param viewGroup    注入的目标ViewGroup
         * @param beanIterable 等待注入的实例集合
         * @param binder       绑定器，用于在主线程创建布局和绑定布局
         */
        @JvmStatic
        fun <Bean, VG : ViewGroup, ViewHolder : RecyclerView.ViewHolder> injectAsync(
            viewGroup: VG, beanIterable: Iterable<Bean>?, binder: ItemViewBinder<Bean, ViewHolder>
        ) {
            val binderClassName = binder.javaClass.name
            injectImpl(viewGroup, beanIterable, binder) { beanIterator ->
                // 异步布局加载器
                val asyncLayoutInflater = AsyncLayoutInflater(viewGroup.context)
                beanIterator.forEach { bean ->
                    asyncLayoutInflater.inflate({ inflater, vg ->
                        // 加载视图
                        val holder = binder.onCreateViewHolder(inflater, vg)
                        holder.itemView.setTag(R.id.viewTag_viewHolder, holder)
                        // 记录binder类名，防止绑定视图错误
                        holder.itemView.setTag(R.id.viewTag_injectorClassName, binderClassName)
                        return@inflate holder.itemView
                    }, viewGroup, { itemView ->
                        // 加入viewGroup并设置数据
                        viewGroup.addView(itemView)
                        @Suppress("UNCHECKED_CAST") binder.onBindViewHolder(
                            itemView.getTag(R.id.viewTag_viewHolder) as ViewHolder,
                            bean,
                            emptyList()
                        )
                    })
                }
            }
        }

        /**
         * 同步的加载布局并注入
         *
         * @param viewGroup    注入的目标ViewGroup
         * @param beanIterable 等待注入的实例集合
         * @param binder       绑定器，用于在主线程创建布局和绑定布局
         */
        fun <Bean, VG : ViewGroup, ViewHolder : RecyclerView.ViewHolder> injectSuspend(
            lifecycle: LifecycleOwner,
            viewGroup: VG,
            beanIterable: Iterable<Bean>?,
            binder: ItemViewBinder<Bean, ViewHolder>
        ) {
            val binderClassName = binder.javaClass.name
            injectImpl(viewGroup, beanIterable, binder) { beanIterator ->
                // 异步布局加载器
                val asyncLayoutInflater = SuspendLayoutInflater(viewGroup.context, lifecycle)
                beanIterator.forEach { bean ->
                    asyncLayoutInflater.inflate({ inflater, vg ->
                        // 加载视图
                        val holder = binder.onCreateViewHolder(inflater, vg)
                        holder.itemView.setTag(R.id.viewTag_viewHolder, holder)
                        // 记录binder类名，防止绑定视图错误
                        holder.itemView.setTag(R.id.viewTag_injectorClassName, binderClassName)
                        return@inflate holder.itemView
                    }, viewGroup, { itemView ->
                        // 加入viewGroup并设置数据
                        viewGroup.addView(itemView)
                        @Suppress("UNCHECKED_CAST") binder.onBindViewHolder(
                            itemView.getTag(R.id.viewTag_viewHolder) as ViewHolder,
                            bean,
                            emptyList()
                        )
                    })
                }
            }
        }

        /**
         * 同步的加载布局并注入
         *
         * @param viewGroup    注入的目标ViewGroup
         * @param beanIterable 等待注入的实例集合
         * @param binder       绑定器，用于在主线程创建布局和绑定布局
         */
        @JvmStatic
        fun <Bean, VG : ViewGroup, ViewHolder : RecyclerView.ViewHolder> inject(
            viewGroup: VG, beanIterable: Iterable<Bean>?, binder: ItemViewDelegate<Bean, ViewHolder>
        ) {
            val binderClassName = binder.javaClass.name
            injectImpl(viewGroup, beanIterable, binder) { beanIterator ->
                beanIterator.forEach { bean ->
                    // 在主线程直接加载布局、加入ViewGroup并绑定数据
                    val holder = binder.onCreateViewHolder(viewGroup.context, viewGroup)
                    holder.itemView.setTag(R.id.viewTag_viewHolder, holder)
                    // 记录binder类名，防止绑定视图错误
                    holder.itemView.setTag(R.id.viewTag_injectorClassName, binderClassName)
                    // 加入viewGroup并设置数据
                    viewGroup.addView(holder.itemView)
                    binder.onBindViewHolder(holder, bean, emptyList())
                }
            }
        }

        /**
         * 同步的加载布局并注入
         *
         * @param viewGroup    注入的目标ViewGroup
         * @param beanIterable 等待注入的实例集合
         * @param binder       绑定器，用于在主线程创建布局和绑定布局
         */
        @JvmStatic
        fun <Bean, VG : ViewGroup, ViewHolder : RecyclerView.ViewHolder> injectAsync(
            viewGroup: VG, beanIterable: Iterable<Bean>?, binder: ItemViewDelegate<Bean, ViewHolder>
        ) {
            val binderClassName = binder.javaClass.name
            injectImpl(viewGroup, beanIterable, binder) { beanIterator ->
                beanIterator.forEach { bean ->
                    ExecutorUtil.execute({
                        // 加载视图
                        val holder = binder.onCreateViewHolder(viewGroup.context, viewGroup)
                        holder.itemView.setTag(R.id.viewTag_viewHolder, holder)
                        // 记录binder类名，防止绑定视图错误
                        holder.itemView.setTag(R.id.viewTag_injectorClassName, binderClassName)
                        return@execute holder.itemView
                    }, { itemView ->
                        // 加入viewGroup并设置数据
                        viewGroup.addView(itemView)
                        @Suppress("UNCHECKED_CAST") binder.onBindViewHolder(
                            itemView.getTag(R.id.viewTag_viewHolder) as ViewHolder,
                            bean,
                            emptyList()
                        )
                    })
                }
            }
        }

        /**
         * 同步的加载布局并注入
         *
         * @param viewGroup    注入的目标ViewGroup
         * @param beanIterable 等待注入的实例集合
         * @param binder       绑定器，用于在主线程创建布局和绑定布局
         */
        fun <Bean, VG : ViewGroup, ViewHolder : RecyclerView.ViewHolder> injectSuspend(
            lifecycle: LifecycleOwner,
            viewGroup: VG,
            beanIterable: Iterable<Bean>?,
            binder: ItemViewDelegate<Bean, ViewHolder>
        ) {
            val binderClassName = binder.javaClass.name
            injectImpl(viewGroup, beanIterable, binder) { beanIterator ->
                lifecycle.coroutineScope.launch {
                    beanIterator.forEach { bean ->
                        val itemView = withContext(Dispatchers.Default) {
                            // 加载视图
                            val holder = binder.onCreateViewHolder(viewGroup.context, viewGroup)
                            holder.itemView.setTag(R.id.viewTag_viewHolder, holder)
                            // 记录binder类名，防止绑定视图错误
                            holder.itemView.setTag(
                                R.id.viewTag_injectorClassName, binderClassName
                            )
                            holder.itemView
                        }
                        // 加入viewGroup并设置数据
                        viewGroup.addView(itemView)
                        @Suppress("UNCHECKED_CAST") binder.onBindViewHolder(
                            itemView.getTag(R.id.viewTag_viewHolder) as ViewHolder,
                            bean,
                            emptyList()
                        )
                    }
                }
            }
        }

        /**
         * 将bean列表设置给容器ViewGroup里
         *
         * @param viewGroup    注入的目标ViewGroup
         * @param beanIterable 等待注入的实例列表
         * @param binder       Binder绑定器
         * @param viewCreator  仍有需要显示的视图时，用来创建新的子视图
         */
        private inline fun <Bean, VG : ViewGroup, ViewHolder : RecyclerView.ViewHolder> injectImpl(
            viewGroup: VG,
            beanIterable: Iterable<Bean>?,
            binder: ItemViewDelegate<Bean, ViewHolder>,
            crossinline viewCreator: (Iterator<Bean>) -> Unit
        ) {
            val binderClassName = binder.javaClass.name
            BaseLikeListViewInjector.injectImpl(
                viewGroup, beanIterable, object : BasicAdapter<VG, Bean> {
                    override fun isView(view: View): Boolean =
                        view.getTag(R.id.viewTag_injectorClassName) == binderClassName

                    @Suppress("UNCHECKED_CAST")
                    override fun onBindView(view: View, bean: Bean) {
                        binder.onBindViewHolder(
                            view.getTag(R.id.viewTag_viewHolder) as ViewHolder, bean, emptyList()
                        )
                    }
                }, viewCreator
            )
        }
    }
}
