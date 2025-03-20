package androidx.fragment.app

import android.os.Bundle

/**
 * [Fragment.restoreChildFragmentState]
 *
 * @author chenf()
 * @date 2023-08-16 18:06
 */
fun Bundle.removeChildFragmentState() {
    remove("android:support:fragments")
    remove(FragmentStateManager.CHILD_FRAGMENT_MANAGER_KEY)
}
