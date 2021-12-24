package io.github.chenfei0928.base.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-08-26 14:52
 */
interface OnBackPressedFragment {
    /**
     * 返回true，则为以消费该事件
     */
    fun onBackPressed(): Boolean
}

fun FragmentActivity.doChildFragmentBackPressed(): Boolean {
    for (fragment in supportFragmentManager.fragments) {
        if (fragment is OnBackPressedFragment) {
            // 对所有的子Fragment检查，如果其在显示状态，询问其是否消费返回事件
            if (fragment.isResumed && fragment.isVisible) {
                if (fragment.onBackPressed()) {
                    return true
                }
            }
        }
    }
    return false
}

fun Fragment.doChildFragmentBackPressed(): Boolean {
    for (fragment in childFragmentManager.fragments) {
        if (fragment is OnBackPressedFragment) {
            // 对所有的子Fragment检查，如果其在显示状态，询问其是否消费返回事件
            if (fragment.isResumed && fragment.isVisible) {
                if (fragment.onBackPressed()) {
                    return true
                }
            }
        }
    }
    return false
}
