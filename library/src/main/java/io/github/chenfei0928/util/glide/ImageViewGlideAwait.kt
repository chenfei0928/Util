package io.github.chenfei0928.util.glide

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 通过协程返回Glide加载结果
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-03-12 16:37
 */
private class ImageViewGlideAwait(
    private val continuation: CancellableContinuation<Drawable>,
    view: ImageView,
) : CustomViewTarget<ImageView, Drawable>(view), Transition.ViewAdapter {
    private var animatable: Animatable? = null

    override fun onResourceLoading(placeholder: Drawable?) {
        super.onResourceLoading(placeholder)
        setResourceInternal(null)
        view.setImageDrawable(placeholder)
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
        setResourceInternal(null)
        view.setImageDrawable(errorDrawable)
        continuation.resumeWithException(RuntimeException("onLoadFailed"))
    }

    override fun onResourceCleared(placeholder: Drawable?) {
        animatable?.stop()
        setResourceInternal(null)
        view.setImageDrawable(placeholder)
    }

    override fun getCurrentDrawable(): Drawable? {
        return view.drawable
    }

    override fun setDrawable(drawable: Drawable?) {
        if (drawable != null) {
            continuation.resume(drawable)
        }
    }

    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
        if (transition == null || !transition.transition(resource, this)) {
            setResourceInternal(resource)
        } else {
            maybeUpdateAnimatable(resource)
        }
    }

    private fun setResourceInternal(resource: Drawable?) {
        // Order matters here. Set the resource first to make sure that the Drawable has a valid and
        // non-null Callback before starting it.
        view.setImageDrawable(resource)
        maybeUpdateAnimatable(resource)
    }

    private fun maybeUpdateAnimatable(resource: Drawable?) {
        if (resource is Animatable) {
            animatable = resource
            resource.start()
        } else {
            animatable = null
        }
    }
}

/**
 * 通过协程获取Glide加载到的图片资源
 *
 * @param view 目标ImageView
 * @return 加载到的图片资源
 */
suspend fun RequestBuilder<Drawable>.await(
    view: ImageView,
): Drawable = suspendCancellableCoroutine { continuation ->
    // 获取view尺寸后开启加载过程
    val glideAwait = ImageViewGlideAwait(continuation, view)
    into(glideAwait)
    continuation.invokeOnCancellation {
        glideAwait.request?.clear()
    }
}
