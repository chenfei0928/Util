package io.github.chenfei0928.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import io.github.chenfei0928.viewbinding.inflateFunc

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-03-26 16:54
 */
inline fun LayoutInflater.inflate(
    @LayoutRes layoutId: Int,
    root: ViewGroup?,
    attachToRoot: Boolean = false,
    applyBlock: View.() -> Unit
): View {
    val inflated = inflate(layoutId, root, false)
    applyBlock(inflated)
    if (attachToRoot) {
        root?.addView(inflated)
    }
    return inflated
}

inline fun <reified VB : ViewBinding> LayoutInflater.inflate(
    root: ViewGroup?,
    attachToRoot: Boolean = false,
    applyBlock: VB.() -> Unit
): VB = inflate(VB::class.java.inflateFunc(), root, attachToRoot, applyBlock)

inline fun <VB : ViewBinding> LayoutInflater.inflate(
    inflateFunc: (LayoutInflater, ViewGroup?, Boolean) -> VB,
    root: ViewGroup?,
    attachToRoot: Boolean = false,
    applyBlock: VB.() -> Unit
): VB {
    val binding = inflateFunc.invoke(this, root, false)
    applyBlock(binding)
    if (attachToRoot) {
        root?.addView(binding.root)
    }
    return binding
}
