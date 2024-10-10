package io.github.chenfei0928.util.glide

import android.graphics.drawable.Drawable
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

/**
 * 对加载到的Drawable进行淡入淡出
 */
@Suppress("kotlin:S6530")
fun <T : Drawable, Req : RequestBuilder<T>> Req.crossFade(
    duration: Int = DrawableCrossFadeFactory.DEFAULT_DURATION_MS
): Req = transition(DrawableTransitionOptions.with(DrawableCrossFadeFactory(duration))) as Req

/**
 * 禁止对图进行动画变换
 */
@Suppress("kotlin:S6530")
fun <T, Req : RequestBuilder<T>> Req.noTransition(): Req {
    return transition(GenericTransitionOptions<T>().dontTransition()) as Req
}
