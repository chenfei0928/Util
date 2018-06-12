package com.chenfei.library.util.kotlin

import android.os.Build
import android.support.annotation.RequiresApi
import android.view.Choreographer

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
inline fun Choreographer.postFrameCallbackDelayed(delayMillis: Long, crossinline block: (Long) -> Unit) {
    postFrameCallbackDelayed({ block(it) }, delayMillis)
}
