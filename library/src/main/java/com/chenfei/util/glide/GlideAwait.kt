package com.chenfei.util.glide

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.ImageViewTargetFactory
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.chenfei.util.Log
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ExecutionException
import kotlin.coroutines.resumeWithException

private const val TAG = "KW_GlideAwait"

/**
 * 通过协程返回Glide加载结果
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-03-12 16:37
 */
private class GlideAwait<T>(
    private val continuation: CancellableContinuation<T>, width: Int, height: Int
) : CustomTarget<T>(width, height), RequestListener<T> {

    override fun onLoadFailed(
        e: GlideException?, model: Any?, target: Target<T>?, isFirstResource: Boolean
    ): Boolean {
        Log.w(TAG, "onLoadFailed: $model", e)
        continuation.resumeWithException(ExecutionException(e))
        return false
    }

    override fun onResourceReady(
        resource: T,
        model: Any?,
        target: Target<T>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {
        continuation.resumeWith(Result.success(resource))
        return false
    }

    override fun onResourceReady(resource: T, transition: Transition<in T>?) {
        continuation.resumeWith(Result.success(resource))
    }

    override fun onLoadCleared(placeholder: Drawable?) {
    }
}

/**
 * 通过协程获取Glide加载到的图片资源
 *
 * @param T 资源类型
 * @param width 宽
 * @param height 高
 * @return 加载到的图片资源
 */
suspend fun <T> RequestBuilder<T>.await(width: Int, height: Int): T {
    return suspendCancellableCoroutine { continuation ->
        val glideAwait = GlideAwait(continuation, width, height)
        into(glideAwait)
        continuation.invokeOnCancellation {
            Log.d(TAG, "await: invokeOnCancellation $this", it)
            glideAwait.request?.clear()
        }
    }
}

/**
 * 通过协程获取Glide加载到的图片资源
 *
 * @param T 资源类型
 * @param view 目标ImageView
 * @return 加载到的图片资源
 */
suspend fun <T> RequestBuilder<T>.await(view: ImageView): T {
    return suspendCancellableCoroutine { continuation ->
        val target = ImageViewTargetFactory().buildTarget(view, Bitmap::class.java)
        // 获取view尺寸后开启加载过程
        val sizeReadyCallback = SizeReadyCallback { width, height ->
            val glideAwait = GlideAwait(continuation, width, height)
            into(glideAwait)
            continuation.invokeOnCancellation {
                glideAwait.request?.clear()
            }
        }
        // 获取尺寸
        target.waitForLayout()
        target.getSize(sizeReadyCallback)
        continuation.invokeOnCancellation {
            target.removeCallback(sizeReadyCallback)
        }
    }
}
