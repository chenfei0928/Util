package io.github.chenfei0928.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-03-26 16:54
 */
inline fun LayoutInflater.inflate(
    @LayoutRes layoutId: Int,
    root: ViewGroup,
    applyBlock: View.() -> Unit
): View {
    val inflated = inflate(layoutId, root, false)
    applyBlock(inflated)
    root.addView(inflated)
    return inflated
}
