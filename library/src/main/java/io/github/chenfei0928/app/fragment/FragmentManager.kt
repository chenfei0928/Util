/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-10-31 11:03
 */
@file:Suppress("TooManyFunctions")

package io.github.chenfei0928.app.fragment

import androidx.annotation.IdRes
import androidx.annotation.Size
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

inline fun <reified F> FragmentManager.forEachFragmentWithChildByType(block: (F) -> Unit) {
    forEachFragmentWithChildByType(F::class.java, block)
}

@Suppress("UNCHECKED_CAST")
inline fun <F> FragmentManager.forEachFragmentWithChildByType(clazz: Class<F>, block: (F) -> Unit) {
    getAllChildFragment().forEach {
        if (clazz.isInstance(it)) {
            block(it as F)
        }
    }
}

fun FragmentManager.getAllChildFragment(output: MutableList<Fragment> = ArrayList()): MutableList<Fragment> {
    output.addAll(this.fragments)
    for (fragment in this.fragments) {
        if (fragment.host == null) continue
        fragment.childFragmentManager.getAllChildFragment(output)
    }
    return output
}

inline fun <reified F> FragmentManager.findFragmentByType(): F? {
    return fragments.find { it is F } as? F
}

inline fun <reified F> FragmentManager.findFragmentWithChildByType(): F? {
    fragments.forEach {
        val findWithChildByType = it.findFragmentWithChildByType(F::class.java)
        if (findWithChildByType != null) {
            return findWithChildByType
        }
    }
    return null
}

/**
 * 向自身和子类遍历并查找实现了制定类或接口的实例
 */
@Suppress("UNCHECKED_CAST")
fun <F> Fragment.findFragmentWithChildByType(clazz: Class<F>): F? {
    return if (clazz.isInstance(this)) {
        this as F
    } else if (host == null) {
        null
    } else {
        childFragmentManager.fragments.forEach {
            val findWithChildByType = it.findFragmentWithChildByType(clazz)
            if (findWithChildByType != null) {
                return findWithChildByType
            }
        }
        null
    }
}

inline fun <reified F : Fragment> FragmentManager.findOrAddChild(
    @IdRes id: Int,
    @Size(min = 1) tag: String,
    commitNow: Boolean = false,
    noinline creator: () -> F
): F = findOrAddChild(id, tag, F::class.java, commitNow, creator)

/**
 * 查找fragment或创建并加入fragment中
 */
inline fun <F : Fragment> FragmentManager.findOrAddChild(
    @IdRes id: Int,
    @Size(min = 1) tag: String,
    clazz: Class<F>,
    commitNow: Boolean = false,
    creator: () -> F
): F = findOrOptionChild(tag, clazz, commitNow, creator) {
    add(id, it, tag)
}

inline fun <reified F : Fragment> FragmentManager.findOrAddChild(
    @Size(min = 1) tag: String, commitNow: Boolean = false, creator: () -> F
): F = findOrAddChild(tag, F::class.java, commitNow, creator)

/**
 * 查找fragment或创建并加入fragment中（不显示其）
 */
inline fun <F : Fragment> FragmentManager.findOrAddChild(
    @Size(min = 1) tag: String, clazz: Class<F>, commitNow: Boolean = false, creator: () -> F
): F = findOrOptionChild(tag, clazz, commitNow, creator) {
    add(it, tag)
}

inline fun <reified F : Fragment> FragmentManager.findOrReplaceChild(
    @IdRes containerViewId: Int,
    @Size(min = 1) tag: String,
    commitNow: Boolean = false,
    creator: () -> F
): F = findOrReplaceChild(containerViewId, tag, F::class.java, commitNow, creator)

/**
 * 查找fragment或创建并替换到view中
 */
inline fun <F : Fragment> FragmentManager.findOrReplaceChild(
    @IdRes containerViewId: Int,
    @Size(min = 1) tag: String,
    clazz: Class<F>,
    commitNow: Boolean = false,
    creator: () -> F
): F = findOrOptionChild(tag, clazz, commitNow, creator) {
    replace(containerViewId, it, tag)
}

/**
 * 从已加载的fragment中找，如未找到对应的fragment，创建一个并对其进行操作以使其加入
 */
inline fun <F : Fragment> FragmentManager.findOrOptionChild(
    @Size(min = 1) tag: String,
    clazz: Class<F>,
    commitNow: Boolean = false,
    creator: () -> F,
    option: FragmentTransaction.(F) -> FragmentTransaction
): F {
    return findAndCheckTypeByTagOrNullInlineOnly(tag, clazz) ?: run {
        // 如果不是该类实例，创建新的并加入host
        creator().also { f ->
            option(beginTransaction(), f).run {
                if (commitNow) {
                    commitNowAllowingStateLoss()
                } else {
                    commitAllowingStateLoss()
                }
            }
        }
    }
}

fun <F : Fragment> FragmentManager.findAndCheckTypeByTagOrNullInlineOnly(
    @Size(min = 1) tag: String,
    clazz: Class<F>,
): F? {
    // 先获取fragment是否已经加入host
    val fragment = findFragmentByTag(tag)
    // fragment不为空但不是该类实例，移除它
    if (fragment != null && !clazz.isInstance(fragment)) {
        beginTransaction()
            .remove(fragment)
            .commitNowAllowingStateLoss()
    }
    // 检查获取到的fragment
    return if (clazz.isInstance(fragment)) {
        clazz.cast(fragment)!!
    } else null
}
