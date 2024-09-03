package io.github.chenfei0928.util.glide

import android.graphics.drawable.Drawable
import androidx.annotation.Px
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * @author chenf()
 * @date 2023-02-20 18:04
 */
private class GlideAwait<TranscodeType>(
    width: Int, height: Int,
    private val continuation: CancellableContinuation<TranscodeType>
) : CustomTarget<TranscodeType>(width, height) {

    override fun onLoadFailed(errorDrawable: Drawable?) {
        super.onLoadFailed(errorDrawable)
        continuation.resumeWithException(RuntimeException("onLoadFailed"))
    }

    override fun onLoadCleared(placeholder: Drawable?) {
        // noop
    }

    override fun onResourceReady(
        resource: TranscodeType & Any,
        transition: Transition<in TranscodeType>?
    ) {
        continuation.resume(resource)
    }
}

/**
 * 通过协程获取Glide加载到的图片资源
 *
 * @return 加载到的图片资源
 */
suspend fun <TranscodeType> RequestBuilder<TranscodeType>.await(
    @Px width: Int = Target.SIZE_ORIGINAL,
    @Px height: Int = Target.SIZE_ORIGINAL
): TranscodeType = suspendCancellableCoroutine { continuation ->
    // 获取view尺寸后开启加载过程
    val glideAwait = GlideAwait(width, height, continuation)
    into(glideAwait)
    continuation.invokeOnCancellation {
        glideAwait.request?.clear()
    }
}
