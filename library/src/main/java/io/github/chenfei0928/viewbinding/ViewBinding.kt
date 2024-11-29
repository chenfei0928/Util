package io.github.chenfei0928.viewbinding

import android.app.Activity
import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.collection.LruCache
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.chenfei0928.reflect.safeInvoke
import io.github.chenfei0928.util.DependencyChecker
import java.lang.reflect.Method

//<editor-fold desc="Activity的setContentViewBinding">
inline fun <reified T : ViewBinding> Activity.setContentViewBinding(
    @LayoutRes layoutId: Int
): T {
    return setContentViewBinding(layoutId, T::class.java.bindFunc())
}

fun <T : ViewBinding> Activity.setContentViewBinding(
    @LayoutRes layoutId: Int, bindBlock: (View) -> T
): T {
    this.setContentView(layoutId)
    val decorView: View = window.decorView
    val contentView = decorView.findViewById<View>(android.R.id.content) as ViewGroup
    return bindToAddedViews(contentView, 0, bindBlock)
}
//</editor-fold>

//<editor-fold desc="Dialog的setContentViewBinding">
inline fun <reified T : ViewBinding> Dialog.setContentViewBinding(
    @LayoutRes layoutId: Int
): T {
    return setContentViewBinding(layoutId, T::class.java.bindFunc())
}

fun <T : ViewBinding> Dialog.setContentViewBinding(
    @LayoutRes layoutId: Int, bindBlock: (View) -> T
): T {
    this.setContentView(layoutId)
    val contentView: ViewGroup = if (DependencyChecker.material && this is BottomSheetDialog) {
        findViewById(com.google.android.material.R.id.design_bottom_sheet)!!
    } else {
        val decorView: View = window!!.decorView
        decorView.findViewById(android.R.id.content)
    }
    return bindToAddedViews(contentView, 0, bindBlock)
}
//</editor-fold>

//<editor-fold desc="获取ViewBinding类的bind方法">
private val bindFuncCache = object : LruCache<Class<out ViewBinding>, Method>(16) {
    override fun create(key: Class<out ViewBinding>): Method {
        return key.getMethod("bind", View::class.java)
    }
}

fun <T : ViewBinding> Class<T>.bindFunc(): (View) -> T = {
    @Suppress("UNCHECKED_CAST")
    bindFuncCache[this]!!.safeInvoke(null, arrayOf(it)) as T
}
//</editor-fold>

//<editor-fold desc="获取ViewBinding类的inflate方法">
private val inflateFuncCache = object : LruCache<Class<out ViewBinding>, Method>(16) {
    override fun create(key: Class<out ViewBinding>): Method {
        return key.getMethod(
            "inflate", LayoutInflater::class.java, ViewGroup::class.java, java.lang.Boolean.TYPE
        )
    }
}

fun <T : ViewBinding> Class<T>.inflateFunc(): (LayoutInflater, ViewGroup?, Boolean) -> T =
    { layoutInflater, container, attachParent ->
        @Suppress("UNCHECKED_CAST")
        inflateFuncCache[this]!!.safeInvoke(
            null, arrayOf(layoutInflater, container, attachParent)
        ) as T
    }
//</editor-fold>

@Suppress("SameParameterValue", "kotlin:S125")
private fun <T : ViewBinding> bindToAddedViews(
    parent: ViewGroup, startChildren: Int, bindBlock: (View) -> T
): T {
    val endChildren = parent.childCount
    val childrenAdded = endChildren - startChildren
    return if (childrenAdded == 1) {
        val childView = parent.getChildAt(endChildren - 1)
        bindBlock(childView)
    } else {
        val children = arrayOfNulls<View>(childrenAdded)
        for (i in 0 until childrenAdded) {
            children[i] = parent.getChildAt(i + startChildren)
        }
        // 只有marge标签会再一次inflate中加载多个直接子view
        throw IllegalArgumentException("ViewBinding不支持marge标签")
        // DataBindingUtil.bind(component, children, layoutId)
    }
}
