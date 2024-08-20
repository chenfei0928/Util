package io.github.chenfei0928.util.glide

import android.graphics.drawable.Drawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.transition.NoTransition
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.request.transition.TransitionFactory

/**
 * [com.bumptech.glide.request.transition.DrawableCrossFadeFactory]
 *
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2019-09-09 16:54
 */
class DrawableCrossFadeFactory(
    private val duration: Int,
    private val isCrossFadeEnabled: Boolean
) : TransitionFactory<Drawable> {

    private val resourceTransition: DrawableCrossFadeTransition by lazy(LazyThreadSafetyMode.NONE) {
        DrawableCrossFadeTransition(duration, isCrossFadeEnabled)
    }

    override fun build(dataSource: DataSource, isFirstResource: Boolean): Transition<Drawable> {
        return if (dataSource == DataSource.MEMORY_CACHE)
            NoTransition.get()
        else resourceTransition
    }
}
