package com.chenfei.util.kotlin

import android.app.Activity
import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.collection.LruCache
import androidx.viewbinding.ViewBinding
import java.lang.reflect.Method

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

inline fun <reified T : ViewBinding> Dialog.setContentViewBinding(
    @LayoutRes layoutId: Int
): T {
    return setContentViewBinding(layoutId, T::class.java.bindFunc())
}

private val bindFuncCache = object : LruCache<Class<out ViewBinding>, Method>(16) {
    override fun create(key: Class<out ViewBinding>): Method {
        return key.getMethod("bind", View::class.java)
    }
}

fun <T : ViewBinding> Class<T>.bindFunc(): (View) -> T {
    return {
        bindFuncCache[this]!!.safeInvoke(null, arrayOf(it)) as T
    }
}

private val inflateFuncCache = object : LruCache<Class<out ViewBinding>, Method>(16) {
    override fun create(key: Class<out ViewBinding>): Method {
        return key.getMethod(
            "inflate", LayoutInflater::class.java, ViewGroup::class.java, java.lang.Boolean.TYPE
        )
    }
}

fun <T : ViewBinding> Class<T>.inflateFunc(): (LayoutInflater, ViewGroup?, Boolean) -> T {
    return { layoutInflater, container, attachParent ->
        inflateFuncCache[this]!!.safeInvoke(
            null, arrayOf(layoutInflater, container, attachParent)
        ) as T
    }
}

fun <T : ViewBinding> Dialog.setContentViewBinding(
    @LayoutRes layoutId: Int, bindBlock: (View) -> T
): T {
    this.setContentView(layoutId)
    val decorView: View = window!!.decorView
    val contentView = decorView.findViewById<View>(android.R.id.content) as ViewGroup
    return bindToAddedViews(contentView, 0, bindBlock)
}

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
        TODO()
        // DataBindingUtil.bind(component, children, layoutId)
    }
}
