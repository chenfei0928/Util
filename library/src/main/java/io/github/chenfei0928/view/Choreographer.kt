package io.github.chenfei0928.view

import android.view.Choreographer

/**
 * @author chenfei()
 * @date 2022-07-06 18:54
 */
inline fun Choreographer.postFrameCallbackDelayed(
    delayMillis: Long, crossinline block: (Long) -> Unit
) = postFrameCallbackDelayed({ block(it) }, delayMillis)
