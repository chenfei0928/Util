package com.chenfei.library.util

import android.graphics.drawable.AnimationDrawable

/**
 * 获取帧动画的总时长
 */
val AnimationDrawable.duration: Int
    get() {
        var duration = 0
        for (i in 0 until numberOfFrames) {
            duration += getDuration(i)
        }
        return duration
    }
