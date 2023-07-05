package androidx.viewpager2.adapter

import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.Types

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2023-03-13 16:12
 */
interface AttachedTypes : Types {
    var adapter: FragmentStateMultiTypeAdapter

    fun onAttachedToRecyclerView(recyclerView: RecyclerView)

    fun onDetachedFromRecyclerView(recyclerView: RecyclerView)
}
