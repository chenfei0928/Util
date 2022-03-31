package io.github.chenfei0928.binder

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.ItemViewDelegate
import io.github.chenfei0928.reflect.asType
import io.github.chenfei0928.widget.recyclerview.adapter.ViewHolder
import kotlin.reflect.typeOf

/**
 * 内部包含子binder的父binder
 * 最佳实践是在父binder创建一个ViewHolder类，并存储子binder的ViewHolder，
 * 子binder只用去处理其自身的binder逻辑，父binder用于注册各个子binder [registerChildBinder]，
 * 并提供子binder的bindBean获取器
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-01-12 18:10
 */
abstract class BaseViewHolderParentLayoutBinder<T, VH : ViewHolder<T>> :
    BaseViewHolderLayoutBinder<T, VH>() {

    private val childBinders: MutableList<ChildBinderInfo<T, VH, Any, RecyclerView.ViewHolder>> =
        ArrayList()

    /**
     * 注册一个子binder
     *
     * 数据内容如果是null（从[childBeanField]中获取的字段值），将根据[childBinder]的[ChildT]类型是否为空安全
     * 来判断如何处理。如果支持传入null，将会将null传入，由子binder负责空内容时的UI处理逻辑。
     * 如果不支持传入null，将会将子view隐藏，不会调用binder处理。
     *
     * @param ChildT 子binder的bean类型
     * @param ChildViewHolder 子view的ViewHolder
     * @param childBeanField 子binder进行bind时所用bean获取器，如返回值为null，将不会执行子binder的bind操作
     * @param childHolderField 子binder进行bind时所用viewHolder获取器
     * @param childBinder 子binder
     */
    @OptIn(ExperimentalStdlibApi::class)
    inline fun <reified ChildT, ChildViewHolder : RecyclerView.ViewHolder> registerChildBinder(
        noinline childBeanField: (T) -> ChildT?,
        noinline childHolderField: (VH) -> ChildViewHolder,
        childBinder: ItemViewDelegate<ChildT, ChildViewHolder>
    ) {
        val markedNullable = typeOf<ChildT>().isMarkedNullable
        registerChildBinderImpl(childBeanField, childHolderField, childBinder, markedNullable)
    }

    /**
     * 注册一个子binder
     *
     * @param childBeanField 子binder进行bind时所用bean获取器，如返回值为null，将不会执行子binder的bind操作
     * @param childHolderField 子binder进行bind时所用viewHolder获取器
     * @param childBinder 子binder
     */
    fun <CT, CVH : RecyclerView.ViewHolder> registerChildBinderImpl(
        childBeanField: (T) -> CT?,
        childHolderField: (VH) -> CVH,
        childBinder: ItemViewDelegate<CT, CVH>,
        binderAllowNullBean: Boolean
    ) {
        val childBinderInfo = ChildBinderInfo(
            childBinder, childBeanField, childHolderField, binderAllowNullBean
        )
        childBinders.add(childBinderInfo as ChildBinderInfo<T, VH, Any, RecyclerView.ViewHolder>)
    }

    // <editor-fold desc="转发binder回调" defaultstate="collapsed">
    override fun onViewAttachedToWindow(holder: VH) {
        super.onViewAttachedToWindow(holder)
        childBinders.forEach {
            it.childBinder.onViewAttachedToWindow(it.childHolderField(holder))
        }
    }

    override fun onViewDetachedFromWindow(holder: VH) {
        super.onViewDetachedFromWindow(holder)
        childBinders.forEach {
            it.childBinder.onViewDetachedFromWindow(it.childHolderField(holder))
        }
    }

    private var onBindViewHolderIsExecuting = false

    override fun onBindViewHolder(holder: VH, item: T, payloads: List<Any>) {
        onBindViewHolderIsExecuting = true
        holder.item = item
        childBinders.forEach {
            it.onBindViewHolder(holder, item, payloads)
        }
        super.onBindViewHolder(holder, item, payloads)
        onBindViewHolderIsExecuting = false
    }

    override fun onBindViewHolder(holder: VH, item: T) {
        if (!onBindViewHolderIsExecuting) {
            childBinders.forEach {
                it.onBindViewHolder(holder, item, emptyList())
            }
        }
    }

    private fun ChildBinderInfo<T, VH, Any, RecyclerView.ViewHolder>.onBindViewHolder(
        holder: VH, item: T, payloads: List<Any>
    ) {
        val childBean = childBeanField(item)
        val childHolder = childHolderField(holder)
        when {
            binderAllowNullBean -> {
                // 子Binder的Bean是可空的（null值由子binder自己处理）
                childHolder.itemView.isVisible = true
                val childBinder: ItemViewDelegate<Any?, RecyclerView.ViewHolder> =
                    childBinder as ItemViewDelegate<Any?, RecyclerView.ViewHolder>
                childBinder.onBindViewHolder(childHolder, childBean, payloads)
            }
            childBean != null -> {
                // 子Binder的Bean不为空，并且bean也不为null，正常自行数据绑定
                childHolder.itemView.isVisible = true
                childBinder.onBindViewHolder(childHolder, childBean, payloads)
            }
            else -> {
                // bean为null，但子Binder的Bean不支持空类型，在此隐藏该view
                childHolder.itemView.isVisible = false
            }
        }
    }

    override fun onViewHolderCreated(holder: VH, parent: ViewGroup) {
        super.onViewHolderCreated(holder, parent)
        childBinders.forEach {
            if (it.childBinder is BaseViewHolderLayoutBinder) {
                val childHolder = it.childHolderField(holder)
                it.childBinder.onViewHolderCreated(
                    childHolder, childHolder.itemView.parent.asType() ?: parent
                )
            }
        }
    }

    override fun onFailedToRecycleView(holder: VH): Boolean {
        var success = super.onFailedToRecycleView(holder)
        childBinders.forEach {
            success = success or it.childBinder.onFailedToRecycleView(it.childHolderField(holder))
        }
        return success
    }

    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        childBinders.forEach {
            it.childBinder.onViewRecycled(it.childHolderField(holder))
        }
    }
    // </editor-fold>

    private class ChildBinderInfo<ParentT, ParentVH, ChildT, ChildVH : RecyclerView.ViewHolder>(
        val childBinder: ItemViewDelegate<ChildT, ChildVH>,
        val childBeanField: (ParentT) -> ChildT?,
        val childHolderField: (ParentVH) -> ChildVH,
        val binderAllowNullBean: Boolean
    )
}
