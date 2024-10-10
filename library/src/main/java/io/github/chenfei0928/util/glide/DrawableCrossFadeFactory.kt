package io.github.chenfei0928.util.glide

import android.graphics.drawable.Drawable
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.NoTransition
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.request.transition.TransitionFactory

/**
 * like：[com.bumptech.glide.request.transition.DrawableCrossFadeFactory]
 *
 * 使用时将构建完成的该实例传入 [DrawableTransitionOptions.with] 进行包装后传入 [RequestBuilder.transition]
 *
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2019-09-09 16:54
 */
class DrawableCrossFadeFactory(
    private val duration: Int = DEFAULT_DURATION_MS,
    private val isCrossFadeEnabled: Boolean = true
) : TransitionFactory<Drawable> {

    private val resourceTransition: DrawableCrossFadeTransition by lazy(LazyThreadSafetyMode.NONE) {
        DrawableCrossFadeTransition(duration, isCrossFadeEnabled)
    }

    override fun build(dataSource: DataSource, isFirstResource: Boolean): Transition<Drawable> {
        return if (dataSource == DataSource.MEMORY_CACHE)
            NoTransition.get()
        else resourceTransition
    }

    companion object {
        /**
         * Glide 默认的淡入淡出时长：
         * [com.bumptech.glide.request.transition.DrawableCrossFadeFactory.Builder.DEFAULT_DURATION_MS]
         */
        internal const val DEFAULT_DURATION_MS = 300
    }
}
