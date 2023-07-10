package io.github.chenfei0928.widget.recyclerview.binding.sorted

import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.MultiTypeAdapter

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2022-09-19 10:54
 */
abstract class RecyclerViewSortedBinding<E : Any>(
    contentView: RecyclerView,
    adapter: MultiTypeAdapter
) : BaseSortedBinding<E>(adapter) {

    init {
        contentView.adapter = adapter
    }
}
