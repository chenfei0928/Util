package io.github.chenfei0928.base.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

/**
 * 基础Fragment，所有含有View的fragment必须继承于此类
 *
 * Created by Admin on 2016/3/2.
 */
abstract class BaseFragment : Fragment(), FragmentHost {

    override fun getSupportFragmentManager(): FragmentManager = childFragmentManager
}
