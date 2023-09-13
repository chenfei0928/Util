package io.github.chenfei0928.widget.viewpager

import androidx.viewpager2.widget.ViewPager2
import com.drakeet.multitype.MultiTypeAdapter
import io.github.chenfei0928.widget.recyclerview.adapter.IMultiTypeAdapterStringer
import io.github.chenfei0928.widget.recyclerview.binding.sorted.BaseSortedBinding

/**
 * @author chenf()
 * @date 2023-03-02 15:11
 */
abstract class ViewPager2SortedBinding<E : Any>(
    contentView: ViewPager2,
    adapter: MultiTypeAdapter = IMultiTypeAdapterStringer.IMultiTypeAdapter()
) : BaseSortedBinding<E>(adapter) {

    init {
        contentView.adapter = adapter
    }
}
