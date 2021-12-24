package io.github.chenfei0928.collection

import androidx.recyclerview.widget.RecyclerView

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-06-24 11:50
 */
interface RecyclerViewAdapterDataSource {
    var adapterDataObserver: RecyclerView.AdapterDataObserver?
}
