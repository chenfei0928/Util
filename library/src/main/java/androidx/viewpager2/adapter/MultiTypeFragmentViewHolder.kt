package androidx.viewpager2.adapter

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import io.github.chenfei0928.widget.recyclerview.adapter.ViewHolder

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2023-03-02 11:35
 */
class MultiTypeFragmentViewHolder<T>(
    val container: FrameLayout
) : ViewHolder<T>(container) {
    companion object {
        fun <T> create(parent: ViewGroup): MultiTypeFragmentViewHolder<T> {
            val container = FrameLayout(parent.context)
            container.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            container.id = ViewCompat.generateViewId()
            container.isSaveEnabled = false
            return MultiTypeFragmentViewHolder(container)
        }
    }
}
