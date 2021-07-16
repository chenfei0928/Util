package com.chenfei.base.fragment

import androidx.fragment.app.FragmentManager

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-10-28 16:47
 */
interface FragmentHost {
    fun getSupportFragmentManager(): FragmentManager
}
