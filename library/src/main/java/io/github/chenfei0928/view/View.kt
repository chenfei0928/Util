package io.github.chenfei0928.view

import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.collection.ArrayMap
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManagerCf0928UtilAccessor
import io.github.chenfei0928.content.checkIsDestroyed
import io.github.chenfei0928.content.findActivity
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T : View> ViewGroup.findChildById(@IdRes id: Int): T? {
    forEach {
        if (it.id == id) {
            @Suppress("UNCHECKED_CAST")
            return it as T
        }
    }
    return null
}

fun View.checkContextIsDestroyed(): Boolean = context.checkIsDestroyed()

fun View?.removeSelfFromParent() {
    val viewGroup = this?.parent as? ViewGroup
        ?: return
    viewGroup.removeView(this)
}

/**
 * 根据view查找距离其最近的fragment，参考自
 * [com.bumptech.glide.manager.RequestManagerRetriever.findSupportFragment]
 */
@Suppress("ReturnCount")
fun View.findParentFragment(): Fragment? {
    val findViewFragment = FragmentManagerCf0928UtilAccessor.findViewFragment(this)
    if (findViewFragment != null) {
        return findViewFragment
    }
    val activity = context.findActivity() as? FragmentActivity
        ?: return null
    val viewsToFragment: Map<View, Fragment> = findAllSupportFragmentsWithViews(
        activity.supportFragmentManager.fragments, ArrayMap()
    )
    val activityRootView = activity.findViewById<View>(android.R.id.content)
    var view: View? = this
    while (view != null && view !== activityRootView) {
        val fragment = viewsToFragment[view]
        if (fragment != null) {
            return fragment
        }
        view = view.parent as? View
    }
    return null
}

private fun findAllSupportFragmentsWithViews(
    topLevelFragments: Collection<Fragment?>?,
    result: MutableMap<View, Fragment>
): Map<View, Fragment> {
    topLevelFragments?.forEach { fragment ->
        // getFragment()s in the support FragmentManager may contain null values, see #1991.
        val view = fragment?.view
            ?: return@forEach
        result[view] = fragment
        findAllSupportFragmentsWithViews(
            fragment.childFragmentManager.fragments, result
        )
    }
    return result
}

inline fun <V : View, R> V.getTagOrPut(
    @IdRes id: Int,
    creator: (V) -> R
): R {
    val tag = this.getTag(id)
    return if (tag != null) {
        @Suppress("UNCHECKED_CAST")
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
    @all:IdRes private val id: Int
) : ReadWriteProperty<View, R> {
    override fun getValue(thisRef: View, property: KProperty<*>): R {
        @Suppress("UNCHECKED_CAST")
        return thisRef.getTag(id) as R
    }

    override fun setValue(thisRef: View, property: KProperty<*>, value: R) {
        thisRef.setTag(id, value)
    }
}

/**
 * 字段委托类，通过viewTag来实现为viewHolder扩展字段
 */
class ViewTagValDelegate<V : View, R>(
    @all:IdRes private val id: Int,
    private val creator: (V) -> R
) : ReadOnlyProperty<V, R> {

    override fun getValue(thisRef: V, property: KProperty<*>): R {
        return thisRef.getTagOrPut(id, creator)
    }
}
