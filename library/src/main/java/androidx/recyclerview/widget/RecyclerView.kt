@file:JvmName("RecyclerViewCf0928Util")

package androidx.recyclerview.widget

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-11-19 17:29
 */
val RecyclerView.LayoutParams.holder: RecyclerView.ViewHolder
    get() = mViewHolder

/**
 * 启用或禁用RecyclerView的[RecyclerView.Adapter.notifyItemChanged]动画
 */
var RecyclerView.supportsChangeAnimations: Boolean
    get() = (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations == true
    set(value) {
        (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = value
    }
