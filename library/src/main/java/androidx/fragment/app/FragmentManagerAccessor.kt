package androidx.fragment.app

import android.view.View

/**
 * @author chenf()
 * @date 2023-04-06 11:44
 */
object FragmentManagerAccessor {
    fun findViewFragment(v: View): Fragment? {
        var view: View? = v
        while (view != null) {
            val fragment = FragmentManager.getViewFragment(view)
            if (fragment != null) {
                return fragment
            }
            view = view.parent as? View
        }
        return null
    }
}
