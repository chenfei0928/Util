package io.github.chenfei0928.view

import android.os.Build
import android.view.Choreographer
import androidx.annotation.RequiresApi

/**
 * @author chenfei()
 * @date 2022-07-06 18:54
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
inline fun Choreographer.postFrameCallbackDelayed(
    delayMillis: Long, crossinline block: (Long) -> Unit
) {
    postFrameCallbackDelayed({ block(it) }, delayMillis)
}
