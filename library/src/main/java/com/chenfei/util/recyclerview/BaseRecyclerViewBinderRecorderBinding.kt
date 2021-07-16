package com.chenfei.util.recyclerview

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager

/**
 * 建议用于[LinearLayoutManager]或其它[LayoutManager]、一bean类型对多binder场景
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-01-19 17:14
 */
abstract class BaseRecyclerViewBinderRecorderBinding(
    contentView: RecyclerView, list: MutableList<Any> = arrayListOf()
) : AbsRecyclerViewBinderRecorderBinding<AbsRecyclerViewBinderRecorderBinding.LayoutParams<*>>(
    contentView, list
) {

    @Deprecated(
        level = DeprecationLevel.HIDDEN,
        replaceWith = ReplaceWith("generateTypedDefaultLayoutParams()"),
        message = "use generateTypedDefaultLayoutParams function"
    )
    override fun generateDefaultLayoutParams(): LayoutParams<*> {
        return generateTypedDefaultLayoutParams<Any>()
    }

    protected inline fun <T> generateTypedDefaultLayoutParams(block: LayoutParams<T>.() -> Unit = {}): LayoutParams<T> {
        return LayoutParams<T>(null).apply(block)
    }

    @Deprecated(
        level = DeprecationLevel.HIDDEN,
        replaceWith = ReplaceWith("addTypedListItems(position, data, layoutParamsGenerator)"),
        message = "use addTypedListItems function"
    )
    override fun <E : Any> addListItems(
        position: Int,
        data: List<E>,
        layoutParamsGenerator: (index: Int, item: E) -> LayoutParams<*>
    ) {
        super.addListItems(position, data, layoutParamsGenerator)
    }

    @Deprecated(
        level = DeprecationLevel.HIDDEN,
        replaceWith = ReplaceWith("addTypedSingleItem(position, item, layoutParams)"),
        message = "use addTypedSingleItem function"
    )
    override fun addSingleItem(position: Int, item: Any, layoutParams: LayoutParams<*>) {
        super.addSingleItem(position, item, layoutParams)
    }

    open fun <E : Any> addTypedListItems(
        position: Int = list.size,
        data: List<E>,
        layoutParamsGenerator: (index: Int, item: E) -> LayoutParams<E> = { _, _ -> generateTypedDefaultLayoutParams() }
    ) {
        super.addListItems(position, data, layoutParamsGenerator)
    }

    open fun <T : Any> addTypedSingleItem(
        position: Int = list.size,
        item: T,
        layoutParams: LayoutParams<T> = generateTypedDefaultLayoutParams()
    ) {
        super.addSingleItem(position, item, layoutParams)
    }
}
