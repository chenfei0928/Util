package io.github.chenfei0928.base

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import java.util.WeakHashMap

/**
 * 用于保存Fragment的详细生命周期
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-02-17 18:42
 */
internal class FragmentViewLifecycleCallback : FragmentManager.FragmentLifecycleCallbacks() {
    private val fragmentStateMap = WeakHashMap<Fragment, State>()

    fun getState(f: Fragment): State = fragmentStateMap[f] ?: State.Constructed

    private fun notifyStateUpdate(f: Fragment, state: State) {
        fragmentStateMap[f] = state
    }

    override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) {
        notifyStateUpdate(f, State.PreAttached)
    }

    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
        notifyStateUpdate(f, State.Attached)
    }

    override fun onFragmentPreCreated(
        fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?
    ) {
        notifyStateUpdate(f, State.PreCreated)
    }

    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        notifyStateUpdate(f, State.Created)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onFragmentActivityCreated(
        fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?
    ) {
        notifyStateUpdate(f, State.ActivityCreated)
    }

    override fun onFragmentViewCreated(
        fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?
    ) {
        notifyStateUpdate(f, State.ViewCreated)
    }

    override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
        notifyStateUpdate(f, State.Started)
    }

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        notifyStateUpdate(f, State.Resumed)
    }

    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
        notifyStateUpdate(f, State.Paused)
    }

    override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
        notifyStateUpdate(f, State.Stopped)
    }

    override fun onFragmentSaveInstanceState(fm: FragmentManager, f: Fragment, outState: Bundle) {
        notifyStateUpdate(f, State.SaveInstanceState)
    }

    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        notifyStateUpdate(f, State.ViewDestroyed)
    }

    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        notifyStateUpdate(f, State.Destroyed)
    }

    override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
        notifyStateUpdate(f, State.Detached)
    }

    enum class State {
        Constructed,
        PreAttached,
        Attached,
        PreCreated,
        Created,
        ActivityCreated,
        ViewCreated,
        Started,
        Resumed,
        Paused,
        Stopped,
        SaveInstanceState,
        ViewDestroyed,
        Destroyed,
        Detached
    }
}