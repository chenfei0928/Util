package io.github.chenfei0928.base.fragment

import androidx.fragment.app.FragmentManager

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-10-28 16:47
 */
fun interface FragmentHost {
    fun getSupportFragmentManager(): FragmentManager
}
