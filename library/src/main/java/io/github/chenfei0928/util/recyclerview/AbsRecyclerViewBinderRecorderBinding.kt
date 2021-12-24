package io.github.chenfei0928.util.recyclerview

import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.ItemViewDelegate
import com.drakeet.multitype.MultiTypeAdapter
import com.drakeet.multitype.ViewTypeProvider
import io.github.chenfei0928.util.recyclerview.AbsRecyclerViewBinderRecorderBinding.LayoutParams
import com.drakeet.multitype.registerWithTypeRecorderMap

/**
 * 提供bean类型映射保存的 [AbsRecyclerViewBinding]
 * 用于同一种Bean要对应多种layout的使用场景（不限制layoutManager类型）
 *
 * 对[AbsLayoutParamRecyclerViewBinding]进行了添加 一bean类型对多binder支持
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-01-19 16:38
 */
abstract class AbsRecyclerViewBinderRecorderBinding<LP : LayoutParams<*>>(
    contentView: RecyclerView, list: MutableList<Any> = arrayListOf()
) : AbsLayoutParamRecyclerViewBinding<LP>(
    contentView, list
) {
    override fun generateDefaultLayoutParams(): LP {
        return LayoutParams<Any>(null) as LP
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

    open class LayoutParams<T>(
        override var binderClazz: Class<out ItemViewDelegate<T, *>>?
    ) : AbsLayoutParamRecyclerViewBinding.LayoutParams(), ViewTypeProvider
}
