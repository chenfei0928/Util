package com.chenfei.util.kotlin

import android.os.Build
import android.view.Choreographer
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
inline fun Choreographer.postFrameCallbackDelayed(
    delayMillis: Long, crossinline block: (Long) -> Unit
) {
    postFrameCallbackDelayed({ block(it) }, delayMillis)
}

fun <T : View> ViewGroup.findChildById(id: Int): T? {
    forEach {
        if (it.id == id) {
            return it as T
        }
    }
    return null
}

fun View.checkContextIsDestroyed(): Boolean = context.checkIsDestroyed()

fun View?.removeSelfFromParent() {
    val viewGroup = this?.parent as? ViewGroup ?: return
    viewGroup.removeView(this)
}

inline fun View.updateTranslationY(block: (translationY: Float) -> Float) {
    val oldTranslationY = translationY
    val newTranslationY = block(oldTranslationY)
    if (oldTranslationY != newTranslationY) {
        translationY = newTranslationY
    }
}

/**
 * 根据view查找距离其最近的fragment
 * [com.bumptech.glide.manager.RequestManagerRetriever.findSupportFragment]
 */
fun View.findParentFragment(): Fragment? {
    val activity = context.findActivity() as? FragmentActivity ?: return null
    val viewsToFragment = mutableMapOf<View, Fragment>()
    findAllSupportFragmentsWithViews(
        activity.supportFragmentManager.fragments, viewsToFragment
    )
    val activityRootView = activity.findViewById<View>(android.R.id.content)
    var view: View? = this
    while (view != null && view != activityRootView) {
        val fragment = viewsToFragment[view]
        if (fragment != null) {
            return fragment
        }
        view = view.parent as? View
    }
    return null
}

private fun findAllSupportFragmentsWithViews(
    topLevelFragments: Collection<Fragment?>?, result: MutableMap<View, Fragment>
) {
    topLevelFragments?.forEach { fragment ->
        // getFragment()s in the support FragmentManager may contain null values, see #1991.
        val view = fragment?.view ?: return@forEach
        result[view] = fragment
        findAllSupportFragmentsWithViews(
            fragment.childFragmentManager.fragments, result
        )
    }
}

inline fun <R> View.getTagOrPut(id: Int, creator: (View) -> R): R {
    val tag = this.getTag(id)
    return if (tag != null) {
        tag as R
    } else {
        creator(this).also {
            this.setTag(id, it)
        }
    }
}

/**
 * 字段委托类，通过viewTag来实现为viewHolder扩展字段
 */
class ViewTagDelegate<R>(
    @IdRes private val id: Int
) : ReadWriteProperty<View, R?> {
    override fun getValue(thisRef: View, property: KProperty<*>): R? {
        return thisRef.getTag(id) as? R
    }

    override fun setValue(thisRef: View, property: KProperty<*>, value: R?) {
        thisRef.setTag(id, value)
    }
}

/**
 * 字段委托类，通过viewTag来实现为viewHolder扩展字段
 */
class ViewTagValDelegate<R>(
    @IdRes private val id: Int, private val creator: (View) -> R
) : ReadOnlyProperty<View, R> {

    override fun getValue(thisRef: View, property: KProperty<*>): R {
        return thisRef.getTagOrPut(id, creator)
    }
}
