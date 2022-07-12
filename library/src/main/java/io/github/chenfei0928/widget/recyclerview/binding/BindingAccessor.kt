package io.github.chenfei0928.widget.recyclerview.binding

import com.drakeet.multitype.MultiTypeAdapter

/**
 * 用于子内容替换时访问Binding的成员
 *
 * @author chenfei()
 * @date 2022-07-12 16:56
 */
interface BindingAccessor {
    val adapter: MultiTypeAdapter
    val list: MutableList<Any>

    fun addSingleItem(position: Int, item: Any)
}
