package io.github.chenfei0928.widget.recyclerview.adapter

import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.MultiTypeAdapter
import com.drakeet.multitype.MutableTypes
import com.drakeet.multitype.Types

/**
 * 修复 [RecyclerView.exceptionLabel] 只输出adapter信息而难以debug问题
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2023-07-05 17:48
 */
interface IMultiTypeAdapterStringer {
    var binding: Any

    open class IMultiTypeAdapter(
        items: List<Any> = emptyList(),
        initialCapacity: Int = 0,
        types: Types = MutableTypes(initialCapacity),
    ) : MultiTypeAdapter(items, initialCapacity, types), IMultiTypeAdapterStringer {
        override var binding: Any = Any()

        override fun toString(): String {
            return super.toString() + ", binding:" + binding
        }
    }
}
