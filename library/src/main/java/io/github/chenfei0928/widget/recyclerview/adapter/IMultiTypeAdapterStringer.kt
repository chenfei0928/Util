package io.github.chenfei0928.widget.recyclerview.adapter

import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.MultiTypeAdapter
import com.drakeet.multitype.MutableTypes
import com.drakeet.multitype.Types
import io.github.chenfei0928.view.findParentFragment
import java.lang.ref.Reference
import java.lang.ref.WeakReference

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
        override var binding: Any = Unit

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            super.onAttachedToRecyclerView(recyclerView)
            if (binding == Unit) {
                binding = recyclerView.findParentFragment()?.let(::WeakReference) ?: binding
            }
        }

        override fun toString(): String {
            val binding = binding
            return if (binding is Reference<*>) {
                super.toString() + ", binding:" + binding.get()
            } else {
                super.toString() + ", binding:" + binding
            }
        }
    }
}
