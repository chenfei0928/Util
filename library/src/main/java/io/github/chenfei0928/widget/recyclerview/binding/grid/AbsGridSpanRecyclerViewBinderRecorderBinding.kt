package io.github.chenfei0928.widget.recyclerview.binding.grid

import android.graphics.Rect
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.ItemViewDelegate
import com.drakeet.multitype.MultiTypeAdapter
import com.drakeet.multitype.ViewTypeProvider
import com.drakeet.multitype.registerWithTypeRecorderMap

/**
 * 提供bean类型映射保存的 [AbsGridSpanRecyclerViewBinding]，
 * 用于同一种Bean要对应多种layout的使用场景，扩展由[GridLayoutManager]支持。
 *
 * 对[AbsGridSpanRecyclerViewBinding]进行了添加 一bean类型对多binder支持，
 * 建议用于[GridLayoutManager]、一bean类型对多binder场景。
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-11-19 20:29
 */
abstract class AbsGridSpanRecyclerViewBinderRecorderBinding
<LP : AbsGridSpanRecyclerViewBinderRecorderBinding.LayoutParams<*>>(
    contentView: RecyclerView, spanCount: Int, list: MutableList<Any> = arrayListOf()
) : AbsGridSpanRecyclerViewBinding<LP>(
    contentView, spanCount, list
) {

    //<editor-fold desc="继承并标注废弃的父类方法" defaultstatus="collapsed">
    @Deprecated(
        level = DeprecationLevel.HIDDEN,
        replaceWith = ReplaceWith("generateTypedDefaultLayoutParams()"),
        message = "use generateTypedDefaultLayoutParams function, because its has generic information."
    )
    final override fun generateDefaultLayoutParams(): LP {
        return generateTypedDefaultLayoutParams<Any>()
    }


    @Deprecated(
        level = DeprecationLevel.HIDDEN,
        replaceWith = ReplaceWith("addTypedListItems(position, data, layoutParamsGenerator)"),
        message = "use addTypedListItems function, " +
                "because its 'layoutParamsGenerator' parameter has generic information."
    )
    override fun <E : Any> addListItems(
        position: Int,
        data: List<E>,
        layoutParamsGenerator: (index: Int, item: E) -> LP
    ) {
        super.addListItems(position, data, layoutParamsGenerator)
    }

    @Deprecated(
        level = DeprecationLevel.HIDDEN,
        replaceWith = ReplaceWith("addTypedSingleItem(position, item, layoutParams)"),
        message = "use addTypedSingleItem function, " +
                "because its 'layoutParamsGenerator' parameter has generic information."
    )
    override fun addSingleItem(position: Int, item: Any, layoutParams: LP) {
        super.addSingleItem(position, item, layoutParams)
    }
    //</editor-fold>

    protected fun <T> generateTypedDefaultLayoutParams(): LP {
        @Suppress("UNCHECKED_CAST")
        return LayoutParams<T>(spanCount, NONE, null) as LP
    }

    protected inline fun <T> generateTypedDefaultLayoutParams(block: LP.() -> Unit): LP {
        return generateTypedDefaultLayoutParams<T>().apply(block)
    }

    /**
     * 注册一个类型bean应对多种渲染样式binder。
     * 其将允许bean在使用有[LayoutParams]参数的[addListItems]或[addSingleItem]方法，通过
     * 传入的[LayoutParams.binderClazz]来指定由哪个binder来渲染自身。
     */
    protected fun <T> MultiTypeAdapter.registerWithTypeRecorderMap(
        type: Class<T>, binders: Array<ItemViewDelegate<T, *>>
    ) {
        @Suppress("UNCHECKED_CAST") val layoutParamsRecord =
            layoutParamsRecord as Map<T, LayoutParams<T>>
        registerWithTypeRecorderMap(type, binders, layoutParamsRecord)
    }

    protected open fun <E : Any> addTypedListItems(
        position: Int = list.size,
        data: List<E>,
        layoutParamsGenerator: (index: Int, item: E) -> LP = { _, _ -> generateTypedDefaultLayoutParams<E>() }
    ) {
        super.addListItems(position, data, layoutParamsGenerator)
    }

    protected open fun <T : Any> addTypedSingleItem(
        position: Int = list.size,
        item: T,
        layoutParams: LP = generateTypedDefaultLayoutParams<T>()
    ) {
        super.addSingleItem(position, item, layoutParams)
    }

    open class LayoutParams<T>(
        /**
         * 针对[GridLayoutManager]，设置该item该占用多少个span
         */
        spanSize: Int,
        /**
         * 设置该item的边距，为辅助[GridLayoutManager]在设置span后，
         * 保持两个或多个span情况下列间距与item对容器间距不一致情况的支持处理
         */
        marginInfo: Rect,
        /**
         * 记录某条数据将由哪种binder去渲染这条数据
         */
        override var binderClazz: Class<out ItemViewDelegate<T, *>>? = null
    ) : AbsGridSpanRecyclerViewBinding.LayoutParams(spanSize, marginInfo), ViewTypeProvider
}
