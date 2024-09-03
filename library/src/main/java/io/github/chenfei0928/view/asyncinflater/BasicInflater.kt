package io.github.chenfei0928.view.asyncinflater

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View

/**
 * @author chenf()
 * @date 2024-09-03 15:37
 */
internal class BasicInflater(
    context: Context
) : LayoutInflater(context) {
    override fun cloneInContext(newContext: Context): LayoutInflater {
        return BasicInflater(newContext)
    }

    @Throws(ClassNotFoundException::class)
    override fun onCreateView(name: String, attrs: AttributeSet): View {
        for (prefix in sClassPrefixList) {
            try {
                val view = createView(name, prefix, attrs)
                if (view != null) {
                    return view
                }
            } catch (e: ClassNotFoundException) {
                // In this case we want to let the base class take a crack
                // at it.
            }
        }
        return super.onCreateView(name, attrs)
    }

    companion object {
        private val sClassPrefixList = arrayOf(
            "android.widget.",
            "android.webkit.",
            "android.app."
        )
    }
}
