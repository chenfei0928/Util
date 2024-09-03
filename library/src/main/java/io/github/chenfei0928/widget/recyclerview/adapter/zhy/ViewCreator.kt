package io.github.chenfei0928.widget.recyclerview.adapter.zhy

import android.view.View
import androidx.annotation.LayoutRes

data class ViewCreator(
    @LayoutRes val layoutId: Int, val view: View?
) {
    var enable: Boolean = true
        get() = (layoutId != 0 || view != null) && field
}
